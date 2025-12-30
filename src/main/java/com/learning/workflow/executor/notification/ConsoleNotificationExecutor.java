package com.learning.workflow.executor.notification;

import com.learning.workflow.engine.NodeExecutor;
import com.learning.workflow.model.core.ExecutionContext;
import com.learning.workflow.model.core.NodeDefinition;
import com.learning.workflow.model.core.NodeExecutionResult;
import com.learning.workflow.model.nodetype.NodeType;
import com.learning.workflow.model.nodetype.NotificationNodeType;
import org.springframework.stereotype.Component;

@Component
public class ConsoleNotificationExecutor implements NodeExecutor {

    @Override
    public NodeType getSupportedNodeType() {
        return NotificationNodeType.CONSOLE;
    }

    @Override
    public NodeExecutionResult execute(NodeDefinition node, Object input, ExecutionContext ctx) {
        String messageTemplate = (String) node.getConfig().getOrDefault("message", "Workflow Notification: {input}");

        // Simple interpolation
        String message = messageTemplate.replace("{input}", String.valueOf(input));

        System.out.println("[CONSOLE NODE]: " + message);

        return NodeExecutionResult.success(node.getId(), input);
    }
}
