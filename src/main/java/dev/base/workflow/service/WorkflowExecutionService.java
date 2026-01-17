package dev.base.workflow.service;

import dev.base.workflow.domain.engine.WorkflowEngine;
import dev.base.workflow.domain.engine.WorkflowRunResult;
import dev.base.workflow.model.core.ExecutionStatus;
import dev.base.workflow.mongo.collection.WorkflowDefinition;
import dev.base.workflow.mongo.collection.WorkflowExecution;
import dev.base.workflow.mongo.collection.WorkflowRun;
import dev.base.workflow.mongo.repository.WorkflowDefinitionRepository;
import dev.base.workflow.mongo.repository.WorkflowExecutionRepository;
import dev.base.workflow.mongo.repository.WorkflowRunRepository;
import dev.base.workflow.mongo.repository.NodeExecutionResultRepository;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.base.workflow.exception.RunNotFoundException;
import dev.base.workflow.exception.WorkflowNotFoundException;
import static dev.base.workflow.constant.WorkflowConstants.*;
import static dev.base.workflow.constant.WorkflowErrorConstants.*;

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
    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowRunRepository runRepository;
    private final NodeExecutionResultRepository nodeResultRepository;
    private final WorkflowScheduler workflowScheduler;

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
    public Object executeWorkflowWithRun(String workflowId, Object input, String runId) {
        return executeWorkflow(workflowId, input, runId, null);
    }

    /**
     * Core execution logic - creates or reuses a WorkflowRun
     */
    private Object executeWorkflow(String workflowId, Object input, String existingRunId,
            WorkflowRun.TriggerType triggerType) {
        log.info(LOG_STARTING_EXECUTION, workflowId);

        WorkflowDefinition workflow = workflowRepository.findByIdAndActiveTrue(workflowId)
                .orElseThrow(() -> new WorkflowNotFoundException(workflowId));

        // --- WorkflowRun Logic ---
        WorkflowRun run = getOrCreateRun(workflowId, existingRunId, triggerType);
        if (run == null) {
            return Map.of(KEY_SKIPPED, true, KEY_REASON, REASON_RUN_STOPPED);
        }

        // Create execution record linked to Run
        WorkflowExecution execution = createExecution(workflowId, run.getId());
        runningExecutions.put(execution.getId(), Thread.currentThread());

        boolean failed = false;
        try {
            var runResult = workflowEngine.run(workflow, input, run.getId());
            completeExecution(execution, runResult);

            // Auto-complete run for one-time workflows (MANUAL trigger + No continuous
            // nodes)
            if (triggerType == WorkflowRun.TriggerType.MANUAL && !isContinuousWorkflow(workflow)) {
                log.info("Auto-completing run {} for one-time workflow {}", run.getId(), workflowId);
                run.setStatus(WorkflowRun.RunStatus.COMPLETED);
                run.setEndTime(LocalDateTime.now());
                runRepository.save(run);
            }

            return Map.of(
                    KEY_RUN_ID, run.getId(),
                    KEY_OUTPUT, runResult.getOutput() != null ? runResult.getOutput() : DEFAULT_NULL,
                    KEY_EXECUTED_NODES, runResult.getExecutedNodeIds());
        } catch (Exception e) {
            failed = true;
            failExecution(execution, e);
            throw e;
        } finally {
            runningExecutions.remove(execution.getId());
            updateRunStats(run, failed);
        }
    }

    private boolean isContinuousWorkflow(WorkflowDefinition workflow) {
        if (workflow.getNodes() == null)
            return false;

        return workflow.getNodes().stream().anyMatch(node -> {
            String type = node.getType();
            // Check for Cron
            if ("TriggerNodeType_CRON".equals(type) || "TriggerNodeType_WEBHOOK".equals(type))
                return true;

            // Check for Kafka Consumer
            if ("IntegrationNodeType_KAFKA".equals(type)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> config = (Map<String, Object>) node.getConfig();
                return config != null && "CONSUMER".equals(config.get("kafkaMode"));
            }
            return false;
        });
    }

    private WorkflowRun getOrCreateRun(String workflowId, String existingRunId, WorkflowRun.TriggerType triggerType) {
        if (existingRunId != null) {
            WorkflowRun run = runRepository.findById(existingRunId)
                    .orElseThrow(() -> new RunNotFoundException(existingRunId));
            if (run.getStatus() == WorkflowRun.RunStatus.STOPPED) {
                log.warn(LOG_SKIPPING_STOPPED, existingRunId);
                return null;
            }
            return run;
        }

        // Check if there is already an active run for this workflow to prevent
        // duplicates
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

    private WorkflowExecution createExecution(String workflowId, String runId) {
        WorkflowExecution execution = new WorkflowExecution();
        execution.setWorkflowId(workflowId);
        execution.setRunId(runId);
        execution.setStatus(ExecutionStatus.RUNNING);
        execution.setStartedAt(LocalDateTime.now());
        return executionRepository.save(execution);
    }

    private void completeExecution(WorkflowExecution execution, WorkflowRunResult result) {
        execution.setStatus(ExecutionStatus.COMPLETED);
        execution.setCompletedAt(LocalDateTime.now());
        execution.setResult(result.getOutput());
        execution.setExecutedNodes(result.getExecutedNodeIds());
        executionRepository.save(execution);

        // Save detailed node execution results
        if (result.getNodeResults() != null) {
            List<NodeExecutionResult> nodeResults = result.getNodeResults();
            nodeResults.forEach(resultItem -> resultItem.setExecutionId(execution.getId()));
            nodeResultRepository.saveAll(nodeResults);
        }
    }

    private void failExecution(WorkflowExecution execution, Exception e) {
        if (Thread.currentThread().isInterrupted()) {
            execution.setStatus(ExecutionStatus.CANCELLED);
            execution.setError(ERR_EXECUTION_CANCELLED);
        } else {
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setError(e.getMessage());
        }
        execution.setCompletedAt(LocalDateTime.now());
        executionRepository.save(execution);
    }

    private void updateRunStats(WorkflowRun run, boolean failed) {
        run.setTotalExecutions(run.getTotalExecutions() + 1);
        if (failed) {
            run.setFailedExecutions(run.getFailedExecutions() + 1);
        }
        run.setLastHeartbeat(LocalDateTime.now());
        runRepository.save(run);
    }

    /**
     * Stop a running workflow execution
     */
    public void stopWorkflow(String workflowId) {
        log.info(LOG_STOPPING_WORKFLOW, workflowId);

        workflowScheduler.unscheduleWorkflow(workflowId);

        List<WorkflowRun> activeRuns = runRepository.findAllByWorkflowIdAndStatus(workflowId,
                WorkflowRun.RunStatus.ACTIVE);

        if (activeRuns.isEmpty()) {
            log.info("No active runs found to stop for workflow {}", workflowId);
        }

        for (WorkflowRun run : activeRuns) {
            run.setStatus(WorkflowRun.RunStatus.STOPPED);
            run.setEndTime(LocalDateTime.now());
            runRepository.save(run);
            log.info(LOG_STOPPED_RUN, run.getId());
        }

        cancelRunningExecutions(workflowId);
        log.info(LOG_WORKFLOW_STOPPED, workflowId);
    }

    private void cancelRunningExecutions(String workflowId) {
        List<WorkflowExecution> runningExecutionsList = executionRepository.findByWorkflowIdAndStatus(workflowId,
                ExecutionStatus.RUNNING);
        for (WorkflowExecution execution : runningExecutionsList) {
            Thread thread = runningExecutions.get(execution.getId());
            if (thread != null) {
                log.info(LOG_INTERRUPTING_THREAD, execution.getId());
                thread.interrupt();
            }
            execution.setStatus(ExecutionStatus.CANCELLED);
            execution.setCompletedAt(LocalDateTime.now());
            execution.setError(ERR_STOPPED_BY_USER);
            executionRepository.save(execution);
        }
    }
}
