package com.learning.workflow.controller;

import com.learning.workflow.dto.ApiResponse;
import com.learning.workflow.model.core.WorkflowDefinition;
import com.learning.workflow.model.core.WorkflowExecution;
import com.learning.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        return ApiResponse.success(workflowService.saveWorkflow(workflow), "Workflow created successfully");
    }

    @PutMapping("/{id}")
    public ApiResponse<WorkflowDefinition> updateWorkflow(@PathVariable String id,
            @RequestBody WorkflowDefinition workflow) {
        workflow.setId(id);
        return ApiResponse.success(workflowService.saveWorkflow(workflow), "Workflow updated successfully");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWorkflow(@PathVariable String id) {
        workflowService.deleteWorkflow(id);
        return ApiResponse.success("Workflow deleted successfully");
    }

    @PostMapping("/{id}/execute")
    public ApiResponse<Object> executeWorkflow(@PathVariable String id,
            @RequestBody(required = false) Map<String, Object> input) {
        Object result = workflowService.executeWorkflow(id, input);
        return ApiResponse.success(result, "Workflow executed successfully");
    }

    @GetMapping("/{id}/executions")
    public ApiResponse<List<WorkflowExecution>> getExecutionHistory(@PathVariable String id) {
        return ApiResponse.success(workflowService.getExecutionHistory(id));
    }

    @GetMapping("/{id}/runs")
    public ApiResponse<List<com.learning.workflow.model.core.WorkflowRun>> getWorkflowRuns(@PathVariable String id) {
        return ApiResponse.success(workflowService.getWorkflowRuns(id));
    }

    @GetMapping("/runs/{runId}/executions")
    public ApiResponse<List<WorkflowExecution>> getExecutionsForRun(@PathVariable String runId) {
        return ApiResponse.success(workflowService.getExecutionsForRun(runId));
    }

    @PostMapping("/{id}/stop")
    public ApiResponse<Void> stopWorkflow(@PathVariable String id) {
        workflowService.stopWorkflow(id);
        return ApiResponse.success("Workflow stopped successfully");
    }

    @GetMapping("/{id}/status")
    public ApiResponse<Map<String, Object>> getWorkflowStatus(@PathVariable String id) {
        boolean isRunning = workflowService.isWorkflowRunning(id);
        var executionStatus = workflowService.getWorkflowExecutionStatus(id);
        return ApiResponse.success(Map.of(
                "isRunning", isRunning,
                "status", executionStatus.name()));
    }
}
