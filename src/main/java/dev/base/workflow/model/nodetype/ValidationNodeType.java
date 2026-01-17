package dev.base.workflow.model.nodetype;

import dev.base.workflow.model.stage.Stages;

public enum ValidationNodeType implements NodeType {
    SCHEMA_CHECK("ValidationNodeType_SCHEMA_CHECK"), // e.g., JSON / XML schema validation
    BUSINESS_RULE("ValidationNodeType_BUSINESS_RULE"), // custom business rules
    REQUIRED_FIELDS("ValidationNodeType_REQUIRED_FIELDS"); // check mandatory fields

    private final String type;

    ValidationNodeType(String type) {
        this.type = type;
    }

    @Override
    public Stages getStage() {
        return Stages.VALIDATION;
    }

    @Override
    public String getName() {
        return type;
    }
}
