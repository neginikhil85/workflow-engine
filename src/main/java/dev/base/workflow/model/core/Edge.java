package dev.base.workflow.model.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a connection between two nodes in the workflow graph.
 * Can have an optional condition for conditional routing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Edge {
    private String from;
    private String to;
    private String condition; // optional runtime condition (SpEL expression)
    private String label; // optional label for UI display
}
