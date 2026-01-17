package dev.base.workflow.service;

import dev.base.workflow.model.core.ExecutionStatus;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import dev.base.workflow.mongo.collection.WorkflowDefinition;
import dev.base.workflow.mongo.collection.WorkflowExecution;
import dev.base.workflow.mongo.collection.WorkflowRun;
import dev.base.workflow.service.execution.WorkflowExecutionService;
import dev.base.workflow.service.management.WorkflowManagementService;
import dev.base.workflow.service.query.WorkflowQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Facade service for backward compatibility.
 * Delegates to specialized services:
 * - WorkflowExecutionService: execute, stop
 * - WorkflowManagementService: save, load, delete
 * - WorkflowQueryService: history, status
 */
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowExecutionService executionService;
    private final WorkflowManagementService managementService;
    private final WorkflowQueryService queryService;

    // --- Execution Delegation ---

    public Object executeWorkflow(String workflowId, Object input) {
        return executionService.executeWorkflow(workflowId, input);
    }

    public void executeWorkflowWithRun(String workflowId, Object input, String runId) {
        executionService.executeWorkflowWithRun(workflowId, input, runId);
    }

    public void stopWorkflow(String workflowId) {
        executionService.stopWorkflow(workflowId);
    }

    public void executeWorkflowByTrigger(String workflowId, Object input, WorkflowRun.TriggerType triggerType) {
        executionService.executeWorkflowByTrigger(workflowId, input, triggerType);
    }

    // --- Management Delegation ---

    public WorkflowDefinition saveWorkflow(WorkflowDefinition workflow) {
        return managementService.saveWorkflow(workflow);
    }

    public WorkflowDefinition getWorkflow(String id) {
        return managementService.getWorkflow(id);
    }

    public List<WorkflowDefinition> getAllWorkflows() {
        return managementService.getAllWorkflows();
    }

    public void deleteWorkflow(String id) {
        managementService.deleteWorkflow(id);
    }

    // --- Query Delegation ---

    public List<WorkflowExecution> getExecutionHistory(String workflowId) {
        return queryService.getExecutionHistory(workflowId);
    }

    public List<WorkflowRun> getWorkflowRuns(String workflowId) {
        return queryService.getWorkflowRuns(workflowId);
    }

    public List<WorkflowExecution> getExecutionsForRun(String runId) {
        return queryService.getExecutionsForRun(runId);
    }

    public List<NodeExecutionResult> getNodeExecutionResults(String runId) {
        return queryService.getNodeExecutionResults(runId);
    }

    public boolean isWorkflowRunning(String workflowId) {
        return queryService.isWorkflowRunning(workflowId);
    }

    public ExecutionStatus getWorkflowExecutionStatus(String workflowId) {
        return queryService.getWorkflowExecutionStatus(workflowId);
    }
}
