package dev.base.workflow.model.nodetype;

import dev.base.workflow.model.stage.Stages;

public enum NotificationNodeType implements NodeType {
    EMAIL,
    LOG,
    CONSOLE;

    @Override
    public Stages getStage() {
        return Stages.NOTIFICATION;
    }

    @Override
    public String getName() {
        return name();
    }
}