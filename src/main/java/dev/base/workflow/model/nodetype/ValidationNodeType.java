package dev.base.workflow.model.nodetype;

import dev.base.workflow.model.stage.Stages;

public enum ValidationNodeType implements NodeType {
    SCHEMA_CHECK,       // e.g., JSON / XML schema validation
    BUSINESS_RULE,      // custom business rules
    REQUIRED_FIELDS;    // check mandatory fields

    @Override
    public Stages getStage() {
        return Stages.VALIDATION;
    }

    @Override
    public String getName() {
        return name();
    }
}
