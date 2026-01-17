package dev.base.workflow.domain.executor.integration.kafka;

import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import dev.base.workflow.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

import static dev.base.workflow.constant.KafkaConstants.*;
import static dev.base.workflow.constant.WorkflowConstants.*;
import static dev.base.workflow.constant.WorkflowErrorConstants.ERR_KAFKA_TOPIC_CONSUMER;

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
        String topic = (String) config.get(CFG_TOPIC);
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException(ERR_KAFKA_TOPIC_CONSUMER);
        }

        String consumerGroup = (String) config.getOrDefault(CFG_CONSUMER_GROUP,
                StringUtils.concat("workflow-consumer-", UUID.randomUUID()));
        int pollTimeout = config.containsKey(CFG_POLL_TIMEOUT_MS)
                ? ((Number) config.get(CFG_POLL_TIMEOUT_MS)).intValue()
                : 5000;

        Properties properties = propertiesBuilder.buildConsumerProperties(config, consumerGroup);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            consumer.subscribe(Collections.singletonList(topic));

            log.info("Polling Kafka topic: {} for {} ms", topic, pollTimeout);

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(pollTimeout));

            List<Map<String, Object>> messages = new ArrayList<>();
            for (ConsumerRecord<String, String> record : records) {
                messages.add(Map.of(
                        CFG_KEY, record.key() != null ? record.key() : "",
                        CFG_VALUE, record.value(),
                        KEY_PARTITION, record.partition(),
                        KEY_OFFSET, record.offset(),
                        KEY_TIMESTAMP, record.timestamp()));
            }

            log.info("Consumed {} messages from Kafka topic: {}", messages.size(), topic);

            return NodeExecutionResult.success(node.getId(), Map.of(
                    KEY_STATUS, "consumed",
                    CFG_TOPIC, topic,
                    KEY_MESSAGE_COUNT, messages.size(),
                    KEY_MESSAGES, messages));

        } catch (Exception e) {
            log.error("Failed to consume from Kafka", e);
            throw new RuntimeException(StringUtils.concat("Failed to consume from Kafka: ", e.getMessage()), e);
        }
    }
}
