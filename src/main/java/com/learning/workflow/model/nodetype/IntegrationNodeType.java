package com.learning.workflow.model.nodetype;

import com.learning.workflow.model.stage.Stages;

public enum IntegrationNodeType implements NodeType {
    HTTP_CALL,
    KAFKA,
    ARTEMIS_QUEUE,
    ACTIVE_MQ;

    @Override
    public Stages getStage() {
        return Stages.INTEGRATION;
    }

    @Override
    public String getName() {
        return name();
    }
}
