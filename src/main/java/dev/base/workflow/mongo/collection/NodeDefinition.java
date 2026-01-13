package dev.base.workflow.mongo.collection;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Document(collection = "nodes")
public class NodeDefinition {
    @Id
    private String id;
    private String nodeType;
    private Map<String, Object> config;
    private List<String> nextNodeIds; // runtime chaining

    // UI Metadata (position: {x, y}, label, etc.)
    private Map<String, Object> metadata;
}
