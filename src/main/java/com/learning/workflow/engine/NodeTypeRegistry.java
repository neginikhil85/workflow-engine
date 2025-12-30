package com.learning.workflow.engine;

import com.learning.workflow.model.nodetype.NodeType;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Maps NodeType → NodeExecutor dynamically at runtime
 */
@Component
public class NodeTypeRegistry {

    private final Map<String, NodeExecutor> registry = new HashMap<>();
    private static final String ERR_NO_EXECUTOR = "No executor registered for nodeType: ";

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
        return type.getClass().getSimpleName() + "_" + type.getName();
    }
}
