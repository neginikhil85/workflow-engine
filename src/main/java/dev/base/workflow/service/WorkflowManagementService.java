package dev.base.workflow.service;

import dev.base.workflow.mongo.collection.WorkflowDefinition;
import dev.base.workflow.mongo.repository.WorkflowDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import dev.base.workflow.exception.WorkflowNotFoundException;

/**
 * Handles workflow persistence operations.
 * Responsibilities: Save, Load, Delete (soft), List workflows.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowManagementService {

    private final WorkflowDefinitionRepository workflowRepository;
    private final WorkflowDefinitionMapper workflowMapper;
    private final WorkflowScheduler workflowScheduler;

    /**
     * Create or update a workflow
     */
    public WorkflowDefinition saveWorkflow(WorkflowDefinition workflow) {
        workflow = workflowMapper.mapFromJsonFormat(workflow);

        if (workflow.getId() == null) {
            workflow.setCreatedAt(LocalDateTime.now());
        }
        workflow.setUpdatedAt(LocalDateTime.now());
        return workflowRepository.save(workflow);
    }

    /**
     * Get workflow by ID
     */
    public WorkflowDefinition getWorkflow(String id) {
        return workflowRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new WorkflowNotFoundException(id));
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
                .orElseThrow(() -> new WorkflowNotFoundException(id));
        workflow.setActive(false);
        workflowRepository.save(workflow);
        workflowScheduler.unscheduleWorkflow(id);
    }
}
