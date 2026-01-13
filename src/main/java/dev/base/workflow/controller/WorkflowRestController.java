package dev.base.workflow.controller;

import dev.base.workflow.model.dto.ApiResponse;
import dev.base.workflow.mongo.collection.WorkflowDefinition;
import dev.base.workflow.mongo.collection.WorkflowExecution;
import dev.base.workflow.mongo.collection.WorkflowRun;
import dev.base.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import dev.base.workflow.model.dto.ExecuteWorkflowRequest;
import dev.base.workflow.model.dto.WorkflowStatusResponse;

import java.util.List;

import static dev.base.workflow.constant.WorkflowResponseConstants.*;

/**
 * REST API controller for workflow management and execution.
 * All endpoints return standardized ApiResponse wrapper.
 */
@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowRestController {

    private final WorkflowService workflowService;

    @GetMapping
    public ApiResponse<List<WorkflowDefinition>> getAllWorkflows() {
        return ApiResponse.success(workflowService.getAllWorkflows());
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkflowDefinition> getWorkflow(@PathVariable String id) {
        return ApiResponse.success(workflowService.getWorkflow(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WorkflowDefinition> createWorkflow(@RequestBody WorkflowDefinition workflow) {
        return ApiResponse.success(workflowService.saveWorkflow(workflow), MSG_WORKFLOW_CREATED);
    }

    @PutMapping("/{id}")
    public ApiResponse<WorkflowDefinition> updateWorkflow(@PathVariable String id,
            @RequestBody WorkflowDefinition workflow) {
        workflow.setId(id);
        return ApiResponse.success(workflowService.saveWorkflow(workflow), MSG_WORKFLOW_UPDATED);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWorkflow(@PathVariable String id) {
        workflowService.deleteWorkflow(id);
        return ApiResponse.success(MSG_WORKFLOW_DELETED);
    }

    @PostMapping("/{id}/execute")
    public ApiResponse<Object> executeWorkflow(@PathVariable String id,
            @RequestBody(required = false) ExecuteWorkflowRequest request) {
        Object result = workflowService.executeWorkflow(id, request != null ? request.getInput() : null);
        return ApiResponse.success(result, MSG_WORKFLOW_EXECUTED);
    }

    @GetMapping("/{id}/executions")
    public ApiResponse<List<WorkflowExecution>> getExecutionHistory(@PathVariable String id) {
        return ApiResponse.success(workflowService.getExecutionHistory(id));
    }

    @GetMapping("/{id}/runs")
    public ApiResponse<List<WorkflowRun>> getWorkflowRuns(@PathVariable String id) {
        return ApiResponse.success(workflowService.getWorkflowRuns(id));
    }

    @GetMapping("/runs/{runId}/executions")
    public ApiResponse<List<WorkflowExecution>> getExecutionsForRun(@PathVariable String runId) {
        return ApiResponse.success(workflowService.getExecutionsForRun(runId));
    }

    @PostMapping("/{id}/stop")
    public ApiResponse<Void> stopWorkflow(@PathVariable String id) {
        workflowService.stopWorkflow(id);
        return ApiResponse.success(MSG_WORKFLOW_STOPPED);
    }

    @GetMapping("/{id}/status")
    public ApiResponse<WorkflowStatusResponse> getWorkflowStatus(@PathVariable String id) {
        boolean isRunning = workflowService.isWorkflowRunning(id);
        var executionStatus = workflowService.getWorkflowExecutionStatus(id);

        return ApiResponse.success(WorkflowStatusResponse.builder()
                .isRunning(isRunning)
                .status(executionStatus)
                .build());
    }
}
