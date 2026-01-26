package dev.base.workflow.model.dto.request.kafka;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class KafkaConnectionResponse {
    private boolean success;
    private String clusterId;
    private List<String> brokers;
    private int brokerCount;
    private String error;
}
