package com.learning.workflow.executor.transformation;

import com.learning.workflow.model.core.ExecutionContext;
import com.learning.workflow.engine.NodeExecutor;
import com.learning.workflow.model.core.NodeDefinition;
import com.learning.workflow.model.nodetype.TransformationNodeType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JsonMapperExecutor implements NodeExecutor {

    @Override
    public TransformationNodeType getSupportedNodeType() {
        return TransformationNodeType.JSON_MAPPER;
    }

    @Override
    public com.learning.workflow.model.core.NodeExecutionResult execute(NodeDefinition node, Object input,
            ExecutionContext ctx) {
        // ... (existing logic for custom mapping) ...
        if (input instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) input;
            Map<String, Object> config = node.getConfig();
            if (config != null && config.containsKey("mapping")) {
                Map<String, String> mapping = (Map<String, String>) config.get("mapping");
                Map<String, Object> result = new java.util.HashMap<>();
                for (Map.Entry<String, String> entry : mapping.entrySet()) {
                    String targetKey = entry.getKey();
                    String sourceKey = entry.getValue();
                    if (map.containsKey(sourceKey)) {
                        result.put(targetKey, map.get(sourceKey));
                    }
                }
                return com.learning.workflow.model.core.NodeExecutionResult.success(node.getId(), result);
            }
        }

        // Fallback: If no config, just return input or convert to Map
        // Removing Camel route logic.
        return com.learning.workflow.model.core.NodeExecutionResult.success(node.getId(), input);
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        return Map.of(
                "mapping", Map.of(
                        "newField", "oldField"));
    }
}
