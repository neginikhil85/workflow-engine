package dev.base.workflow.domain.executor.integration.kafka;

import dev.base.workflow.domain.engine.ExpressionEvaluator;
import dev.base.workflow.model.core.ExecutionContext;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import dev.base.workflow.util.StringUtils;
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

import static dev.base.workflow.constant.KafkaConstants.*;
import static dev.base.workflow.constant.WorkflowConstants.KEY_STATUS;
import static dev.base.workflow.constant.WorkflowErrorConstants.ERR_KAFKA_TOPIC_PRODUCER;

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
        String topic = (String) config.get(CFG_TOPIC);
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException(ERR_KAFKA_TOPIC_PRODUCER);
        }

        // Get message template from config
        String messageTemplate = (String) config.get(CFG_MESSAGE_TEMPLATE);
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
            String messageKey = (String) config.get(CFG_MESSAGE_KEY);
            if (messageKey != null && !messageKey.isBlank()) {
                record = new ProducerRecord<>(topic, messageKey, message);
            }

            Future<RecordMetadata> future = producer.send(record);
            RecordMetadata metadata = future.get(10, TimeUnit.SECONDS);

            log.info("Kafka message sent to topic: {} partition: {} offset: {}",
                    metadata.topic(), metadata.partition(), metadata.offset());

            return NodeExecutionResult.success(node.getId(), Map.of(
                    KEY_STATUS, "sent",
                    CFG_TOPIC, metadata.topic(),
                    KEY_PARTITION, metadata.partition(),
                    KEY_OFFSET, metadata.offset(),
                    KEY_TIMESTAMP, metadata.timestamp()));

        } catch (Exception e) {
            log.error("Failed to send Kafka message", e);
            throw new RuntimeException(StringUtils.concat("Failed to produce message to Kafka: ", e.getMessage()), e);
        }
    }
}
