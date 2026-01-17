package dev.base.workflow.domain.engine;

import dev.base.workflow.exception.WorkflowException;
import dev.base.workflow.model.core.Edge;
import dev.base.workflow.model.core.ExecutionContext;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import dev.base.workflow.mongo.collection.WorkflowDefinition;
import dev.base.workflow.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static dev.base.workflow.constant.WorkflowConstants.*;

@Component
@Slf4j
public class WorkflowEngine {

    private final NodeTypeRegistry registry;
    private final ExpressionEvaluator evaluator;

    public WorkflowEngine(NodeTypeRegistry registry, ExpressionEvaluator evaluator) {
        this.registry = registry;
        this.evaluator = evaluator;
    }

    public WorkflowRunResult run(WorkflowDefinition workflow, Object initialInput) {
        return run(workflow, initialInput, null);
    }

    public WorkflowRunResult run(WorkflowDefinition workflow, Object initialInput, String runId) {
        Map<String, NodeDefinition> nodeMap = mapNodes(workflow.getNodes());
        Map<String, List<Edge>> graph = mapEdges(workflow.getEdges());
        ExecutionContext context = createExecutionContext(workflow, runId);

        Queue<ExecutionItem> queue = initializeQueue(workflow, initialInput);
        Object lastOutput = initialInput;
        List<String> executedNodeIds = new ArrayList<>();
        List<NodeExecutionResult> nodeResults = new ArrayList<>();

        int safetyCounter = 0;
        final int MAX_STEPS = 1000;

        while (!queue.isEmpty()) {
            checkMaxSteps(++safetyCounter, MAX_STEPS);

            ExecutionItem item = queue.poll();
            NodeDefinition node = nodeMap.get(item.nodeId);

            if (node == null)
                continue;

            executedNodeIds.add(node.getId());
            context.put(KEY_CURRENT_NODE_ID, node.getId());

            NodeExecutionResult result = executeNodeWithMonitoring(node, item.executionData, context, runId);
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

    private ExecutionContext createExecutionContext(WorkflowDefinition workflow, String runId) {
        ExecutionContext context = new ExecutionContext();
        context.put(KEY_WORKFLOW_ID, workflow.getId());
        context.put(KEY_WORKFLOW_NAME, workflow.getName());
        if (runId != null) {
            context.put(KEY_RUN_ID, runId);
        }
        return context;
    }

    private Queue<ExecutionItem> initializeQueue(WorkflowDefinition workflow, Object initialInput) {
        Queue<ExecutionItem> queue = new LinkedList<>();
        if (workflow.getStartNodeId() != null) {
            queue.add(new ExecutionItem(workflow.getStartNodeId(), initialInput));
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

    private void processSuccess(NodeExecutionResult result, NodeDefinition node, Map<String, List<Edge>> graph,
            ExecutionContext context, Queue<ExecutionItem> queue) {
        List<String> nextNodes = result.getNextNodes();
        if (CollectionUtils.isEmpty(nextNodes)) {
            nextNodes = determineNextNodes(node, result.getExecutionDetails(), graph, context);
        }

        for (String nextId : nextNodes) {
            queue.add(new ExecutionItem(nextId, result.getExecutionDetails()));
        }
    }

    private Map<String, NodeDefinition> mapNodes(List<NodeDefinition> nodes) {
        Map<String, NodeDefinition> nodeMap = new HashMap<>();
        for (NodeDefinition node : nodes)
            nodeMap.put(node.getId(), node);
        return nodeMap;
    }

    private Map<String, List<Edge>> mapEdges(List<Edge> edges) {
        Map<String, List<Edge>> graph = new HashMap<>();
        for (Edge e : edges)
            graph.computeIfAbsent(e.getFrom(), k -> new ArrayList<>()).add(e);
        return graph;
    }

    private NodeExecutionResult executeNode(NodeDefinition node, Object input,
            ExecutionContext context) {
        var executor = registry.resolve(node.getNodeType());
        executor.validate(node);
        return executor.execute(node, input, context);
    }

    private List<String> determineNextNodes(NodeDefinition node, Object data, Map<String, List<Edge>> graph,
            ExecutionContext context) {
        List<Edge> outgoing = graph.get(node.getId());
        if (CollectionUtils.isEmpty(outgoing))
            return Collections.emptyList();

        List<String> nextIds = new ArrayList<>();
        for (Edge e : outgoing) {
            if (e.getCondition() == null || evaluator.evaluate(e.getCondition(), data, context)) {
                nextIds.add(e.getTo());
            }
        }
        return nextIds;
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
