package com.learning.workflow.model.core;

/**
 * Enum representing the status of a workflow execution.
 */
public enum ExecutionStatus {

    /**
     * Workflow is currently being executed
     */
    RUNNING,

    /**
     * Workflow execution completed successfully
     */
    COMPLETED,

    /**
     * Workflow execution failed with an error
     */
    FAILED,

    /**
     * Workflow execution was cancelled/stopped by user
     */
    CANCELLED,

    /**
     * Workflow is scheduled (cron job) and waiting for trigger
     */
    SCHEDULED,

    /**
     * Workflow is idle (saved but not running/scheduled)
     */
    IDLE
}
