package dev.base.workflow.controller;

import dev.base.workflow.model.dto.request.kafka.KafkaConnectionRequest;
import dev.base.workflow.model.dto.request.kafka.KafkaConnectionResponse;
import dev.base.workflow.model.dto.request.kafka.KafkaTopicRequest;
import dev.base.workflow.model.dto.request.kafka.KafkaTopicResponse;
import dev.base.workflow.model.dto.response.common.ApiResponse;
import dev.base.workflow.service.integration.KafkaAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * REST API controller for Kafka admin operations.
 * Used by UI to test connections, list topics, and create topics.
 */
@RestController
@RequestMapping("/api/v1/kafka")
@RequiredArgsConstructor
@Slf4j
public class KafkaController {

    private final KafkaAdminService kafkaAdminService;

    /**
     * Test connection to Kafka cluster.
     */
    @PostMapping("/test-connection")
    public ApiResponse<KafkaConnectionResponse> testConnection(@RequestBody KafkaConnectionRequest request) {
        return ApiResponse.success(kafkaAdminService.testConnection(request));
    }

    /**
     * List all topics in Kafka cluster.
     */
    @PostMapping("/topics")
    public ApiResponse<Set<String>> listTopics(@RequestBody KafkaConnectionRequest request) {
        return ApiResponse.success(kafkaAdminService.listTopics(request));
    }

    /**
     * Create a new topic in Kafka cluster.
     */
    @PostMapping("/topics/create")
    public ApiResponse<KafkaTopicResponse> createTopic(@RequestBody KafkaTopicRequest request) {
        return ApiResponse.success(kafkaAdminService.createTopic(request));
    }
}
