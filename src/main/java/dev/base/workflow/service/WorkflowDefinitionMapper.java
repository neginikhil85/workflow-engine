package dev.base.workflow.service;

import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.WorkflowDefinition;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import static dev.base.workflow.constant.WorkflowConstants.CFG_NODE_TYPE;

/**
 * Maps workflow definitions from JSON/UI format to internal format.
 */
@Component
@RequiredArgsConstructor
public class WorkflowDefinitionMapper {

    /**
     * Map workflow definition from UI/JSON format where nodeType is a string
     * to internal format where nodeType is an enum.
     */
    public WorkflowDefinition mapFromJsonFormat(WorkflowDefinition workflow) {
        if (workflow.getNodes() != null) {
            for (NodeDefinition node : workflow.getNodes()) {
                // If nodeType is stored as string (from JSON), convert to enum
                if (node.getNodeType() == null && node.getConfig() != null) {
                    Object nodeTypeObj = node.getConfig().get(CFG_NODE_TYPE);
                    if (nodeTypeObj instanceof String) {
                        node.setNodeType((String) nodeTypeObj);
                        // Remove from config as it's now a proper field
                        Map<String, Object> config = node.getConfig();
                        config.remove(CFG_NODE_TYPE);
                    }
                }
            }
        }
        return workflow;
    }
}
