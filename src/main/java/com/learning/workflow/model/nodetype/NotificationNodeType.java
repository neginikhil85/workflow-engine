package com.learning.workflow.model.nodetype;

import com.learning.workflow.model.stage.Stages;

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