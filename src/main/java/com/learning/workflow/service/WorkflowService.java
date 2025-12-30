package com.learning.workflow.service;

import com.learning.workflow.engine.WorkflowEngine;
import com.learning.workflow.model.core.WorkflowDefinition;
import com.learning.workflow.model.core.WorkflowExecution;
import com.learning.workflow.repository.WorkflowDefinitionRepository;
import com.learning.workflow.repository.WorkflowExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {

    private final WorkflowEngine workflowEngine;
    private final WorkflowDefinitionRepository workflowRepository;
    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowDefinitionMapper workflowMapper;
    private final WorkflowScheduler workflowScheduler;

    /**
     * Execute a workflow by ID
     */
    public Object executeWorkflow(String workflowId, Object input) {
        log.info(">>> STARTING WORKFLOW EXECUTION: {}", workflowId);

        WorkflowDefinition workflow = workflowRepository.findByIdAndActiveTrue(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow not found: " + workflowId));

        // Create execution record
        WorkflowExecution execution = new WorkflowExecution();
        execution.setWorkflowId(workflowId);
        execution.setStatus("RUNNING");
        execution.setStartedAt(LocalDateTime.now());
        execution = executionRepository.save(execution);

        try {
            // Execute workflow
            com.learning.workflow.engine.WorkflowRunResult runResult = workflowEngine.run(workflow, input);

            // Update execution record
            execution.setStatus("COMPLETED");
            execution.setCompletedAt(LocalDateTime.now());
            execution.setResult(runResult.getOutput());
            execution.setExecutedNodes(runResult.getExecutedNodeIds());
            executionRepository.save(execution);

            return Map.of(
                    "output", runResult.getOutput() != null ? runResult.getOutput() : "null",
                    "executedNodes", runResult.getExecutedNodeIds());
        } catch (Exception e) {
            execution.setStatus("FAILED");
            execution.setError(e.getMessage());
            execution.setCompletedAt(LocalDateTime.now());
            executionRepository.save(execution);
            throw e;
        }
    }

    /**
     * Create or update a workflow
     */
    public WorkflowDefinition saveWorkflow(WorkflowDefinition workflow) {
        // Map from JSON format if needed
        workflow = workflowMapper.mapFromJsonFormat(workflow);

        if (workflow.getId() == null) {
            workflow.setCreatedAt(LocalDateTime.now());
        }
        workflow.setUpdatedAt(LocalDateTime.now());
        WorkflowDefinition saved = workflowRepository.save(workflow);

        // Update Scheduler
        workflowScheduler.scheduleWorkflow(saved);
        return saved;
    }

    /**
     * Get workflow by ID
     */
    public WorkflowDefinition getWorkflow(String id) {
        return workflowRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found: " + id));
    }

    /**
     * Get all active workflows
     */
    public List<WorkflowDefinition> getAllWorkflows() {
        return workflowRepository.findByActiveTrue();
    }

    /**
     * Delete workflow (soft delete)
     */
    public void deleteWorkflow(String id) {
        WorkflowDefinition workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found: " + id));
        workflow.setActive(false);
        workflowRepository.save(workflow);
        workflowScheduler.unscheduleWorkflow(id);
    }

    /**
     * Get execution history for a workflow
     */
    public List<WorkflowExecution> getExecutionHistory(String workflowId) {
        return executionRepository.findByWorkflowIdOrderByStartedAtDesc(workflowId);
    }

}
