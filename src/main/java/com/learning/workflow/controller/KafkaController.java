package com.learning.workflow.controller;

import com.learning.workflow.dto.ApiResponse;
import com.learning.workflow.service.KafkaAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

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
        log.info("Testing Kafka connection to: {}", config.get("bootstrapServers"));
        Map<String, Object> result = kafkaAdminService.testConnection(config);

        if (Boolean.TRUE.equals(result.get("success"))) {
            return ApiResponse.success(result, "Connection successful");
        } else {
            return ApiResponse.error((String) result.get("error"));
        }
    }

    /**
     * List all topics in Kafka cluster.
     */
    @PostMapping("/topics")
    public ApiResponse<Set<String>> listTopics(@RequestBody Map<String, Object> config) {
        log.info("Listing Kafka topics from: {}", config.get("bootstrapServers"));
        Set<String> topics = kafkaAdminService.listTopics(config);
        return ApiResponse.success(topics);
    }

    /**
     * Create a new topic in Kafka cluster.
     */
    @PostMapping("/topics/create")
    public ApiResponse<Map<String, Object>> createTopic(@RequestBody Map<String, Object> request) {
        String topicName = (String) request.get("topicName");
        int partitions = request.containsKey("partitions")
                ? ((Number) request.get("partitions")).intValue()
                : 1;
        short replicationFactor = request.containsKey("replicationFactor")
                ? ((Number) request.get("replicationFactor")).shortValue()
                : 1;

        log.info("Creating Kafka topic: {} with {} partitions", topicName, partitions);
        Map<String, Object> result = kafkaAdminService.createTopic(request, topicName, partitions, replicationFactor);

        if (Boolean.TRUE.equals(result.get("success"))) {
            return ApiResponse.success(result, "Topic created successfully");
        } else {
            return ApiResponse.error((String) result.get("error"));
        }
    }
}
