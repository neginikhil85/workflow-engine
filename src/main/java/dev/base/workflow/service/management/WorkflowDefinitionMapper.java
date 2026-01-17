package dev.base.workflow.service.management;

import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.WorkflowDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
            workflow.getNodes().forEach(this::processNodeConfiguration);
        }
        return workflow;
    }

    private void processNodeConfiguration(NodeDefinition node) {
        if (node.getNodeType() != null || node.getConfig() == null) {
            return;
        }

        Object nodeTypeObj = node.getConfig().get(CFG_NODE_TYPE);
        if (nodeTypeObj instanceof String) {
            node.setNodeType((String) nodeTypeObj);
            node.getConfig().remove(CFG_NODE_TYPE);
        }
    }
}
