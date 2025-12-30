package com.learning.workflow.engine;

import com.learning.workflow.core.plugin.Plugin;
import com.learning.workflow.model.core.ExecutionContext;
import com.learning.workflow.model.core.NodeDefinition;
import com.learning.workflow.model.nodetype.NodeType;

/**
 * Core interface for node execution.
 * Extends Plugin to support plugin architecture.
 * Every node executor implements this interface.
 */
public interface NodeExecutor extends Plugin {

    /**
     * Returns which NodeType this executor handles.
     * Must be implemented by concrete executors.
     */
    NodeType getSupportedNodeType();

    /**
     * Plugin metadata - provides information about the executor
     */
    @Override
    default Plugin.PluginMetadata getMetadata() {
        return new Plugin.PluginMetadata() {
            @Override
            public String getName() {
                return getSupportedNodeType().getName();
            }

            @Override
            public String getVersion() {
                return "1.0.0";
            }

            @Override
            public String getDescription() {
                return "Node executor for " + getSupportedNodeType().getName();
            }

            @Override
            public String getCategory() {
                return getSupportedNodeType().getClass().getSimpleName();
            }
        };
    }

    /**
     * Execute the node with given input and context
     * 
     * @param node  Node definition containing configuration
     * @param input Input data from previous node or initial input
     * @param ctx   Execution context for shared state
     * @return Execution result containing output data and flow control
     */
    com.learning.workflow.model.core.NodeExecutionResult execute(NodeDefinition node, Object input,
            ExecutionContext ctx);

    /**
     * Validate node configuration before execution
     * 
     * @param node Node definition to validate
     * @throws IllegalArgumentException if configuration is invalid
     */
    default void validate(NodeDefinition node) {
        // Default: no validation, can be overridden
    }

    /**
     * Get default configuration template for this node type
     * Useful for UI to show available configuration options
     * 
     * @return Default configuration map
     */
    default java.util.Map<String, Object> getDefaultConfig() {
        return java.util.Collections.emptyMap();
    }
}
