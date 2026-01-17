package dev.base.workflow.model.nodetype;

import dev.base.workflow.model.stage.Stages;

public enum TriggerNodeType implements NodeType {
    WEBHOOK("TriggerNodeType_WEBHOOK"),
    CRON("TriggerNodeType_CRON"),
    FILE_CHANGE("TriggerNodeType_FILE_CHANGE");

    private final String type;

    TriggerNodeType(String type) {
        this.type = type;
    }

    @Override
    public Stages getStage() {
        return Stages.TRIGGER;
    }

    @Override
    public String getName() {
        return type;
    }
}
