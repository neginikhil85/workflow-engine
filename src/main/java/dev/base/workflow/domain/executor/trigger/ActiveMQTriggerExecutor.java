package dev.base.workflow.domain.executor.trigger;

import dev.base.workflow.domain.engine.NodeExecutor;
import dev.base.workflow.model.core.ExecutionContext;
import dev.base.workflow.model.nodetype.NodeType;
import dev.base.workflow.model.nodetype.TriggerNodeType;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import dev.base.workflow.service.execution.trigger.ActiveMQTriggerManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Executor for ActiveMQ Trigger nodes.
 * Delegates lifecycle management to ActiveMQTriggerManager.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ActiveMQTriggerExecutor implements NodeExecutor {

    private final ActiveMQTriggerManager activeMQTriggerManager;

    @Override
    public NodeType getSupportedNodeType() {
        return TriggerNodeType.ACTIVEMQ;
    }

    @Override
    public NodeExecutionResult execute(NodeDefinition node, Object input, ExecutionContext ctx) {
        String workflowId = (String) ctx.get("workflowId");
        log.info("Executing ActiveMQTriggerExecutor for workflow: {}", workflowId);

        // Ensure Consumer is Running / Updated via Manager
        activeMQTriggerManager.refreshConsumer(workflowId, node.getConfig());

        // Pass-through the input (message) to the output
        return NodeExecutionResult.success(node.getId(), input);
    }
}
