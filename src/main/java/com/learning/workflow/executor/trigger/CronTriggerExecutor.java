package com.learning.workflow.executor.trigger;

import com.learning.workflow.engine.NodeExecutor;
import com.learning.workflow.model.core.ExecutionContext;
import com.learning.workflow.model.core.NodeDefinition;
import com.learning.workflow.model.core.NodeExecutionResult;
import com.learning.workflow.model.nodetype.NodeType;
import com.learning.workflow.model.nodetype.TriggerNodeType;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.learning.workflow.constant.WorkflowConstants.*;

@Component
@lombok.RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class CronTriggerExecutor implements NodeExecutor {

    private final com.learning.workflow.service.WorkflowScheduler workflowScheduler;

    @Override
    public NodeType getSupportedNodeType() {
        return TriggerNodeType.CRON;
    }

    @Override
    public NodeExecutionResult execute(NodeDefinition node, Object input, ExecutionContext ctx) {
        String cronExpression = (String) node.getConfig().get(CFG_CRON);
        String workflowId = (String) ctx.get("workflowId");
        String runId = (String) ctx.get(KEY_RUN_ID);

        log.info("Executing CronTriggerExecutor for workflow: {}", workflowId);

        if (workflowId != null && cronExpression != null && !cronExpression.isBlank()) {
            log.info("Scheduling cron task for workflow {} with expression: {} and runId: {}", workflowId,
                    cronExpression, runId);
            workflowScheduler.scheduleCronTask(workflowId, cronExpression, runId);
        } else {
            log.warn("Skipping scheduling: workflowId={} cronExpression={}", workflowId, cronExpression);
        }

        return NodeExecutionResult.success(node.getId(), Map.of(
                "trigger", CFG_CRON,
                "expression", cronExpression != null ? cronExpression : "unknown"));
    }
}
