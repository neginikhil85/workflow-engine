package dev.base.workflow.mongo.repository;

import dev.base.workflow.mongo.collection.NodeExecutionResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeExecutionResultRepository extends MongoRepository<NodeExecutionResult, String> {

}
