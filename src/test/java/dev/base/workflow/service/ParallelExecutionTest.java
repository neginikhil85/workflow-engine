package dev.base.workflow.service;

import dev.base.workflow.domain.engine.*;
import dev.base.workflow.model.core.Edge;
import dev.base.workflow.model.core.ExecutionContext;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import dev.base.workflow.mongo.collection.WorkflowDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParallelExecutionTest {

    private WorkflowEngine workflowEngine;
    private NodeTypeRegistry registry;
    private ExpressionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        registry = mock(NodeTypeRegistry.class);
        evaluator = mock(ExpressionEvaluator.class);
        EnvironmentService environmentService = mock(EnvironmentService.class);
        workflowEngine = new WorkflowEngine(registry, evaluator, environmentService);
    }

    @Test
    void testParallelExecution_MultiRoot() {
        // 1. Define Nodes
        NodeDefinition nodeA = createNode("A");
        NodeDefinition nodeB = createNode("B"); // Second Root
        NodeDefinition nodeC = createNode("C"); // Child of A
        NodeDefinition nodeD = createNode("D"); // Child of B

        // 2. Define Workflow
        WorkflowDefinition workflow = new WorkflowDefinition();
        workflow.setId("wf-parallel");
        workflow.setName("Parallel Test Workflow");
        workflow.setNodes(List.of(nodeA, nodeB, nodeC, nodeD));
        workflow.setEdges(List.of(
                new Edge("A", "C", null, null, null, null),
                new Edge("B", "D", null, null, null, null)));

        // 3. Mock Execution Logic
        NodeExecutor mockExecutor = mock(NodeExecutor.class);
        when(registry.resolve(any())).thenReturn(mockExecutor);
        when(mockExecutor.execute(any(), any(), any())).thenAnswer(invocation -> {
            NodeDefinition node = invocation.getArgument(0);
            NodeExecutionResult result = new NodeExecutionResult();
            result.setStatus(NodeExecutionResult.Status.SUCCESS);
            result.setExecutionDetails("Executed " + node.getId());
            return result;
        });

        // 4. Run
        WorkflowRunResult result = workflowEngine.run(workflow, new Object());

        // 5. Assertions
        List<String> executedIds = result.getExecutedNodeIds();
        System.out.println("Executed Nodes: " + executedIds);

        assertEquals(4, executedIds.size(), "All 4 nodes should be executed");
        assertTrue(executedIds.contains("A"));
        assertTrue(executedIds.contains("B"));
        assertTrue(executedIds.contains("C"));
        assertTrue(executedIds.contains("D"));
    }

    private NodeDefinition createNode(String id) {
        NodeDefinition node = new NodeDefinition();
        node.setId(id);
        node.setNodeType("TEST_TYPE"); // Needs meaningful type if registry cares, but we mock resolve(any())
        return node;
    }
}
