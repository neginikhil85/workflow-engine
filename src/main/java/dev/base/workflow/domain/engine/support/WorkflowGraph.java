package dev.base.workflow.domain.engine.support;

import dev.base.workflow.domain.engine.ExpressionEvaluator;
import dev.base.workflow.model.core.Edge;
import dev.base.workflow.model.core.ExecutionContext;
import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.collection.WorkflowDefinition;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Encapsulates the structural representation of a workflow and its traversal
 * logic.
 */
public class WorkflowGraph {

    private final Map<String, NodeDefinition> nodeMap;
    private final Map<String, List<Edge>> adjacencyList;
    private final List<NodeDefinition> allNodes;

    public WorkflowGraph(WorkflowDefinition workflow) {
        this.allNodes = workflow.getNodes() != null ? workflow.getNodes() : Collections.emptyList();
        this.nodeMap = mapNodes(this.allNodes);
        this.adjacencyList = mapEdges(workflow.getEdges() != null ? workflow.getEdges() : Collections.emptyList());
    }

    public NodeDefinition getNode(String nodeId) {
        return nodeMap.get(nodeId);
    }

    /**
     * Identifies root nodes (nodes with no incoming edges) to start execution.
     */
    public List<NodeDefinition> getRootNodes() {
        Set<String> targetNodes = new HashSet<>();
        adjacencyList.values().forEach(edges -> edges.forEach(edge -> targetNodes.add(edge.getTo())));

        List<NodeDefinition> roots = new ArrayList<>();
        for (NodeDefinition node : allNodes) {
            if (!targetNodes.contains(node.getId())) {
                roots.add(node);
            }
        }
        return roots;
    }

    /**
     * Determines the next nodes to execute based on edge conditions.
     */
    public List<String> determineNextNodes(NodeDefinition currentNode, Object data,
            ExpressionEvaluator evaluator, ExecutionContext context) {
        List<Edge> outgoing = adjacencyList.get(currentNode.getId());
        if (CollectionUtils.isEmpty(outgoing)) {
            return Collections.emptyList();
        }

        List<String> nextIds = new ArrayList<>();
        for (Edge e : outgoing) {
            if (e.getCondition() == null || evaluator.evaluate(e.getCondition(), data, context)) {
                nextIds.add(e.getTo());
            }
        }
        return nextIds;
    }

    private Map<String, NodeDefinition> mapNodes(List<NodeDefinition> nodes) {
        Map<String, NodeDefinition> map = new HashMap<>();
        for (NodeDefinition node : nodes) {
            map.put(node.getId(), node);
        }
        return map;
    }

    private Map<String, List<Edge>> mapEdges(List<Edge> edges) {
        Map<String, List<Edge>> map = new HashMap<>();
        for (Edge e : edges) {
            map.computeIfAbsent(e.getFrom(), k -> new ArrayList<>()).add(e);
        }
        return map;
    }
}
