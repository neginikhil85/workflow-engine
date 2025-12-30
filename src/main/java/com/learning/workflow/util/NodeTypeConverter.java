package com.learning.workflow.util;

import com.learning.workflow.model.nodetype.*;
import org.springframework.stereotype.Component;

/**
 * Utility to convert node type strings to enum instances.
 * Used when loading workflows from JSON/UI.
 */
@Component
public class NodeTypeConverter {
    
    public NodeType convertFromString(String nodeTypeString) {
        if (nodeTypeString == null || nodeTypeString.isEmpty()) {
            throw new IllegalArgumentException("Node type string cannot be empty");
        }
        
        String[] parts = nodeTypeString.split("_", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid node type format: " + nodeTypeString + ". Expected: TYPE_NAME");
        }
        
        String category = parts[0];
        String name = parts[1];
        
        try {
            switch (category.toUpperCase()) {
                case "TRIGGER":
                    return TriggerNodeType.valueOf(name);
                case "INTEGRATION":
                    return IntegrationNodeType.valueOf(name);
                case "VALIDATION":
                    return ValidationNodeType.valueOf(name);
                case "TRANSFORMATION":
                    return TransformationNodeType.valueOf(name);
                case "NOTIFICATION":
                    return NotificationNodeType.valueOf(name);
                case "CONTROLFLOW":
                case "CONTROL_FLOW":
                    return ControlFlowNodeType.valueOf(name);
                default:
                    throw new IllegalArgumentException("Unknown node type category: " + category);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid node type: " + nodeTypeString, e);
        }
    }
}

