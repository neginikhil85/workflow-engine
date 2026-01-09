package com.learning.workflow.repository;

import com.learning.workflow.model.core.WorkflowExecution;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowExecutionRepository extends MongoRepository<WorkflowExecution, String> {

    List<WorkflowExecution> findByWorkflowIdOrderByStartedAtDesc(String workflowId);

    List<WorkflowExecution> findByStatus(String status);

    List<WorkflowExecution> findByWorkflowIdAndStatus(String workflowId, String status);
}
