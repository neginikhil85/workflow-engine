package com.learning.workflow.repository;

import com.learning.workflow.model.core.NodeDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeDefinitionRepository extends MongoRepository<NodeDefinition, String> {

}

