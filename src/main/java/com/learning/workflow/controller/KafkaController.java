package com.learning.workflow.controller;

import com.learning.workflow.service.KafkaAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
     * 
     * Request body:
     * {
     * "bootstrapServers": "localhost:9092",
     * "securityProtocol": "PLAINTEXT",
     * // Optional SSL/SASL configs
     * }
     */
    @PostMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection(@RequestBody Map<String, Object> config) {
        log.info("Testing Kafka connection to: {}", config.get("bootstrapServers"));
        Map<String, Object> result = kafkaAdminService.testConnection(config);

        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * List all topics in Kafka cluster.
     * 
     * Request body: same config as test-connection
     */
    @PostMapping("/topics")
    public ResponseEntity<Map<String, Object>> listTopics(@RequestBody Map<String, Object> config) {
        log.info("Listing Kafka topics from: {}", config.get("bootstrapServers"));
        try {
            Set<String> topics = kafkaAdminService.listTopics(config);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "topics", topics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    /**
     * Create a new topic in Kafka cluster.
     * 
     * Request body:
     * {
     * "bootstrapServers": "localhost:9092",
     * "securityProtocol": "PLAINTEXT",
     * "topicName": "my-new-topic",
     * "partitions": 3,
     * "replicationFactor": 1
     * }
     */
    @PostMapping("/topics/create")
    public ResponseEntity<Map<String, Object>> createTopic(@RequestBody Map<String, Object> request) {
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
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}
