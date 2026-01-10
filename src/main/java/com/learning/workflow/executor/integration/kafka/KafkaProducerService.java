package com.learning.workflow.executor.integration.kafka;

import com.learning.workflow.engine.ExpressionEvaluator;
import com.learning.workflow.model.core.ExecutionContext;
import com.learning.workflow.model.core.NodeDefinition;
import com.learning.workflow.model.core.NodeExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Handles Kafka Producer operations.
 * Single Responsibility: Only handles message production to Kafka topics.
 * Supports expression evaluation in message body using ${...} syntax.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaPropertiesBuilder propertiesBuilder;
    private final ExpressionEvaluator evaluator;

    /**
     * Sends a message to a Kafka topic.
     * Supports expression evaluation in message body (e.g.,
     * ${input.responseBody.id}).
     *
     * @param node   The node definition
     * @param input  Input from the previous node (used for expression evaluation)
     * @param config Kafka configuration
     * @param ctx    Execution context
     * @return NodeExecutionResult with send metadata
     */
    public NodeExecutionResult produce(NodeDefinition node, Object input, Map<String, Object> config,
            ExecutionContext ctx) {
        String topic = (String) config.get("topic");
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("Kafka topic is required for producer");
        }

        // Get message template from config
        String messageTemplate = (String) config.get("message");
        if (messageTemplate == null || messageTemplate.isBlank()) {
            messageTemplate = "{}";
        }

        // Evaluate expressions in message body (supports ${input.fieldName} syntax)
        // This allows passing data from previous nodes like HTTP call responses
        String message = evaluator.parseTemplate(messageTemplate, input, ctx);
        log.debug("Kafka message after expression evaluation: {}", message);

        Properties props = propertiesBuilder.buildProducerProperties(config);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);

            // Optional: message key for partitioning
            String messageKey = (String) config.get("messageKey");
            if (messageKey != null && !messageKey.isBlank()) {
                record = new ProducerRecord<>(topic, messageKey, message);
            }

            Future<RecordMetadata> future = producer.send(record);
            RecordMetadata metadata = future.get(10, TimeUnit.SECONDS);

            log.info("Kafka message sent to topic: {} partition: {} offset: {}",
                    metadata.topic(), metadata.partition(), metadata.offset());

            return NodeExecutionResult.success(node.getId(), Map.of(
                    "status", "sent",
                    "topic", metadata.topic(),
                    "partition", metadata.partition(),
                    "offset", metadata.offset(),
                    "timestamp", metadata.timestamp()));

        } catch (Exception e) {
            log.error("Failed to send Kafka message", e);
            throw new RuntimeException("Failed to produce message to Kafka: " + e.getMessage(), e);
        }
    }
}
