package com.learning.workflow.controller;

import com.learning.workflow.model.core.WorkflowDefinition;
import com.learning.workflow.model.core.WorkflowExecution;
import com.learning.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API controller for workflow management and execution.
 */
@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowRestController {

    private final WorkflowService workflowService;

    @GetMapping
    public List<WorkflowDefinition> getAllWorkflows() {
        return workflowService.getAllWorkflows();
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkflowDefinition> getWorkflow(@PathVariable String id) {
        try {
            return ResponseEntity.ok(workflowService.getWorkflow(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<WorkflowDefinition> createWorkflow(@RequestBody WorkflowDefinition workflow) {
        WorkflowDefinition saved = workflowService.saveWorkflow(workflow);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkflowDefinition> updateWorkflow(@PathVariable String id, 
                                                             @RequestBody WorkflowDefinition workflow) {
        workflow.setId(id);
        WorkflowDefinition updated = workflowService.saveWorkflow(workflow);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable String id) {
        try {
            workflowService.deleteWorkflow(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<Object> executeWorkflow(@PathVariable String id, 
                                                  @RequestBody Map<String, Object> input) {
        try {
            Object result = workflowService.executeWorkflow(id, input);
            return ResponseEntity.ok(Map.of("success", true, "result", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/executions")
    public List<WorkflowExecution> getExecutionHistory(@PathVariable String id) {
        return workflowService.getExecutionHistory(id);
    }
}

