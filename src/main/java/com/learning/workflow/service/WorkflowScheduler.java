package com.learning.workflow.service;

import com.learning.workflow.event.WorkflowScheduledEvent;
import com.learning.workflow.model.core.NodeDefinition;
import com.learning.workflow.model.core.WorkflowDefinition;
import com.learning.workflow.model.nodetype.TriggerNodeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowScheduler {

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final TaskScheduler taskScheduler;
    private final ApplicationEventPublisher eventPublisher;

    public void scheduleWorkflow(WorkflowDefinition workflow) {
        String workflowId = workflow.getId();
        log.info("Attempting to schedule workflow: {}", workflowId);

        unscheduleWorkflow(workflowId);

        if (!workflow.isActive()) {
            log.info("Workflow {} is inactive, skipping schedule.", workflowId);
            return;
        }

        List<NodeDefinition> nodes = workflow.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            log.info("Workflow {} has no nodes.", workflowId);
            return;
        }

        for (NodeDefinition node : nodes) {

            if ("TriggerNodeType_CRON".equals(node.getNodeType()) ||
                    TriggerNodeType.CRON.name().equals(node.getNodeType())) {

                String cronExpression = (String) node.getConfig().get("cron");
                log.info("Found CRON node for workflow {} with expression: {}", workflowId, cronExpression);

                if (cronExpression != null && !cronExpression.isBlank()) {
                    try {
                        scheduleCronTask(workflowId, cronExpression);
                        log.info("Successfully scheduled workflow {} with cron {}", workflowId, cronExpression);
                    } catch (Exception e) {
                        log.error("Failed to schedule workflow {}", workflowId, e);
                    }
                } else {
                    log.warn("Cron expression is missing or blank for node {}", node.getId());
                }
            }
        }
    }

    public void unscheduleWorkflow(String workflowId) {
        ScheduledFuture<?> future = scheduledTasks.remove(workflowId);
        if (future != null) {
            future.cancel(false);
            log.info("Unschedulled workflow {}", workflowId);
        }
    }

    public void scheduleCronTask(String workflowId, String cronExpression) {
        Runnable task = () -> {
            log.info("Cron Trigger Fired for workflow: {}", workflowId);
            eventPublisher.publishEvent(new WorkflowScheduledEvent(this, workflowId));
        };

        ScheduledFuture<?> future = taskScheduler.schedule(task, new CronTrigger(cronExpression));
        scheduledTasks.put(workflowId, future);
    }

    /**
     * Check if a workflow is currently scheduled
     */
    public boolean isScheduled(String workflowId) {
        ScheduledFuture<?> future = scheduledTasks.get(workflowId);
        return future != null && !future.isCancelled() && !future.isDone();
    }
}
