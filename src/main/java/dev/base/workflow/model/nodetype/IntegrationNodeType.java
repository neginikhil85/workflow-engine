package dev.base.workflow.model.nodetype;

import dev.base.workflow.model.stage.Stages;

public enum IntegrationNodeType implements NodeType {
    HTTP_CALL("IntegrationNodeType_HTTP_CALL"),
    KAFKA("IntegrationNodeType_KAFKA"),
    ARTEMIS_QUEUE("IntegrationNodeType_ARTEMIS_QUEUE"),
    ACTIVE_MQ("IntegrationNodeType_ACTIVE_MQ");

    private final String type;

    IntegrationNodeType(String type) {
        this.type = type;
    }

    @Override
    public Stages getStage() {
        return Stages.INTEGRATION;
    }

    @Override
    public String getName() {
        return type;
    }
}
