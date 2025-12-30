package com.learning.workflow.executor.validation;

import com.learning.workflow.engine.NodeExecutor;
import com.learning.workflow.model.core.ExecutionContext;
import com.learning.workflow.model.core.NodeDefinition;
import com.learning.workflow.model.nodetype.ValidationNodeType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class RequiredFieldsValidatorExecutor implements NodeExecutor {

    @Override
    public ValidationNodeType getSupportedNodeType() {
        return ValidationNodeType.REQUIRED_FIELDS;
    }

    @Override
    public com.learning.workflow.model.core.NodeExecutionResult execute(NodeDefinition node, Object input,
            ExecutionContext ctx) {
        if (!(input instanceof Map)) {
            throw new RuntimeException("Validation failed: input is not a Map");
        }

        Map<String, Object> map = (Map<String, Object>) input;
        Map<String, Object> config = node.getConfig();

        // Get required fields from config, or use defaults
        List<String> requiredFields;
        if (config != null && config.containsKey("requiredFields")) {
            requiredFields = (List<String>) config.get("requiredFields");
        } else {
            // Fallback required fields
            requiredFields = List.of(
                    ValidationConstants.FIELD_ID,
                    ValidationConstants.FIELD_NAME);
        }

        // Validate
        for (String field : requiredFields) {
            if (!map.containsKey(field)) {
                throw new RuntimeException("Validation failed: " + field + " missing");
            }
        }

        return com.learning.workflow.model.core.NodeExecutionResult.success(node.getId(), input);
    }

    @Override
    public Map<String, Object> getDefaultConfig() {
        return Map.of(
                "requiredFields", List.of("id", "name"));
    }
}
