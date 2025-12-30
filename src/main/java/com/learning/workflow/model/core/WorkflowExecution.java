package com.learning.workflow.model.core;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Represents a single execution of a workflow.
 * Tracks execution status, timing, and results.
 */
@Data
@Document(collection = "workflow_executions")
public class WorkflowExecution {

    @Id
    private String id;

    private String workflowId;
    private String status; // RUNNING, COMPLETED, FAILED
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Object result;
    private String error;
    private Object input;
    private java.util.List<String> executedNodes;
}
