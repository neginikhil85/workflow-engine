package com.learning.workflow.executor.transformation;

import com.learning.workflow.model.core.ExecutionContext;
import com.learning.workflow.engine.NodeExecutor;
import com.learning.workflow.model.core.NodeDefinition;
import com.learning.workflow.model.nodetype.TransformationNodeType;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.learning.workflow.constant.WorkflowConstants.CFG_MAPPING;

@Component
public class JsonMapperExecutor implements NodeExecutor {

    @Override
    public TransformationNodeType getSupportedNodeType() {
        return TransformationNodeType.JSON_MAPPER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public com.learning.workflow.model.core.NodeExecutionResult execute(NodeDefinition node, Object input,
            ExecutionContext ctx) {
        if (input instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) input;
            Map<String, Object> config = node.getConfig();
            if (config != null && config.containsKey(CFG_MAPPING)) {
                Map<String, String> mapping = (Map<String, String>) config.get(CFG_MAPPING);
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

        // Fallback: If no config, just return input
        return com.learning.workflow.model.core.NodeExecutionResult.success(node.getId(), input);
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        return Map.of(
                CFG_MAPPING, Map.of("newField", "oldField"));
    }
}
