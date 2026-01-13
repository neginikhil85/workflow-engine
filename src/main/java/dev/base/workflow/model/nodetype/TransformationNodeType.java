package dev.base.workflow.model.nodetype;

import dev.base.workflow.model.stage.Stages;

public enum TransformationNodeType implements NodeType {
    JSON_MAPPER,
    EXPRESSION;

    @Override
    public Stages getStage() {
        return Stages.TRANSFORMATION;
    }

    @Override
    public String getName() {
        return name();
    }
}
