package dev.base.workflow.mongo.repository;

import dev.base.workflow.mongo.collection.NodeDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeDefinitionRepository extends MongoRepository<NodeDefinition, String> {

}
