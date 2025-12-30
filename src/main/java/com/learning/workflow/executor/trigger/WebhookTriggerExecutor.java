package com.learning.workflow.executor.trigger;

import com.learning.workflow.engine.NodeExecutor;
import com.learning.workflow.model.core.ExecutionContext;
import com.learning.workflow.model.core.NodeDefinition;
import com.learning.workflow.model.core.NodeExecutionResult;
import com.learning.workflow.model.nodetype.NodeType;
import com.learning.workflow.model.nodetype.TriggerNodeType;
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
