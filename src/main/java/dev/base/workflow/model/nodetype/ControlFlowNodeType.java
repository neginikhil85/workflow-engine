package dev.base.workflow.model.nodetype;

import dev.base.workflow.model.stage.Stages;

public enum ControlFlowNodeType implements NodeType {
    IF,
    SWITCH,
    LOOP,
    DELAY;

    @Override
    public Stages getStage() {
        return Stages.CONTROL_FLOW;
    }

    @Override
    public String getName() {
        return name();
    }
}
