package dev.base.workflow.model.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared state across the workflow execution.
 */
public class ExecutionContext {

    private final Map<String, Object> context = new ConcurrentHashMap<>();

    public void put(String key, Object value) {
        context.put(key, value);
    }

    public Object get(String key) {
        return context.get(key);
    }

    public Map<String, Object> getAll() {
        return context;
    }
}

