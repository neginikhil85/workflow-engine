package com.learning.workflow.model.core;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

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

    @Indexed
    private String workflowId;

    /**
     * Links this execution to its parent WorkflowRun (session).
     * All executions from the same "Run" click share the same runId.
     */
    @Indexed
    private String runId;

    private ExecutionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Object result;
    private String error;
    private Object input;
    private java.util.List<String> executedNodes;
}
