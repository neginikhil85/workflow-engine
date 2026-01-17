package dev.base.workflow.mongo.repository;

import dev.base.workflow.mongo.collection.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndActiveTrue(String id);

    List<User> findByActiveTrue();
}
