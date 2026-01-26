package dev.base.workflow.mongo.repository;

import dev.base.workflow.mongo.collection.EnvironmentVariable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnvironmentVariableRepository extends MongoRepository<EnvironmentVariable, String> {
}
