package dev.base.workflow.model.node.details;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConsoleExecutionDetails {
    private String message;
    private String level; // INFO, WARN, ERROR
    private LocalDateTime timestamp;
}
