package dev.base.workflow.domain.executor.notification;

import dev.base.workflow.domain.engine.NodeExecutor;
import dev.base.workflow.model.core.ExecutionContext;
import dev.base.workflow.model.nodetype.NotificationNodeType;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import dev.base.workflow.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static dev.base.workflow.constant.WorkflowConstants.KEY_WORKFLOW_ID;

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
    public NodeExecutionResult execute(NodeDefinition node, Object input,
            ExecutionContext ctx) {
        Map<String, Object> config = node.getConfig();
        String logLevel = config != null && config.containsKey(LEVEL)
                ? (String) config.get(LEVEL)
                : INFO;

        String message = StringUtils.format("Workflow [{}] Node [{}] Output: {}",
                ctx.get(KEY_WORKFLOW_ID), node.getId(), input);

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
