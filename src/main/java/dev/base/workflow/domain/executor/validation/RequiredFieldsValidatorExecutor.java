package dev.base.workflow.domain.executor.validation;

import dev.base.workflow.domain.engine.NodeExecutor;
import dev.base.workflow.model.core.ExecutionContext;
import dev.base.workflow.model.nodetype.ValidationNodeType;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import dev.base.workflow.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static dev.base.workflow.constant.WorkflowConstants.*;
import static dev.base.workflow.constant.WorkflowErrorConstants.ERR_VALIDATION_MISSING_FMT;
import static dev.base.workflow.constant.WorkflowErrorConstants.ERR_VALIDATION_NOT_MAP;

@Component
public class RequiredFieldsValidatorExecutor implements NodeExecutor {

    @Override
    public ValidationNodeType getSupportedNodeType() {
        return ValidationNodeType.REQUIRED_FIELDS;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NodeExecutionResult execute(NodeDefinition node, Object input,
            ExecutionContext ctx) {
        if (!(input instanceof Map)) {
            throw new RuntimeException(ERR_VALIDATION_NOT_MAP);
        }

        Map<String, Object> map = (Map<String, Object>) input;
        Map<String, Object> config = node.getConfig();

        // Get required fields from config, or use defaults
        List<String> requiredFields;
        if (config != null && config.containsKey(CFG_REQUIRED_FIELDS)) {
            requiredFields = (List<String>) config.get(CFG_REQUIRED_FIELDS);
        } else {
            // Fallback required fields from WorkflowConstants
            requiredFields = List.of(FIELD_ID, FIELD_NAME);
        }

        // Validate
        for (String field : requiredFields) {
            if (!map.containsKey(field)) {
                throw new RuntimeException(StringUtils.format(ERR_VALIDATION_MISSING_FMT, field));
            }
        }

        return NodeExecutionResult.success(node.getId(), input);
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        return Map.of(CFG_REQUIRED_FIELDS, List.of(FIELD_ID, FIELD_NAME));
    }
}
