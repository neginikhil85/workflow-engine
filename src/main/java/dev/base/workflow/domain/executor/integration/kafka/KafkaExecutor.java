package dev.base.workflow.domain.executor.integration.kafka;

import dev.base.workflow.domain.engine.NodeExecutor;
import dev.base.workflow.model.core.ExecutionContext;
import dev.base.workflow.model.nodetype.IntegrationNodeType;
import dev.base.workflow.model.nodetype.NodeType;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import dev.base.workflow.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static dev.base.workflow.constant.KafkaConstants.*;
import static dev.base.workflow.constant.WorkflowErrorConstants.*;

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
            throw new IllegalArgumentException(ERR_KAFKA_CONFIG_REQUIRED);
        }

        String mode = (String) config.getOrDefault(CFG_KAFKA_MODE, MODE_PRODUCER);

        log.info("Executing Kafka node in {} mode", mode);

        return switch (mode.toUpperCase()) {
            case MODE_PRODUCER -> producerService.produce(node, input, config, ctx);
            case MODE_CONSUMER -> consumerService.consume(node, config);
            default ->
                throw new IllegalArgumentException(
                        StringUtils.concat("Invalid Kafka mode: ", mode, ". Must be PRODUCER or CONSUMER"));
        };
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        return Map.of(
                CFG_BOOTSTRAP_SERVERS, "localhost:9092",
                CFG_SECURITY_PROTOCOL, VAL_SEC_PROTO_PLAINTEXT,
                CFG_KAFKA_MODE, MODE_PRODUCER,
                CFG_TOPIC, "",
                CFG_MESSAGE_TEMPLATE, "{}",
                CFG_CONSUMER_GROUP, "workflow-consumer-group",
                CFG_POLL_TIMEOUT_MS, 5000);
    }

    @Override
    public void validate(NodeDefinition node) {
        Map<String, Object> config = node.getConfig();
        if (config == null) {
            throw new IllegalArgumentException(ERR_KAFKA_CONFIG_REQUIRED);
        }

        String topic = (String) config.get(CFG_TOPIC);
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException(ERR_KAFKA_TOPIC_REQUIRED);
        }

        String bootstrapServers = (String) config.get(CFG_BOOTSTRAP_SERVERS);
        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            throw new IllegalArgumentException(ERR_KAFKA_BOOTSTRAP_REQUIRED);
        }
    }
}
