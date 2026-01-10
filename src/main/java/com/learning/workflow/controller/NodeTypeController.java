package com.learning.workflow.controller;

import com.learning.workflow.core.adapter.IntegrationAdapterRegistry;
import com.learning.workflow.core.adapter.MessagingAdapter;
import com.learning.workflow.dto.ApiResponse;
import com.learning.workflow.engine.NodeTypeRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API controller for node types and adapters metadata.
 * Used by UI to discover available node types and their configurations.
 */
@RestController
@RequestMapping("/api/nodes")
@RequiredArgsConstructor
public class NodeTypeController {

    private final NodeTypeRegistry nodeTypeRegistry;
    private final IntegrationAdapterRegistry adapterRegistry;

    @GetMapping("/types")
    public ApiResponse<Map<String, Map<String, Object>>> getNodeTypes() {
        Map<String, Map<String, Object>> nodeTypes = nodeTypeRegistry.getAll().values().stream()
                .collect(Collectors.toMap(
                        executor -> executor.getSupportedNodeType().getName(),
                        executor -> {
                            Map<String, Object> metadata = new HashMap<>();
                            metadata.put("type", executor.getSupportedNodeType().getClass().getSimpleName());
                            metadata.put("name", executor.getMetadata().getName());
                            metadata.put("description", executor.getMetadata().getDescription());
                            metadata.put("category", executor.getMetadata().getCategory());
                            metadata.put("defaultConfig", executor.getDefaultConfig());
                            return metadata;
                        }));

        return ApiResponse.success(nodeTypes);
    }

    @GetMapping("/adapters")
    public ApiResponse<Map<String, Object>> getAdapters() {
        Map<String, Object> adapters = new HashMap<>();

        adapterRegistry.getAllAdapters().forEach((id, adapter) -> {
            Map<String, Object> adapterInfo = new HashMap<>();
            adapterInfo.put("id", adapter.getAdapterId());
            adapterInfo.put("name", adapter.getName());
            adapterInfo.put("type", adapter instanceof MessagingAdapter ? "messaging" : "integration");
            adapters.put(id, adapterInfo);
        });

        return ApiResponse.success(adapters);
    }
}
