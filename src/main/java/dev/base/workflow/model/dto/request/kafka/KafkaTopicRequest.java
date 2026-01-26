package dev.base.workflow.model.dto.request.kafka;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class KafkaTopicRequest extends KafkaConnectionRequest {
    private String topicName;
    private int partitions = 1;
    private short replicationFactor = 1;
}
