package dev.base.workflow.controller;

import dev.base.workflow.model.dto.ApiResponse;
import dev.base.workflow.service.integration.KafkaAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

import static dev.base.workflow.constant.KafkaConstants.CFG_BOOTSTRAP_SERVERS;
import static dev.base.workflow.constant.WorkflowConstants.KEY_SUCCESS;
import static dev.base.workflow.constant.WorkflowConstants.KEY_ERROR;
import static dev.base.workflow.constant.KafkaConstants.CFG_TOPIC_NAME;
import static dev.base.workflow.constant.KafkaConstants.CFG_PARTITIONS;
import static dev.base.workflow.constant.KafkaConstants.CFG_REPLICATION_FACTOR;

/**
 * REST API controller for Kafka admin operations.
 * Used by UI to test connections, list topics, and create topics.
 */
@RestController
@RequestMapping("/api/kafka")
@RequiredArgsConstructor
@Slf4j
public class KafkaController {

    private final KafkaAdminService kafkaAdminService;

    /**
     * Test connection to Kafka cluster.
     */
    @PostMapping("/test-connection")
    public ApiResponse<Map<String, Object>> testConnection(@RequestBody Map<String, Object> config) {
        log.info("Testing Kafka connection to: {}", config.get(CFG_BOOTSTRAP_SERVERS));
        Map<String, Object> result = kafkaAdminService.testConnection(config);

        if (Boolean.TRUE.equals(result.get(KEY_SUCCESS))) {
            return ApiResponse.success(result, "Connection successful");
        } else {
            return ApiResponse.error((String) result.get(KEY_ERROR));
        }
    }

    /**
     * List all topics in Kafka cluster.
     */
    @PostMapping("/topics")
    public ApiResponse<Set<String>> listTopics(@RequestBody Map<String, Object> config) {
        log.info("Listing Kafka topics from: {}", config.get(CFG_BOOTSTRAP_SERVERS));
        Set<String> topics = kafkaAdminService.listTopics(config);
        return ApiResponse.success(topics);
    }

    /**
     * Create a new topic in Kafka cluster.
     */
    @PostMapping("/topics/create")
    public ApiResponse<Map<String, Object>> createTopic(@RequestBody Map<String, Object> request) {
        String topicName = (String) request.get(CFG_TOPIC_NAME);
        int partitions = request.containsKey(CFG_PARTITIONS)
                ? ((Number) request.get(CFG_PARTITIONS)).intValue()
                : 1;
        short replicationFactor = request.containsKey(CFG_REPLICATION_FACTOR)
                ? ((Number) request.get(CFG_REPLICATION_FACTOR)).shortValue()
                : 1;

        log.info("Creating Kafka topic: {} with {} partitions", topicName, partitions);
        Map<String, Object> result = kafkaAdminService.createTopic(request, topicName, partitions, replicationFactor);

        if (Boolean.TRUE.equals(result.get(KEY_SUCCESS))) {
            return ApiResponse.success(result, "Topic created successfully");
        } else {
            return ApiResponse.error((String) result.get(KEY_ERROR));
        }
    }
}
