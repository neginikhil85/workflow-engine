package com.learning.workflow.core.plugin;

import com.learning.workflow.model.nodetype.NodeType;

/**
 * Base interface for all workflow plugins.
 * Allows extensible plugin architecture for future integrations.
 */
public interface Plugin {
    
    /**
     * Returns the node type this plugin supports
     */
    NodeType getSupportedNodeType();
    
    /**
     * Plugin metadata
     */
    PluginMetadata getMetadata();
    
    /**
     * Plugin metadata information
     */
    interface PluginMetadata {
        String getName();
        String getVersion();
        String getDescription();
        String getCategory();
    }
}

