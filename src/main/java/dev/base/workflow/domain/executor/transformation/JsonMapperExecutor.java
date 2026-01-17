package dev.base.workflow.domain.executor.transformation;

import dev.base.workflow.domain.engine.NodeExecutor;
import dev.base.workflow.model.core.ExecutionContext;
import dev.base.workflow.model.nodetype.TransformationNodeType;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import org.springframework.stereotype.Component;

import java.util.Map;

import static dev.base.workflow.constant.WorkflowConstants.*;

@Component
public class JsonMapperExecutor implements NodeExecutor {

    @Override
    public TransformationNodeType getSupportedNodeType() {
        return TransformationNodeType.JSON_MAPPER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NodeExecutionResult execute(NodeDefinition node, Object input,
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
                return NodeExecutionResult.success(node.getId(), result);
            }
        }

        // Fallback: If no config, just return input
        return NodeExecutionResult.success(node.getId(), input);
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        return Map.of(
                CFG_MAPPING, Map.of(DEFAULT_FIELD_NEW, DEFAULT_FIELD_OLD));
    }
}
