package dev.base.workflow.service.execution;

import dev.base.workflow.domain.engine.WorkflowEngine;
import dev.base.workflow.mongo.repository.WorkflowDefinitionRepository;
import dev.base.workflow.service.execution.helper.WorkflowExecutionHelper;
import dev.base.workflow.service.execution.helper.WorkflowRunHelper;
import dev.base.workflow.service.execution.trigger.KafkaTriggerManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowExecutionServiceTest {

    @Mock
    private WorkflowEngine workflowEngine;

    @Mock
    private WorkflowDefinitionRepository workflowRepository;

    @Mock
    private WorkflowScheduler workflowScheduler;

    @Mock
    private KafkaTriggerManager kafkaTriggerManager;

    @Mock
    private WorkflowRunHelper runHelper;

    @Mock
    private WorkflowExecutionHelper executionHelper;

    @InjectMocks
    private WorkflowExecutionService workflowExecutionService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void stopWorkflow_ShouldStopComponents() {
        String workflowId = "test-workflow-id";

        // Mock execution helper to return empty list or mock executions if needed
        when(executionHelper.findRunningExecutions(workflowId)).thenReturn(Collections.emptyList());

        workflowExecutionService.stopWorkflow(workflowId);

        // Verify Scheduler is unscheduled
        verify(workflowScheduler).unscheduleWorkflow(workflowId);

        // Verify Kafka Consumer is stopped
        verify(kafkaTriggerManager).stopConsumer(workflowId);

        // Verify Active runs are stopped
        verify(runHelper).stopActiveRuns(workflowId);
    }
}
