package dev.base.workflow.domain.executor.trigger;

import dev.base.workflow.domain.engine.NodeExecutor;
import dev.base.workflow.model.core.ExecutionContext;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import dev.base.workflow.model.nodetype.NodeType;
import dev.base.workflow.model.nodetype.TriggerNodeType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WebhookTriggerExecutor implements NodeExecutor {

    @Override
    public NodeType getSupportedNodeType() {
        return TriggerNodeType.WEBHOOK;
    }

    @Override
    public NodeExecutionResult execute(NodeDefinition node, Object input, ExecutionContext ctx) {
        // Webhooks start the flow.
        return NodeExecutionResult.success(node.getId(),
                Map.of("trigger", "webhook", "payload", input != null ? input : "{}"));
    }
}
