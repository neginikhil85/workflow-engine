package dev.base.workflow.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("isRunning")
    private boolean isRunning;
    private ExecutionStatus status;
}
