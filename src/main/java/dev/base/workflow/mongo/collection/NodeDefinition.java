package dev.base.workflow.mongo.collection;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Map;

@Data
public class NodeDefinition {
    @Id
    private String id;
    private String nodeType;
    private Map<String, Object> config;

    // UI Metadata (position: {x, y}, label, etc.)
    private Map<String, Object> metadata;
}
