package dev.base.workflow.model.dto;

import dev.base.workflow.model.core.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStatusResponse {
    private boolean isRunning;
    private ExecutionStatus status;
}
