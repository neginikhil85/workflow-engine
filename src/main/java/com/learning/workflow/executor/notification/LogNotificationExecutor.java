package com.learning.workflow.executor.notification;

import com.learning.workflow.engine.NodeExecutor;
import com.learning.workflow.model.core.ExecutionContext;
import com.learning.workflow.model.core.NodeDefinition;
import com.learning.workflow.model.nodetype.NotificationNodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class LogNotificationExecutor implements NodeExecutor {

    @Override
    public NotificationNodeType getSupportedNodeType() {
        return NotificationNodeType.LOG;
    }

    @Override
    public com.learning.workflow.model.core.NodeExecutionResult execute(NodeDefinition node, Object input,
            ExecutionContext ctx) {
        Map<String, Object> config = node.getConfig();
        String logLevel = config != null && config.containsKey("level")
                ? (String) config.get("level")
                : "INFO";

        String message = "Workflow [" + ctx.get("workflowId") + "] Node [" + node.getId() + "] Output: " + input;

        switch (logLevel.toUpperCase()) {
            case "ERROR":
                log.error(message);
                break;
            case "WARN":
                log.warn(message);
                break;
            case "DEBUG":
                log.debug(message);
                break;
            default:
                log.info(message);
                break;
        }

        return com.learning.workflow.model.core.NodeExecutionResult.success(node.getId(), input);
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        return Map.of("level", "INFO");
    }
}
