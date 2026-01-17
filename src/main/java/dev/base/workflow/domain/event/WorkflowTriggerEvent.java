package dev.base.workflow.domain.event;

import dev.base.workflow.mongo.collection.WorkflowRun;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WorkflowTriggerEvent extends ApplicationEvent {
    private final String workflowId;
    private final Object input;
    private final WorkflowRun.TriggerType triggerType;

    public WorkflowTriggerEvent(Object source, String workflowId, Object input, WorkflowRun.TriggerType triggerType) {
        super(source);
        this.workflowId = workflowId;
        this.input = input;
        this.triggerType = triggerType;
    }
}
