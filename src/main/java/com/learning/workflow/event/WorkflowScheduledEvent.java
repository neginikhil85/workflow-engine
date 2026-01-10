package com.learning.workflow.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WorkflowScheduledEvent extends ApplicationEvent {
    private final String workflowId;
    private final String runId;

    public WorkflowScheduledEvent(Object source, String workflowId, String runId) {
        super(source);
        this.workflowId = workflowId;
        this.runId = runId;
    }
}
