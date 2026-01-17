package dev.base.workflow.service.execution.trigger;

import dev.base.workflow.domain.event.WorkflowTriggerEvent;
import dev.base.workflow.domain.executor.integration.kafka.KafkaPropertiesBuilder;
import dev.base.workflow.mongo.collection.WorkflowRun;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static dev.base.workflow.constant.KafkaConstants.*;
import static dev.base.workflow.constant.WorkflowConstants.*;

/**
 * Manages the lifecycle of Kafka Consumers for Kafka Trigger nodes.
 * Analogous to WorkflowScheduler (which manages Cron tasks).
 * Handles Dynamic Updates and Cleanup (triggered by Execution or Management
 * service).
 * Note: Startup registration is NOT handled here, following the Cron pattern
 * (Requires kickoff).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaTriggerManager {

    private final KafkaPropertiesBuilder propertiesBuilder;
    private final ApplicationEventPublisher eventPublisher;

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Map<String, ConsumerContainer> activeConsumers = new ConcurrentHashMap<>();

    /**
     * Start, Stop, or Restart consumer based on configuration state.
     */
    public synchronized void refreshConsumer(String workflowId, Map<String, Object> config) {
        ConsumerContainer container = activeConsumers.get(workflowId);
        int newConfigHash = config.hashCode();

        if (container != null) {
            if (container.configHash == newConfigHash && container.loop.isRunning()) {
                log.debug("Kafka Consumer for workflow {} is up-to-date.", workflowId);
                return;
            }
            log.info("Configuration changed for workflow {}. Restarting Kafka Consumer.", workflowId);
            stopConsumer(workflowId);
        }

        startConsumer(workflowId, config, newConfigHash);
    }

    private void startConsumer(String workflowId, Map<String, Object> config, int configHash) {
        log.info("Starting Kafka Consumer for workflow: {}", workflowId);
        KafkaConsumerLoop loop = new KafkaConsumerLoop(workflowId, config);
        activeConsumers.put(workflowId, new ConsumerContainer(loop, configHash));
        executorService.submit(loop);
    }

    public synchronized void stopConsumer(String workflowId) {
        ConsumerContainer container = activeConsumers.remove(workflowId);
        if (container != null) {
            log.info("Stopping Kafka Consumer for workflow: {}", workflowId);
            container.loop.shutdown();
        }
    }

    // --- Inner Classes ---

    private static class ConsumerContainer {
        final KafkaConsumerLoop loop;
        final int configHash;

        ConsumerContainer(KafkaConsumerLoop loop, int configHash) {
            this.loop = loop;
            this.configHash = configHash;
        }
    }

    private class KafkaConsumerLoop implements Runnable {
        private final String workflowId;
        private final AtomicBoolean running = new AtomicBoolean(true);
        private final String topic;
        private final Properties properties;
        private final long pollTimeoutMs;

        public KafkaConsumerLoop(String workflowId, Map<String, Object> config) {
            this.workflowId = workflowId;
            this.topic = (String) config.get(CFG_TOPIC);
            String groupId = (String) config.getOrDefault(CFG_CONSUMER_GROUP, "trigger-" + workflowId);
            this.properties = propertiesBuilder.buildConsumerProperties(config, groupId);
            this.pollTimeoutMs = config.containsKey(CFG_POLL_TIMEOUT_MS)
                    ? ((Number) config.get(CFG_POLL_TIMEOUT_MS)).longValue()
                    : 1000L;
        }

        public boolean isRunning() {
            return running.get();
        }

        @Override
        public void run() {
            log.info("Kafka Trigger Loop started for workflow: {} on topic: {}", workflowId, topic);
            try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
                consumer.subscribe(Collections.singletonList(topic));

                while (running.get()) {
                    try {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(pollTimeoutMs));
                        for (ConsumerRecord<String, String> record : records) {
                            handleMessage(record);
                        }
                        if (!records.isEmpty())
                            consumer.commitSync();
                    } catch (Exception e) {
                        log.error("Error in Kafka Trigger Loop for workflow: {}", workflowId, e);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Fatal error in Kafka Trigger Loop for workflow: {}", workflowId, e);
            } finally {
                log.info("Kafka Trigger Loop stopped for workflow: {}", workflowId);
            }
        }

        private void handleMessage(ConsumerRecord<String, String> record) {
            Map<String, Object> input = new HashMap<>();
            input.put(CFG_KEY, record.key());
            input.put(CFG_VALUE, record.value());
            input.put(KEY_PARTITION, record.partition());
            input.put(KEY_OFFSET, record.offset());
            input.put(KEY_TIMESTAMP, record.timestamp());
            input.put(CFG_TOPIC, record.topic());

            log.info("Kafka Trigger fired for workflow: {}. Offset: {}", workflowId, record.offset());
            try {
                eventPublisher
                        .publishEvent(new WorkflowTriggerEvent(this, workflowId, input, WorkflowRun.TriggerType.KAFKA));
            } catch (Exception e) {
                log.error("Failed to publish workflow trigger event: {}", workflowId, e);
            }
        }

        public void shutdown() {
            running.set(false);
        }
    }
}
