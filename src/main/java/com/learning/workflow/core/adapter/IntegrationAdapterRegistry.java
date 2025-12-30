package com.learning.workflow.core.adapter;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for all integration adapters.
 * Allows dynamic registration and lookup of adapters by ID.
 */
@Component
public class IntegrationAdapterRegistry {
    
    private final Map<String, IntegrationAdapter> adapters = new HashMap<>();
    
    public IntegrationAdapterRegistry(List<IntegrationAdapter> adapterList) {
        for (IntegrationAdapter adapter : adapterList) {
            adapters.put(adapter.getAdapterId(), adapter);
        }
    }
    
    /**
     * Get adapter by ID
     */
    public Optional<IntegrationAdapter> getAdapter(String adapterId) {
        return Optional.ofNullable(adapters.get(adapterId));
    }
    
    /**
     * Get all registered adapters
     */
    public Map<String, IntegrationAdapter> getAllAdapters() {
        return Map.copyOf(adapters);
    }
    
    /**
     * Get all messaging adapters
     */
    public Map<String, MessagingAdapter> getMessagingAdapters() {
        Map<String, MessagingAdapter> messagingAdapters = new HashMap<>();
        adapters.values().stream()
                .filter(adapter -> adapter instanceof MessagingAdapter)
                .forEach(adapter -> messagingAdapters.put(adapter.getAdapterId(), (MessagingAdapter) adapter));
        return messagingAdapters;
    }
    
    /**
     * Register a new adapter dynamically (useful for plugin system)
     */
    public void registerAdapter(IntegrationAdapter adapter) {
        adapters.put(adapter.getAdapterId(), adapter);
    }
}

