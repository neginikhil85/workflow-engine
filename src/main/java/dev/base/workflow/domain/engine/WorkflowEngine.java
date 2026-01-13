package dev.base.workflow.domain.engine;

import dev.base.workflow.model.core.*;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.NodeExecutionResult;
import dev.base.workflow.mongo.collection.WorkflowDefinition;
import dev.base.workflow.mongo.collection.WorkflowExecution;
import static dev.base.workflow.constant.WorkflowConstants.*;
import dev.base.workflow.util.StringUtils;

import java.util.*;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
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

        ExecutionContext ctx = new ExecutionContext();
        ctx.put(KEY_WORKFLOW_ID, workflow.getId());
        ctx.put(KEY_WORKFLOW_NAME, workflow.getName());
        if (runId != null) {
            ctx.put(KEY_RUN_ID, runId);
        }

        Queue<ExecutionItem> queue = new LinkedList<>();
        if (workflow.getStartNodeId() != null) {
            queue.add(new ExecutionItem(workflow.getStartNodeId(), initialInput));
        }

        Object lastOutput = initialInput;
        List<String> executedNodeIds = new ArrayList<>();

        int safetyCounter = 0;
        final int MAX_STEPS = 1000;

        while (!queue.isEmpty()) {
            if (++safetyCounter > MAX_STEPS) {
                throw new RuntimeException(
                        StringUtils.format("Workflow execution exceeded max steps ({}). Possible infinite loop.",
                                MAX_STEPS));
            }

            ExecutionItem item = queue.poll();
            String currentId = item.nodeId;
            Object input = item.input;

            NodeDefinition node = nodeMap.get(currentId);
            if (node == null)
                continue;

            executedNodeIds.add(currentId);
            ctx.put(KEY_CURRENT_NODE_ID, currentId);

            NodeExecutionResult result;
            try {
                // Execute Node
                result = executeNode(node, input, ctx);

            } catch (Exception e) {
                // Log and Fail
                System.err.println(StringUtils.format("Error executing node {}: {}", currentId, e.getMessage()));
                throw new RuntimeException(StringUtils.format("Error executing node {}: {}", currentId, e.getMessage()),
                        e);
            }

            if (result.getStatus() == NodeExecutionResult.Status.SUCCESS) {
                lastOutput = result.getOutputData();

                List<String> nextNodes = result.getNextNodes();
                if (nextNodes == null || nextNodes.isEmpty()) {
                    nextNodes = determineNextNodes(node, result.getOutputData(), graph, ctx);
                }

                for (String nextId : nextNodes) {
                    queue.add(new ExecutionItem(nextId, result.getOutputData()));
                }
            } else {
                // Functional Failure
                System.err.println(StringUtils.format("Node {} failed with status: {}", currentId, result.getStatus()));
            }
        }

        return new WorkflowRunResult(lastOutput, executedNodeIds);
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
            ExecutionContext ctx) {
        var executor = registry.resolve(node.getNodeType());
        executor.validate(node);
        return executor.execute(node, input, ctx);
    }

    private List<String> determineNextNodes(NodeDefinition node, Object data, Map<String, List<Edge>> graph,
            ExecutionContext ctx) {
        List<Edge> outgoing = graph.get(node.getId());
        if (CollectionUtils.isEmpty(outgoing))
            return Collections.emptyList();

        List<String> nextIds = new ArrayList<>();
        for (Edge e : outgoing) {
            if (e.getCondition() == null || evaluator.evaluate(e.getCondition(), data, ctx)) {
                nextIds.add(e.getTo());
            }
        }
        return nextIds;
    }

    private static class ExecutionItem {
        String nodeId;
        Object input;

        public ExecutionItem(String nodeId, Object input) {
            this.nodeId = nodeId;
            this.input = input;
        }
    }
}
