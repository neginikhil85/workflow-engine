package dev.base.workflow.domain.executor.integration.activemq;

import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.base.workflow.constant.ActiveMQConstants.*;
import static dev.base.workflow.constant.WorkflowConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActiveMQConsumerService {

    private final ActiveMQPropertiesBuilder propertiesBuilder;

    public NodeExecutionResult consume(NodeDefinition node, Map<String, Object> config) {
        try {
            JmsTemplate jmsTemplate = createConfiguredJmsTemplate(config);
            String destinationName = getDestinationName(config);
            int pollTimeout = getPollTimeout(config);
            int batchSize = getBatchSize(config);

            log.info("Polling ActiveMQ destination [{}] for {} ms (Batch Size: {})",
                    destinationName, pollTimeout, batchSize);

            List<Map<String, Object>> messages = new ArrayList<>();

            Message firstMessage = fetchFirstMessage(jmsTemplate, destinationName);
            if (firstMessage != null) {
                messages.add(extractMessageData(firstMessage));
                fetchRemainingMessages(jmsTemplate, destinationName, messages, batchSize);
            }

            return buildExecutionResult(node.getId(), destinationName, messages);

        } catch (Exception e) {
            log.error("Failed to consume from ActiveMQ", e);
            throw new RuntimeException("ActiveMQ Consume Error: " + e.getMessage(), e);
        }
    }

    private Message fetchFirstMessage(JmsTemplate jmsTemplate, String destinationName) {
        return jmsTemplate.receive(destinationName);
    }

    private void fetchRemainingMessages(JmsTemplate jmsTemplate, String destinationName,
            List<Map<String, Object>> messages, int batchSize) throws Exception {
        if (batchSize <= 1)
            return;

        jmsTemplate.setReceiveTimeout(100); // Short timeout for batch collection

        while (messages.size() < batchSize) {
            Message nextMessage = jmsTemplate.receive(destinationName);
            if (nextMessage == null)
                break;
            messages.add(extractMessageData(nextMessage));
        }
    }

    private JmsTemplate createConfiguredJmsTemplate(Map<String, Object> config) throws Exception {
        ActiveMQConnectionFactory connectionFactory = propertiesBuilder.buildConnectionFactory(config);
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);

        jmsTemplate.setReceiveTimeout(getPollTimeout(config));
        jmsTemplate.setPubSubDomain(isTopic(config));

        return jmsTemplate;
    }

    private Map<String, Object> extractMessageData(Message message) throws Exception {
        String body = (message instanceof TextMessage)
                ? ((TextMessage) message).getText()
                : message.toString();

        return Map.of(
                CFG_MESSAGE_BODY, body,
                "messageId", message.getJMSMessageID() != null ? message.getJMSMessageID() : "",
                "timestamp", message.getJMSTimestamp());
    }

    private NodeExecutionResult buildExecutionResult(String nodeId, String destinationName,
            List<Map<String, Object>> messages) {
        return NodeExecutionResult.success(nodeId, Map.of(
                KEY_STATUS, "consumed",
                "destination", destinationName,
                KEY_MESSAGE_COUNT, messages.size(),
                KEY_MESSAGES, messages));
    }

    // --- Config Helpers ---

    private String getDestinationName(Map<String, Object> config) {
        return (String) config.get(CFG_DESTINATION_NAME);
    }

    private boolean isTopic(Map<String, Object> config) {
        String type = (String) config.getOrDefault(CFG_DESTINATION_TYPE, DESTINATION_TYPE_QUEUE);
        return DESTINATION_TYPE_TOPIC.equalsIgnoreCase(type);
    }

    private int getPollTimeout(Map<String, Object> config) {
        return config.containsKey(CFG_POLL_TIMEOUT_MS)
                ? ((Number) config.get(CFG_POLL_TIMEOUT_MS)).intValue()
                : 5000;
    }

    private int getBatchSize(Map<String, Object> config) {
        return config.containsKey(CFG_BATCH_SIZE)
                ? ((Number) config.get(CFG_BATCH_SIZE)).intValue()
                : 50; // Default batch size
    }
}
