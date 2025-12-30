package com.learning.workflow.service;

import com.learning.workflow.model.core.NodeDefinition;
import com.learning.workflow.model.core.WorkflowDefinition;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

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
                    Object nodeTypeObj = node.getConfig().get("nodeType");
                    if (nodeTypeObj instanceof String) {
                        node.setNodeType((String) nodeTypeObj);
                        // Remove from config as it's now a proper field
                        Map<String, Object> config = node.getConfig();
                        config.remove("nodeType");
                    }
                }
            }
        }
        return workflow;
    }
}
