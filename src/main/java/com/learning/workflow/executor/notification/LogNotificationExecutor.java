package com.learning.workflow.executor.notification;

import com.learning.workflow.engine.NodeExecutor;
import com.learning.workflow.model.core.ExecutionContext;
import com.learning.workflow.model.core.NodeDefinition;
import com.learning.workflow.model.core.NodeExecutionResult;
import com.learning.workflow.model.nodetype.NotificationNodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class LogNotificationExecutor implements NodeExecutor {

    public static final String LEVEL = "level";
    public static final String INFO = "INFO";
    public static final String ERROR = "ERROR";
    public static final String WARN = "WARN";
    public static final String DEBUG = "DEBUG";

    @Override
    public NotificationNodeType getSupportedNodeType() {
        return NotificationNodeType.LOG;
    }

    @Override
    public com.learning.workflow.model.core.NodeExecutionResult execute(NodeDefinition node, Object input,
            ExecutionContext ctx) {
        Map<String, Object> config = node.getConfig();
        String logLevel = config != null && config.containsKey(LEVEL)
                ? (String) config.get(LEVEL)
                : INFO;

        String message = "Workflow [" + ctx.get("workflowId") + "] Node [" + node.getId() + "] Output: " + input;

        switch (logLevel.toUpperCase()) {
            case ERROR:
                log.error(message);
                break;
            case WARN:
                log.warn(message);
                break;
            case DEBUG:
                log.debug(message);
                break;
            default:
                log.info(message);
                break;
        }

        return NodeExecutionResult.success(node.getId(), input);
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        return Map.of(LEVEL, INFO);
    }
}
