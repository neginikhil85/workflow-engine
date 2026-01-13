package dev.base.workflow.domain.engine;

import dev.base.workflow.domain.core.plugin.Plugin;
import dev.base.workflow.model.core.ExecutionContext;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import dev.base.workflow.model.nodetype.NodeType;

import static dev.base.workflow.constant.WorkflowConstants.DEFAULT_VERSION;
import static dev.base.workflow.constant.WorkflowConstants.MSG_EXECUTOR_FOR;

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
                return DEFAULT_VERSION;
            }

            @Override
            public String getDescription() {
                return MSG_EXECUTOR_FOR + getSupportedNodeType().getName();
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
    NodeExecutionResult execute(NodeDefinition node, Object input,
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
