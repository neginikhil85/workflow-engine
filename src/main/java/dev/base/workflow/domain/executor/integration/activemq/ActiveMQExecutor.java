package dev.base.workflow.domain.executor.integration.activemq;

import dev.base.workflow.domain.engine.NodeExecutor;
import dev.base.workflow.model.core.ExecutionContext;
import dev.base.workflow.model.nodetype.IntegrationNodeType;
import dev.base.workflow.model.nodetype.NodeType;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static dev.base.workflow.constant.ActiveMQConstants.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class ActiveMQExecutor implements NodeExecutor {

    private final ActiveMQProducerService producerService;
    private final ActiveMQConsumerService consumerService;

    @Override
    public NodeType getSupportedNodeType() {
        return IntegrationNodeType.ACTIVE_MQ;
    }

    @Override
    public NodeExecutionResult execute(NodeDefinition node, Object input, ExecutionContext ctx) {
        Map<String, Object> config = node.getConfig();
        if (config == null) {
            throw new IllegalArgumentException("Configuration required for ActiveMQ Node");
        }

        String mode = (String) config.getOrDefault(CFG_ACTIVEMQ_MODE, MODE_PRODUCER);
        log.info("Executing ActiveMQ node in {} mode", mode);

        if (MODE_CONSUMER.equalsIgnoreCase(mode)) {
            return consumerService.consume(node, config);
        }
        return producerService.produce(node, input, config, ctx);
    }

    @Override
    public void validate(NodeDefinition node) {
        Map<String, Object> config = node.getConfig();
        if (config == null) {
            throw new IllegalArgumentException("Configuration required");
        }

        if (!StringUtils.hasText((String) config.get(CFG_BROKER_URL))) {
            throw new IllegalArgumentException(ERR_BROKER_URL_REQUIRED);
        }
        if (!StringUtils.hasText((String) config.get(CFG_DESTINATION_NAME))) {
            throw new IllegalArgumentException(ERR_DESTINATION_NAME_REQUIRED);
        }
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        return Map.of(
                CFG_BROKER_URL, "tcp://localhost:61616",
                CFG_DESTINATION_TYPE, DESTINATION_TYPE_QUEUE,
                CFG_DESTINATION_NAME, "test.queue",
                CFG_MESSAGE_BODY, "{}",
                CFG_ACTIVEMQ_MODE, MODE_PRODUCER,
                CFG_POLL_TIMEOUT_MS, 5000);
    }
}
