package dev.base.workflow.service.execution.helper;

import dev.base.workflow.exception.RunNotFoundException;
import dev.base.workflow.model.nodetype.TriggerNodeType;
import dev.base.workflow.mongo.collection.WorkflowDefinition;
import dev.base.workflow.mongo.collection.WorkflowRun;
import dev.base.workflow.mongo.repository.WorkflowRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static dev.base.workflow.constant.WorkflowConstants.*;

/**
 * Helper class for managing WorkflowRun lifecycle.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowRunHelper {

    private final WorkflowRunRepository runRepository;

    public WorkflowRun getOrCreateRun(String workflowId, String existingRunId, WorkflowRun.TriggerType triggerType) {
        if (existingRunId != null) {
            WorkflowRun run = runRepository.findById(existingRunId)
                    .orElseThrow(() -> new RunNotFoundException(existingRunId));
            if (run.getStatus() == WorkflowRun.RunStatus.STOPPED) {
                log.warn(LOG_SKIPPING_STOPPED, existingRunId);
                return null;
            }
            return run;
        }

        // Check for existing active run
        var activeRun = runRepository.findFirstByWorkflowIdAndStatus(workflowId, WorkflowRun.RunStatus.ACTIVE);
        if (activeRun.isPresent()) {
            log.info("Found existing active run {} for workflow {}, reusing it.", activeRun.get().getId(), workflowId);
            return activeRun.get();
        }

        WorkflowRun run = WorkflowRun.builder()
                .workflowId(workflowId)
                .triggerType(triggerType)
                .status(WorkflowRun.RunStatus.ACTIVE)
                .startTime(LocalDateTime.now())
                .totalExecutions(0)
                .failedExecutions(0)
                .build();
        run = runRepository.save(run);
        log.info(LOG_CREATED_RUN, run.getId());
        return run;
    }

    public void updateRunStats(WorkflowRun run, boolean failed) {
        run.setTotalExecutions(run.getTotalExecutions() + 1);
        if (failed) {
            run.setFailedExecutions(run.getFailedExecutions() + 1);
        }
        run.setLastHeartbeat(LocalDateTime.now());
        runRepository.save(run);
    }

    public void handleOneTimeWorkflowCompletion(WorkflowRun run, WorkflowDefinition workflow,
            WorkflowRun.TriggerType triggerType) {
        if (triggerType == WorkflowRun.TriggerType.MANUAL && !isContinuousWorkflow(workflow)) {
            log.info("Auto-completing run {} for one-time workflow {}", run.getId(), run.getWorkflowId());
            run.setStatus(WorkflowRun.RunStatus.COMPLETED);
            run.setEndTime(LocalDateTime.now());
            runRepository.save(run);
        }
    }

    public void stopActiveRuns(String workflowId) {
        List<WorkflowRun> activeRuns = runRepository.findAllByWorkflowIdAndStatus(workflowId,
                WorkflowRun.RunStatus.ACTIVE);

        if (activeRuns.isEmpty()) {
            log.info("No active runs found to stop for workflow {}", workflowId);
            return;
        }

        for (WorkflowRun run : activeRuns) {
            run.setStatus(WorkflowRun.RunStatus.STOPPED);
            run.setEndTime(LocalDateTime.now());
            runRepository.save(run);
            log.info(LOG_STOPPED_RUN, run.getId());
        }
    }

    public boolean isContinuousWorkflow(WorkflowDefinition workflow) {
        if (CollectionUtils.isEmpty(workflow.getNodes()))
            return false;

        return workflow.getNodes().stream()
                .anyMatch(node -> Arrays.stream(TriggerNodeType.values())
                        .anyMatch(trigger -> trigger.getName().equals(node.getNodeType())));
    }
}
