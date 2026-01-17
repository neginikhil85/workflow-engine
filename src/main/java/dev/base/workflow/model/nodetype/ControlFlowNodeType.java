package dev.base.workflow.model.nodetype;

import dev.base.workflow.model.stage.Stages;

public enum ControlFlowNodeType implements NodeType {
    IF("ControlFlowNodeType_IF"),
    SWITCH("ControlFlowNodeType_SWITCH"),
    LOOP("ControlFlowNodeType_LOOP"),
    DELAY("ControlFlowNodeType_DELAY");

    private final String type;

    ControlFlowNodeType(String type) {
        this.type = type;
    }

    @Override
    public Stages getStage() {
        return Stages.CONTROL_FLOW;
    }

    @Override
    public String getName() {
        return type;
    }
}
