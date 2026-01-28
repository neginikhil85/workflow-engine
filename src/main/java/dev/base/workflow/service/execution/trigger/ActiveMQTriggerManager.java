package dev.base.workflow.service.execution.trigger;

import dev.base.workflow.domain.event.WorkflowTriggerEvent;
import dev.base.workflow.domain.executor.integration.activemq.ActiveMQPropertiesBuilder;
import dev.base.workflow.mongo.collection.WorkflowRun;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.base.workflow.constant.ActiveMQConstants.*;
import static dev.base.workflow.constant.WorkflowConstants.KEY_TIMESTAMP;

/**
 * Manages the lifecycle of JMS Consumers (ActiveMQ) for ActiveMQ Trigger nodes.
 * Uses DefaultMessageListenerContainer for robust JMS handling.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ActiveMQTriggerManager {

    private final ActiveMQPropertiesBuilder propertiesBuilder;
    private final ApplicationEventPublisher eventPublisher;

    // Map of WorkflowID -> ListenerContainer
    private final Map<String, ContainerWrapper> activeConsumers = new ConcurrentHashMap<>();

    public synchronized void refreshConsumer(String workflowId, Map<String, Object> config) {
        int newConfigHash = config.hashCode();
        ContainerWrapper wrapper = activeConsumers.get(workflowId);

        if (wrapper != null) {
            if (wrapper.configHash == newConfigHash && wrapper.container.isRunning()) {
                log.debug("ActiveMQ Consumer for workflow {} is up-to-date.", workflowId);
                return;
            }
            log.info("Configuration changed for workflow {}. Restarting ActiveMQ Consumer.", workflowId);
            stopConsumer(workflowId);
        }

        startConsumer(workflowId, config, newConfigHash);
    }

    private void startConsumer(String workflowId, Map<String, Object> config, int configHash) {
        log.info("Starting ActiveMQ Consumer for workflow: {}", workflowId);

        try {
            DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
            container.setConnectionFactory(propertiesBuilder.buildConnectionFactory(config));

            String destinationName = (String) config.get(CFG_DESTINATION_NAME);
            if (destinationName == null)
                destinationName = (String) config.get("destination"); // fallback

            container.setDestinationName(destinationName);

            // Set Topic vs Queue
            String destType = (String) config.getOrDefault(CFG_DESTINATION_TYPE, DESTINATION_TYPE_QUEUE);
            container.setPubSubDomain(DESTINATION_TYPE_TOPIC.equals(destType));

            // Set Message Listener
            container.setMessageListener(new WorkflowMessageListener(workflowId, destinationName));

            // Auto start
            container.setAutoStartup(true);
            container.initialize();
            container.start();

            activeConsumers.put(workflowId, new ContainerWrapper(container, configHash));

        } catch (Exception e) {
            log.error("Failed to start ActiveMQ consumer for workflow: {}", workflowId, e);
        }
    }

    public synchronized void stopConsumer(String workflowId) {
        ContainerWrapper wrapper = activeConsumers.remove(workflowId);
        if (wrapper != null) {
            log.info("Stopping ActiveMQ Consumer for workflow: {}", workflowId);
            wrapper.container.shutdown();
        }
    }

    // --- Inner Classes ---

    private record ContainerWrapper(DefaultMessageListenerContainer container, int configHash) {
    }

    private class WorkflowMessageListener implements MessageListener {
        private final String workflowId;
        private final String destination;

        public WorkflowMessageListener(String workflowId, String destination) {
            this.workflowId = workflowId;
            this.destination = destination;
        }

        @Override
        public void onMessage(Message message) {
            Map<String, Object> input = new HashMap<>();
            try {
                if (message instanceof TextMessage) {
                    input.put(CFG_MESSAGE_BODY, ((TextMessage) message).getText());
                } else {
                    input.put(CFG_MESSAGE_BODY, message.toString());
                }

                input.put(KEY_MESSAGE_ID, message.getJMSMessageID());
                input.put(KEY_CORRELATION_ID, message.getJMSCorrelationID());
                input.put(KEY_DESTINATION, destination);
                input.put(KEY_TIMESTAMP, message.getJMSTimestamp());
                input.put(KEY_TYPE, message.getJMSType());

                log.info("ActiveMQ Trigger fired for workflow: {}. MsgID: {}", workflowId, message.getJMSMessageID());

                eventPublisher.publishEvent(new WorkflowTriggerEvent(
                        this,
                        workflowId,
                        input,
                        WorkflowRun.TriggerType.ACTIVEMQ));

            } catch (Exception e) {
                log.error("Error processing ActiveMQ message for workflow: {}", workflowId, e);
            }
        }
    }
}
