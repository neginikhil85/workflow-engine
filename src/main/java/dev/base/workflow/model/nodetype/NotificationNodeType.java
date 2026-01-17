package dev.base.workflow.model.nodetype;

import dev.base.workflow.model.stage.Stages;

public enum NotificationNodeType implements NodeType {
    EMAIL("NotificationNodeType_EMAIL"),
    LOG("NotificationNodeType_LOG"),
    CONSOLE("NotificationNodeType_CONSOLE");

    private final String type;

    NotificationNodeType(String type) {
        this.type = type;
    }

    @Override
    public Stages getStage() {
        return Stages.NOTIFICATION;
    }

    @Override
    public String getName() {
        return type;
    }
}