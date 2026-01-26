package dev.base.workflow.domain.engine;

import dev.base.workflow.exception.WorkflowException;
import dev.base.workflow.model.core.ExecutionContext;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import dev.base.workflow.mongo.collection.WorkflowDefinition;
import dev.base.workflow.service.EnvironmentService;
import dev.base.workflow.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import dev.base.workflow.domain.engine.support.WorkflowGraph;

import static dev.base.workflow.constant.WorkflowConstants.*;

@Component
@Slf4j
public class WorkflowEngine {

    private final NodeTypeRegistry registry;
    private final ExpressionEvaluator evaluator;
    private final EnvironmentService environmentService;

    public WorkflowEngine(NodeTypeRegistry registry, ExpressionEvaluator evaluator,
            EnvironmentService environmentService) {
        this.registry = registry;
        this.evaluator = evaluator;
        this.environmentService = environmentService;
    }

    public WorkflowRunResult run(WorkflowDefinition workflow, Object initialInput) {
        return run(workflow, initialInput, null);
    }

    public WorkflowRunResult run(WorkflowDefinition workflow, Object initialInput, String runId) {
        WorkflowGraph graph = new WorkflowGraph(workflow);
        ExecutionContext context = prepareExecutionContext(workflow, runId);
        Queue<ExecutionItem> queue = initializeQueue(graph, initialInput);

        return executeWorkflowLoop(graph, context, queue, initialInput, runId);
    }

    private ExecutionContext prepareExecutionContext(WorkflowDefinition workflow, String runId) {
        ExecutionContext context = createExecutionContext(workflow, runId);
        context.put(EXPR_VAR_ENV, environmentService.getGlobalContext());
        return context;
    }

    private WorkflowRunResult executeWorkflowLoop(WorkflowGraph graph, ExecutionContext context,
            Queue<ExecutionItem> queue, Object initialInput, String runId) {
        List<String> executedNodeIds = new ArrayList<>();
        List<NodeExecutionResult> nodeResults = new ArrayList<>();
        Object lastOutput = initialInput;
        int steps = 0;
        final int MAX_STEPS = 1000;

        while (!queue.isEmpty()) {
            checkMaxSteps(++steps, MAX_STEPS);

            ExecutionItem item = queue.poll();
            NodeDefinition node = graph.getNode(item.nodeId);

            if (node == null)
                continue;

            NodeExecutionResult result = executeNodeStep(node, item, context, runId);

            executedNodeIds.add(node.getId());
            nodeResults.add(result);

            if (result.getStatus() == NodeExecutionResult.Status.SUCCESS) {
                lastOutput = result.getExecutionDetails();
                processSuccess(result, node, graph, context, queue);
            } else {
                log.warn("Node {} failed with status: {}", node.getId(), result.getStatus());
            }
        }

        return new WorkflowRunResult(lastOutput, executedNodeIds, nodeResults);
    }

    private NodeExecutionResult executeNodeStep(NodeDefinition node, ExecutionItem item,
            ExecutionContext context, String runId) {
        context.put(KEY_CURRENT_NODE_ID, node.getId());
        return executeNodeWithMonitoring(node, item.executionData, context, runId);
    }

    private ExecutionContext createExecutionContext(WorkflowDefinition workflow, String runId) {
        ExecutionContext context = new ExecutionContext();
        context.put(KEY_WORKFLOW_ID, workflow.getId());
        context.put(KEY_WORKFLOW_NAME, workflow.getName());
        if (runId != null) {
            context.put(KEY_RUN_ID, runId);
        }
        return context;
    }

    private Queue<ExecutionItem> initializeQueue(WorkflowGraph graph, Object initialInput) {
        Queue<ExecutionItem> queue = new LinkedList<>();
        List<NodeDefinition> roots = graph.getRootNodes();

        for (NodeDefinition node : roots) {
            queue.add(new ExecutionItem(node.getId(), initialInput));
        }
        return queue;
    }

    private void checkMaxSteps(int counter, int maxSteps) {
        if (counter > maxSteps) {
            throw new WorkflowException(
                    StringUtils.format("Workflow execution exceeded max steps ({}). Possible infinite loop.",
                            maxSteps));
        }
    }

    private NodeExecutionResult executeNodeWithMonitoring(NodeDefinition node, Object input, ExecutionContext context,
            String runId) {
        long startTime = System.currentTimeMillis();
        NodeExecutionResult result;

        try {
            result = executeNode(node, input, context);
        } catch (Exception e) {
            log.error("Error executing node {}: {}", node.getId(), e.getMessage(), e);
            throw new WorkflowException(StringUtils.format("Error executing node {}: {}", node.getId(), e.getMessage()),
                    e);
        }

        enrichResultWithMetrics(result, runId, input, startTime);
        return result;
    }

    private void enrichResultWithMetrics(NodeExecutionResult result, String runId, Object input, long startTime) {
        result.setRunId(runId);
        result.setDuration(System.currentTimeMillis() - startTime);
        result.setCompletedAt(java.time.LocalDateTime.now());
        result.setStartedAt(result.getCompletedAt().minusNanos(result.getDuration() * 1000000));
    }

    private void processSuccess(NodeExecutionResult result, NodeDefinition node, WorkflowGraph graph,
            ExecutionContext context, Queue<ExecutionItem> queue) {
        List<String> nextNodes = result.getNextNodes();
        if (CollectionUtils.isEmpty(nextNodes)) {
            nextNodes = graph.determineNextNodes(node, result.getExecutionDetails(), evaluator, context);
        }

        for (String nextId : nextNodes) {
            queue.add(new ExecutionItem(nextId, result.getExecutionDetails()));
        }
    }

    private NodeExecutionResult executeNode(NodeDefinition node, Object input,
            ExecutionContext context) {
        var executor = registry.resolve(node.getNodeType());
        executor.validate(node);
        return executor.execute(node, input, context);
    }

    private static class ExecutionItem {
        String nodeId;
        Object executionData;

        public ExecutionItem(String nodeId, Object executionData) {
            this.nodeId = nodeId;
            this.executionData = executionData;
        }
    }
}
