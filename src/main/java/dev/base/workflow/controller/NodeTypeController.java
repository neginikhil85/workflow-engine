package dev.base.workflow.controller;

import dev.base.workflow.domain.core.adapter.IntegrationAdapterRegistry;
import dev.base.workflow.domain.core.adapter.MessagingAdapter;
import dev.base.workflow.model.dto.ApiResponse;
import dev.base.workflow.domain.engine.NodeTypeRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.base.workflow.model.dto.AdapterDTO;
import dev.base.workflow.model.dto.NodeTypeDTO;

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
    public ApiResponse<Map<String, NodeTypeDTO>> getNodeTypes() {
        Map<String, NodeTypeDTO> nodeTypes = nodeTypeRegistry.getAll().values().stream()
                .collect(Collectors.toMap(
                        executor -> executor.getSupportedNodeType().getName(),
                        executor -> NodeTypeDTO.builder()
                                .type(executor.getSupportedNodeType().getClass().getSimpleName())
                                .name(executor.getMetadata().getName())
                                .description(executor.getMetadata().getDescription())
                                .category(executor.getMetadata().getCategory())
                                .defaultConfig(executor.getDefaultConfig())
                                .build()));

        return ApiResponse.success(nodeTypes);
    }

    @GetMapping("/adapters")
    public ApiResponse<Map<String, AdapterDTO>> getAdapters() {
        Map<String, AdapterDTO> adapters = new HashMap<>();

        adapterRegistry.getAllAdapters().forEach((id, adapter) -> {
            adapters.put(id, AdapterDTO.builder()
                    .id(adapter.getAdapterId())
                    .name(adapter.getName())
                    .type(adapter instanceof MessagingAdapter ? "messaging" : "integration")
                    .build());
        });

        return ApiResponse.success(adapters);
    }
}
