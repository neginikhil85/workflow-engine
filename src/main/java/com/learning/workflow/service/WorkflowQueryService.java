package com.learning.workflow.service;

import com.learning.workflow.model.core.ExecutionStatus;
import com.learning.workflow.model.core.WorkflowExecution;
import com.learning.workflow.model.core.WorkflowRun;
import com.learning.workflow.repository.WorkflowExecutionRepository;
import com.learning.workflow.repository.WorkflowRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles workflow status queries and history.
 * Responsibilities: Check running status, Get execution history, Get status.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowQueryService {

    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowRunRepository runRepository;
    private final WorkflowScheduler workflowScheduler;

    /**
     * Get execution history for a workflow
     */
    public List<WorkflowExecution> getExecutionHistory(String workflowId) {
        return executionRepository.findByWorkflowIdOrderByStartedAtDesc(workflowId);
    }

    /**
     * Check if a workflow is currently running or scheduled
     */
    public boolean isWorkflowRunning(String workflowId) {
        return runRepository.findByWorkflowIdAndStatus(workflowId, WorkflowRun.RunStatus.ACTIVE).isPresent()
                || workflowScheduler.isScheduled(workflowId);
    }

    /**
     * Get current execution status of a workflow
     */
    public ExecutionStatus getWorkflowExecutionStatus(String workflowId) {
        // Check for running executions
        List<WorkflowExecution> runningExecs = executionRepository.findByWorkflowIdAndStatus(workflowId,
                ExecutionStatus.RUNNING);
        if (!runningExecs.isEmpty()) {
            return ExecutionStatus.RUNNING;
        }

        // Check if scheduled (cron)
        if (workflowScheduler.isScheduled(workflowId)) {
            return ExecutionStatus.SCHEDULED;
        }

        // Check last execution status
        List<WorkflowExecution> history = executionRepository.findByWorkflowIdOrderByStartedAtDesc(workflowId);
        if (!history.isEmpty()) {
            return history.get(0).getStatus();
        }

        // No executions yet
        return ExecutionStatus.IDLE;
    }

    /**
     * Get all runs for a workflow
     */
    public List<WorkflowRun> getWorkflowRuns(String workflowId) {
        return runRepository.findByWorkflowIdOrderByStartTimeDesc(workflowId);
    }

    /**
     * Get executions for a specific run
     */
    public List<WorkflowExecution> getExecutionsForRun(String runId) {
        return executionRepository.findByRunIdOrderByStartedAtDesc(runId);
    }
}
