package dev.base.workflow.domain.engine;

import dev.base.workflow.model.nodetype.NodeType;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

import java.util.List;

import static dev.base.workflow.constant.WorkflowErrorConstants.ERR_NO_EXECUTOR;
import dev.base.workflow.util.StringUtils;

/**
 * Maps NodeType → NodeExecutor dynamically at runtime
 */
@Component
public class NodeTypeRegistry {

    private final Map<String, NodeExecutor> registry = new HashMap<>();

    public NodeTypeRegistry(List<NodeExecutor> executors) {
        for (NodeExecutor executor : executors) {
            String key = generateKey(executor.getSupportedNodeType());
            registry.put(key, executor);
        }
    }

    public NodeExecutor resolve(String nodeType) {
        NodeExecutor executor = registry.get(nodeType);
        return validateExecutor(nodeType, executor);
    }

    private NodeExecutor validateExecutor(String nodeType, NodeExecutor executor) {
        if (executor == null) {
            throw new RuntimeException(ERR_NO_EXECUTOR + nodeType);
        }
        return executor;
    }

    public Map<String, NodeExecutor> getAll() {
        return Map.copyOf(registry);
    }

    // Helper to generate unique key: e.g. "TriggerNodeType_WEBHOOK"
    // Or we can rely on frontend sending "TRIGGER_WEBHOOK" if we align names?
    // Let's use ClassName_EnumName but maybe simplify?
    // "TRIGGER_WEBHOOK" matches "TriggerNodeType_WEBHOOK" if we map correctly?
    // Actually, "TriggerNodeType" -> "TRIGGER"? No.
    // Let's stick to "TriggerNodeType_WEBHOOK" as the reliable key.
    public static String generateKey(NodeType type) {
        return StringUtils.concat(type.getClass().getSimpleName(), "_", type.getName());
    }
}
