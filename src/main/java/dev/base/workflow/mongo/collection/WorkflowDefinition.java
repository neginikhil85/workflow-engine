package dev.base.workflow.mongo.collection;

import dev.base.workflow.model.core.Edge;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Complete workflow definition with nodes and edges.
 * Stored as a document in MongoDB.
 */
@Data
@Document(collection = "workflows")
public class WorkflowDefinition {

    @Id
    private String id;

    private String name;
    private String description;
    private String startNodeId;

    private List<NodeDefinition> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    // Metadata
    private String ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active = true;
    private int version = 1;

    /**
     * Get node by ID
     */
    public NodeDefinition getNodeById(String nodeId) {
        return nodes.stream()
                .filter(node -> nodeId.equals(node.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get edges from a specific node
     */
    public List<Edge> getEdgesFrom(String fromNodeId) {
        return edges.stream()
                .filter(edge -> fromNodeId.equals(edge.getFrom()))
                .toList();
    }
}
