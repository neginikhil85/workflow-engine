package dev.base.workflow.model.dto.request.kafka;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KafkaTopicResponse {
    private boolean success;
    private String topicName;
    private int partitions;
    private short replicationFactor;
    private String error;
}
