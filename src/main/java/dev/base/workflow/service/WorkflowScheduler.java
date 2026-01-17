package dev.base.workflow.service;

import dev.base.workflow.constant.WorkflowConstants;
import dev.base.workflow.domain.event.WorkflowScheduledEvent;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.WorkflowDefinition;
import dev.base.workflow.model.nodetype.TriggerNodeType;

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
            scheduleCronNode(node, workflowId);
        }
    }

    private void scheduleCronNode(NodeDefinition node, String workflowId) {
        if (!isCronNode(node)) {
            return;
        }

        String cronExpression = (String) node.getConfig().get(WorkflowConstants.CFG_CRON);
        log.info("Found CRON node for workflow {} with expression: {}", workflowId, cronExpression);

        if (cronExpression != null && !cronExpression.isBlank()) {
            try {
                // Note: runId is null here for legacy scheduling (e.g., on startup)
                scheduleCronTask(workflowId, cronExpression, null);
                log.info("Successfully scheduled workflow {} with cron {}", workflowId, cronExpression);
            } catch (Exception e) {
                log.error("Failed to schedule workflow {}", workflowId, e);
            }
        } else {
            log.warn("Cron expression is missing or blank for node {}", node.getId());
        }
    }

    private boolean isCronNode(NodeDefinition node) {
        return TriggerNodeType.CRON.name().equals(node.getNodeType());
    }

    public void unscheduleWorkflow(String workflowId) {
        ScheduledFuture<?> future = scheduledTasks.remove(workflowId);
        if (future != null) {
            future.cancel(false);
            log.info("Unschedulled workflow {}", workflowId);
        }
    }

    public void scheduleCronTask(String workflowId, String cronExpression, String runId) {
        // Critical: Cancel existing task to prevent duplicates/orphan threads
        if (scheduledTasks.containsKey(workflowId)) {
            log.warn("Workflow {} is already scheduled. Cancelling previous task before rescheduling.", workflowId);
            unscheduleWorkflow(workflowId);
        }

        Runnable task = () -> {
            log.info("Cron Trigger Fired for workflow: {} with runId: {}", workflowId, runId);
            eventPublisher.publishEvent(new WorkflowScheduledEvent(this, workflowId, runId));
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
