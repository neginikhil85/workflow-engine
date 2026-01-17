package dev.base.workflow.model.nodetype;

import dev.base.workflow.model.stage.Stages;

public enum TransformationNodeType implements NodeType {
    JSON_MAPPER("TransformationNodeType_JSON_MAPPER"),
    EXPRESSION("TransformationNodeType_EXPRESSION");

    private final String type;

    TransformationNodeType(String type) {
        this.type = type;
    }

    @Override
    public Stages getStage() {
        return Stages.TRANSFORMATION;
    }

    @Override
    public String getName() {
        return type;
    }
}
