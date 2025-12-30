package com.learning.workflow.model.nodetype;

import com.learning.workflow.model.stage.Stages;

public interface NodeType {
    Stages getStage();
    String getName();
}
