package com.learning.workflow.executor.integration.kafka;

import com.learning.workflow.engine.NodeExecutor;
import com.learning.workflow.model.core.ExecutionContext;
import com.learning.workflow.model.core.NodeDefinition;
import com.learning.workflow.model.core.NodeExecutionResult;
import com.learning.workflow.model.nodetype.IntegrationNodeType;
import com.learning.workflow.model.nodetype.NodeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka node executor supporting both Producer and Consumer modes.
 * 
 * SOLID Principles applied:
 * - S (Single Responsibility): This class only coordinates between
 * producer/consumer
 * - O (Open/Closed): New modes can be added without modifying existing code
 * - D (Dependency Inversion): Depends on abstractions (services) not
 * implementations
 * 
 * Delegates actual work to:
 * - KafkaProducerService: Handles message production with expression evaluation
 * - KafkaConsumerService: Handles message consumption
 * - KafkaPropertiesBuilder: Handles property/security configuration
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaExecutor implements NodeExecutor {

    private final KafkaProducerService producerService;
    private final KafkaConsumerService consumerService;

    @Override
    public NodeType getSupportedNodeType() {
        return IntegrationNodeType.KAFKA;
    }

    @Override
    public NodeExecutionResult execute(NodeDefinition node, Object input, ExecutionContext ctx) {
        Map<String, Object> config = node.getConfig();

        if (config == null) {
            throw new IllegalArgumentException("Kafka node configuration is missing");
        }

        String mode = (String) config.getOrDefault("kafkaMode", "PRODUCER");

        log.info("Executing Kafka node in {} mode", mode);

        return switch (mode.toUpperCase()) {
            case "PRODUCER" -> producerService.produce(node, input, config, ctx);
            case "CONSUMER" -> consumerService.consume(node, config);
            default ->
                throw new IllegalArgumentException("Invalid Kafka mode: " + mode + ". Must be PRODUCER or CONSUMER");
        };
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        return Map.of(
                "bootstrapServers", "localhost:9092",
                "securityProtocol", "PLAINTEXT",
                "kafkaMode", "PRODUCER",
                "topic", "",
                "message", "{}",
                "consumerGroup", "workflow-consumer-group",
                "pollTimeoutMs", 5000);
    }

    @Override
    public void validate(NodeDefinition node) {
        Map<String, Object> config = node.getConfig();
        if (config == null) {
            throw new IllegalArgumentException("Kafka configuration is required");
        }

        String topic = (String) config.get("topic");
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("Kafka topic is required");
        }

        String bootstrapServers = (String) config.get("bootstrapServers");
        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            throw new IllegalArgumentException("Bootstrap servers are required");
        }
    }
}
