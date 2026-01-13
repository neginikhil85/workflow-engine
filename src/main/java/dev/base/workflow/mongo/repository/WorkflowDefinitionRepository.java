package dev.base.workflow.mongo.repository;

import dev.base.workflow.mongo.collection.WorkflowDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowDefinitionRepository extends MongoRepository<WorkflowDefinition, String> {

    List<WorkflowDefinition> findByActiveTrue();

    Optional<WorkflowDefinition> findByIdAndActiveTrue(String id);

    List<WorkflowDefinition> findByCreatedBy(String createdBy);
}
