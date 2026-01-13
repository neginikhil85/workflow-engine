package dev.base.workflow.domain.engine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowRunResult {
    private Object output;
    private List<String> executedNodeIds;
    private List<dev.base.workflow.mongo.collection.NodeExecutionResult> nodeResults;
}
