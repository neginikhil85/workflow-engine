package dev.base.workflow.mongo.collection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Represents a "Session" or "Job Instance" for a workflow.
 * A WorkflowRun is created when a user clicks "Run" or a trigger starts.
 * Multiple WorkflowExecutions (ticks) can belong to a single Run.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "workflow_runs")
public class WorkflowRun {

    @Id
    private String id;

    @Indexed
    private String workflowId;

    /**
     * How this run was triggered.
     * Values: MANUAL, CRON, WEBHOOK, KAFKA
     */
    private TriggerType triggerType;

    /**
     * Current status of the run.
     */
    private RunStatus status;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Stats (Updated atomically using $inc)
    private long totalExecutions;
    private long failedExecutions;

    /**
     * Placeholder for V2: Heartbeat timestamp for orphan detection.
     * Will be used to detect crashed runs.
     */
    private LocalDateTime lastHeartbeat;

    // Enums defined here for simplicity (can be moved to separate files if needed)

    public enum TriggerType {
        MANUAL,
        CRON,
        WEBHOOK,
        KAFKA
    }

    public enum RunStatus {
        ACTIVE, // Currently running/scheduled
        COMPLETED, // Finished successfully (for one-shot flows)
        STOPPED, // Manually stopped by user
        FAILED, // Errored out
        INTERRUPTED // Server crash/unexpected termination (V2)
    }
}
