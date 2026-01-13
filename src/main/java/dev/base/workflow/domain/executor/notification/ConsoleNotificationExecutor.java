package dev.base.workflow.domain.executor.notification;

import dev.base.workflow.domain.engine.NodeExecutor;
import dev.base.workflow.model.core.ExecutionContext;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import dev.base.workflow.model.nodetype.NodeType;
import dev.base.workflow.model.nodetype.NotificationNodeType;
import dev.base.workflow.util.StringUtils;
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

        System.out.println(StringUtils.concat("[CONSOLE NODE]: ", message));

        return NodeExecutionResult.success(node.getId(), input);
    }
}
