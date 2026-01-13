package dev.base.workflow.model.nodetype;

import dev.base.workflow.model.stage.Stages;

public enum TriggerNodeType implements NodeType {
    WEBHOOK,
    CRON,
    FILE_CHANGE;

    @Override
    public Stages getStage() {
        return Stages.TRIGGER;
    }

    @Override
    public String getName() {
        return name();
    }
}
