package dev.base.workflow.model.dto.request.activemq;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActiveMQConnectionResponse {
    private boolean success;
    private String providerVersion;
    private String error;
}
