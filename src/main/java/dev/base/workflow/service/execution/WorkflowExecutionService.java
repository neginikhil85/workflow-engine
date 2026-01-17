package dev.base.workflow.service.execution;

import dev.base.workflow.domain.engine.WorkflowEngine;
import dev.base.workflow.exception.WorkflowNotFoundException;
import dev.base.workflow.mongo.collection.WorkflowDefinition;
import dev.base.workflow.mongo.collection.WorkflowExecution;
import dev.base.workflow.mongo.collection.WorkflowRun;
import dev.base.workflow.mongo.repository.WorkflowDefinitionRepository;
import dev.base.workflow.service.execution.helper.WorkflowExecutionHelper;
import dev.base.workflow.service.execution.helper.WorkflowRunHelper;
import dev.base.workflow.service.execution.trigger.KafkaTriggerManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.base.workflow.constant.WorkflowConstants.*;
import static dev.base.workflow.constant.WorkflowErrorConstants.ERR_STOPPED_BY_USER;

/**
 * Handles workflow execution lifecycle.
 * Responsibilities: Execute, Stop, Track running executions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionService {

    private final WorkflowEngine workflowEngine;
    private final WorkflowDefinitionRepository workflowRepository;
    private final WorkflowScheduler workflowScheduler;
    private final KafkaTriggerManager kafkaTriggerManager;
    private final WorkflowRunHelper runHelper;
    private final WorkflowExecutionHelper executionHelper;

    // Track running workflow threads for cancellation
    private final Map<String, Thread> runningExecutions = new ConcurrentHashMap<>();

    /**
     * Execute a workflow by ID (creates a new Run for MANUAL triggers)
     */
    public Object executeWorkflow(String workflowId, Object input) {
        return executeWorkflow(workflowId, input, null, WorkflowRun.TriggerType.MANUAL);
    }

    /**
     * Execute a workflow within an existing Run (used by Cron/Kafka ticks)
     */
    @Transactional
    public void executeWorkflowWithRun(String workflowId, Object input, String runId) {
        executeWorkflow(workflowId, input, runId, null);
    }

    /**
     * Stop a running workflow execution
     */
    public void stopWorkflow(String workflowId) {
        log.info(LOG_STOPPING_WORKFLOW, workflowId);

        workflowScheduler.unscheduleWorkflow(workflowId);
        kafkaTriggerManager.stopConsumer(workflowId);
        runHelper.stopActiveRuns(workflowId);
        cancelRunningExecutions(workflowId);

        log.info(LOG_WORKFLOW_STOPPED, workflowId);
    }

    /**
     * Execute a workflow triggered by an external event (e.g. Kafka, Webhook)
     */
    @Transactional
    public void executeWorkflowByTrigger(String workflowId, Object input, WorkflowRun.TriggerType triggerType) {
        executeWorkflow(workflowId, input, null, triggerType);
    }

    /**
     * Core execution logic
     */
    private Object executeWorkflow(String workflowId, Object input, String existingRunId,
            WorkflowRun.TriggerType triggerType) {
        log.info(LOG_STARTING_EXECUTION, workflowId);
        WorkflowDefinition workflow = resolveWorkflow(workflowId);

        WorkflowRun run = runHelper.getOrCreateRun(workflowId, existingRunId, triggerType);
        if (run == null) {
            return Map.of(KEY_SKIPPED, true, KEY_REASON, REASON_RUN_STOPPED);
        }

        return runWorkflowLogic(workflow, input, run, triggerType);
    }

    private WorkflowDefinition resolveWorkflow(String workflowId) {
        return workflowRepository.findByIdAndActiveTrue(workflowId)
                .orElseThrow(() -> new WorkflowNotFoundException(workflowId));
    }

    private Object runWorkflowLogic(WorkflowDefinition workflow, Object input, WorkflowRun run,
            WorkflowRun.TriggerType triggerType) {
        WorkflowExecution execution = executionHelper.createExecution(workflow.getId(), run.getId());
        runningExecutions.put(execution.getId(), Thread.currentThread());
        boolean failed = false;

        try {
            var runResult = workflowEngine.run(workflow, input, run.getId());
            executionHelper.completeExecution(execution, runResult);
            runHelper.handleOneTimeWorkflowCompletion(run, workflow, triggerType);

            return Map.of(
                    KEY_RUN_ID, run.getId(),
                    KEY_OUTPUT, runResult.getOutput() != null ? runResult.getOutput() : DEFAULT_NULL,
                    KEY_EXECUTED_NODES, runResult.getExecutedNodeIds());
        } catch (Exception e) {
            failed = true;
            executionHelper.failExecution(execution, e, Thread.currentThread().isInterrupted());
            throw e;
        } finally {
            runningExecutions.remove(execution.getId());
            runHelper.updateRunStats(run, failed);
        }
    }

    private void cancelRunningExecutions(String workflowId) {
        List<WorkflowExecution> runningExecutionsList = executionHelper.findRunningExecutions(workflowId);
        for (WorkflowExecution execution : runningExecutionsList) {
            Thread thread = runningExecutions.get(execution.getId());
            if (thread != null) {
                log.info(LOG_INTERRUPTING_THREAD, execution.getId());
                thread.interrupt();
            }
            executionHelper.cancelExecutionRecord(execution, ERR_STOPPED_BY_USER);
        }
    }
}
