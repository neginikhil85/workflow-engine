package com.learning.workflow.model.nodetype;

import com.learning.workflow.model.stage.Stages;

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
