package dev.base.workflow.model.node.details;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CronExecutionDetails {
    private String expression;
    private LocalDateTime triggeredAt;
    private String scheduledTime;
}
