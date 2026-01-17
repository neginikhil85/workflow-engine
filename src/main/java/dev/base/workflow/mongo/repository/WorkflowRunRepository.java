package dev.base.workflow.mongo.repository;

import dev.base.workflow.mongo.collection.WorkflowRun;
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
     * Safely find the first active run (avoids NonUniqueResultException).
     */
    Optional<WorkflowRun> findFirstByWorkflowIdAndStatus(String workflowId, WorkflowRun.RunStatus status);

    /**
     * Find all runs with a specific status (useful for cleaning up duplicates).
     */
    List<WorkflowRun> findAllByWorkflowIdAndStatus(String workflowId, WorkflowRun.RunStatus status);

    /**
     * Find all active runs (for dashboard monitoring).
     */
    List<WorkflowRun> findByStatus(WorkflowRun.RunStatus status);
}
