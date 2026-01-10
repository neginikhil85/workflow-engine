package com.learning.workflow.executor.integration.kafka;

import com.learning.workflow.model.core.NodeDefinition;
import com.learning.workflow.model.core.NodeExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

/**
 * Handles Kafka Consumer operations.
 * Single Responsibility: Only handles message consumption from Kafka topics.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final KafkaPropertiesBuilder propertiesBuilder;

    /**
     * Consumes messages from a Kafka topic.
     *
     * @param node   The node definition
     * @param config Kafka configuration
     * @return NodeExecutionResult with consumed messages
     */
    public NodeExecutionResult consume(NodeDefinition node, Map<String, Object> config) {
        String topic = (String) config.get("topic");
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("Kafka topic is required for consumer");
        }

        String consumerGroup = (String) config.getOrDefault("consumerGroup", "workflow-consumer-" + UUID.randomUUID());
        int pollTimeout = config.containsKey("pollTimeoutMs")
                ? ((Number) config.get("pollTimeoutMs")).intValue()
                : 5000;

        Properties props = propertiesBuilder.buildConsumerProperties(config, consumerGroup);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            log.info("Polling Kafka topic: {} for {} ms", topic, pollTimeout);

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(pollTimeout));

            List<Map<String, Object>> messages = new ArrayList<>();
            for (ConsumerRecord<String, String> record : records) {
                messages.add(Map.of(
                        "key", record.key() != null ? record.key() : "",
                        "value", record.value(),
                        "partition", record.partition(),
                        "offset", record.offset(),
                        "timestamp", record.timestamp()));
            }

            log.info("Consumed {} messages from Kafka topic: {}", messages.size(), topic);

            return NodeExecutionResult.success(node.getId(), Map.of(
                    "status", "consumed",
                    "topic", topic,
                    "messageCount", messages.size(),
                    "messages", messages));

        } catch (Exception e) {
            log.error("Failed to consume from Kafka", e);
            throw new RuntimeException("Failed to consume from Kafka: " + e.getMessage(), e);
        }
    }
}
