package com.learning.workflow.event;

import com.learning.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowEventListener {

    private final WorkflowService workflowService;

    @EventListener
    public void handleScheduledEvent(WorkflowScheduledEvent event) {
        log.info("Received scheduled event for workflow: {}", event.getWorkflowId());
        try {
            workflowService.executeWorkflow(event.getWorkflowId(), null);
        } catch (Exception e) {
            log.error("Failed to execute scheduled workflow: {}", event.getWorkflowId(), e);
        }
    }
}
