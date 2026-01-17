package dev.base.workflow.service.execution.helper;

import dev.base.workflow.domain.engine.WorkflowRunResult;
import dev.base.workflow.model.core.ExecutionStatus;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import dev.base.workflow.mongo.collection.WorkflowExecution;
import dev.base.workflow.mongo.repository.NodeExecutionResultRepository;
import dev.base.workflow.mongo.repository.WorkflowExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static dev.base.workflow.constant.WorkflowConstants.*;
import static dev.base.workflow.constant.WorkflowErrorConstants.ERR_EXECUTION_CANCELLED;

/**
 * Helper class for managing WorkflowExecution records.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionHelper {

    private final WorkflowExecutionRepository executionRepository;
    private final NodeExecutionResultRepository nodeResultRepository;

    public WorkflowExecution createExecution(String workflowId, String runId) {
        WorkflowExecution execution = new WorkflowExecution();
        execution.setWorkflowId(workflowId);
        execution.setRunId(runId);
        execution.setStatus(ExecutionStatus.RUNNING);
        execution.setStartedAt(LocalDateTime.now());
        return executionRepository.save(execution);
    }

    public void completeExecution(WorkflowExecution execution, WorkflowRunResult result) {
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

    public void failExecution(WorkflowExecution execution, Exception e, boolean isInterrupted) {
        if (isInterrupted) {
            execution.setStatus(ExecutionStatus.CANCELLED);
            execution.setError(ERR_EXECUTION_CANCELLED);
        } else {
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setError(e.getMessage());
        }
        execution.setCompletedAt(LocalDateTime.now());
        executionRepository.save(execution);
    }

    public List<WorkflowExecution> findRunningExecutions(String workflowId) {
        return executionRepository.findByWorkflowIdAndStatus(workflowId, ExecutionStatus.RUNNING);
    }

    public void cancelExecutionRecord(WorkflowExecution execution, String error) {
        execution.setStatus(ExecutionStatus.CANCELLED);
        execution.setCompletedAt(LocalDateTime.now());
        execution.setError(error);
        executionRepository.save(execution);
    }
}
