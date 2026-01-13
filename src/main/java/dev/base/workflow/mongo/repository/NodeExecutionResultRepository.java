package dev.base.workflow.mongo.repository;

import dev.base.workflow.mongo.collection.NodeExecutionResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NodeExecutionResultRepository extends MongoRepository<NodeExecutionResult, String> {
    List<NodeExecutionResult> findByRunId(String runId);
}
