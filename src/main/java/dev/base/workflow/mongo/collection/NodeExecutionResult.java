package dev.base.workflow.mongo.collection;

import lombok.Data;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.mongodb.core.index.Indexed;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "node_execution_results")
public class NodeExecutionResult {
    @Id
    private String id;

    @Indexed
    private String runId;

    @Indexed
    private String executionId;

    private String nodeId;
    private Status status;
    private Object executionDetails;

    private List<String> nextNodes;
    private String errorMessage;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private long duration;

    public enum Status {
        SUCCESS,
        FAILURE,
        SKIPPED
    }

    // Helper method for success
    public static NodeExecutionResult success(String nodeId, Object executionDetails) {
        NodeExecutionResult result = new NodeExecutionResult();
        result.setNodeId(nodeId);
        result.setStatus(Status.SUCCESS);
        result.setExecutionDetails(executionDetails);
        return result;
    }

    // Helper method for success with branching
    public static NodeExecutionResult success(String nodeId, Object executionDetails,
            java.util.List<String> nextNodes) {
        NodeExecutionResult result = new NodeExecutionResult();
        result.setNodeId(nodeId);
        result.setStatus(Status.SUCCESS);
        result.setExecutionDetails(executionDetails);
        result.setNextNodes(nextNodes);
        return result;
    }

    // Helper method for failure
    public static NodeExecutionResult failure(String nodeId, String errorMessage) {
        NodeExecutionResult result = new NodeExecutionResult();
        result.setNodeId(nodeId);
        result.setStatus(Status.FAILURE);
        result.setErrorMessage(errorMessage);
        return result;
    }
}