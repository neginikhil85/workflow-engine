package dev.base.workflow.controller;

import dev.base.workflow.model.dto.request.activemq.ActiveMQConnectionRequest;
import dev.base.workflow.model.dto.request.activemq.ActiveMQConnectionResponse;
import dev.base.workflow.model.dto.response.common.ApiResponse;
import dev.base.workflow.service.integration.ActiveMQAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API controller for ActiveMQ admin operations.
 * Used by UI to test connections and list destinations.
 */
@RestController
@RequestMapping("/api/v1/activemq")
@RequiredArgsConstructor
@Slf4j
public class ActiveMQController {

    private final ActiveMQAdminService activeMQAdminService;

    /**
     * Test connection to ActiveMQ broker.
     */
    @PostMapping("/test-connection")
    public ApiResponse<ActiveMQConnectionResponse> testConnection(@RequestBody ActiveMQConnectionRequest request) {
        return ApiResponse.success(activeMQAdminService.testConnection(request));
    }

    /**
     * List all queues and topics in ActiveMQ broker.
     */
    @PostMapping("/destinations")
    public ApiResponse<List<String>> listDestinations(@RequestBody ActiveMQConnectionRequest request) {
        return ApiResponse.success(activeMQAdminService.listDestinations(request));
    }
}
