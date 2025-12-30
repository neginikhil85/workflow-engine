package com.learning.workflow.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WorkflowScheduledEvent extends ApplicationEvent {
    private final String workflowId;

    public WorkflowScheduledEvent(Object source, String workflowId) {
        super(source);
        this.workflowId = workflowId;
    }
}
