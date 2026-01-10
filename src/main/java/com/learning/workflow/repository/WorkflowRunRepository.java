package com.learning.workflow.repository;

import com.learning.workflow.model.core.WorkflowRun;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowRunRepository extends MongoRepository<WorkflowRun, String> {

    /**
     * Find all runs for a given workflow, ordered by start time descending.
     */
    List<WorkflowRun> findByWorkflowIdOrderByStartTimeDesc(String workflowId);

    /**
     * Find the currently active run for a workflow (should be at most one).
     */
    Optional<WorkflowRun> findByWorkflowIdAndStatus(String workflowId, WorkflowRun.RunStatus status);

    /**
     * Find all active runs (for dashboard monitoring).
     */
    List<WorkflowRun> findByStatus(WorkflowRun.RunStatus status);
}
