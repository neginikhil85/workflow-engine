package com.learning.workflow.repository;

import com.learning.workflow.model.core.NodeExecutionResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeExecutionResultRepository extends MongoRepository<NodeExecutionResult, String> {

}
