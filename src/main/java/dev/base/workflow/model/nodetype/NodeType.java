package dev.base.workflow.model.nodetype;

import dev.base.workflow.model.stage.Stages;

public interface NodeType {
    Stages getStage();
    String getName();
}
