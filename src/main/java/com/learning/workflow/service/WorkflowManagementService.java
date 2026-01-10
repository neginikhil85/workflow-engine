package com.learning.workflow.service;

import com.learning.workflow.model.core.WorkflowDefinition;
import com.learning.workflow.repository.WorkflowDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.learning.workflow.constant.WorkflowConstants.*;

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
                .orElseThrow(() -> new RuntimeException(ERR_WORKFLOW_NOT_FOUND + id));
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
                .orElseThrow(() -> new RuntimeException(ERR_WORKFLOW_NOT_FOUND + id));
        workflow.setActive(false);
        workflowRepository.save(workflow);
        workflowScheduler.unscheduleWorkflow(id);
    }
}
