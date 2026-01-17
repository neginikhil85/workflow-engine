package dev.base.workflow.service;

import dev.base.workflow.exception.UserNotFoundException;
import dev.base.workflow.mongo.collection.User;
import dev.base.workflow.mongo.collection.WorkflowDefinition;
import dev.base.workflow.mongo.repository.UserRepository;
import dev.base.workflow.mongo.repository.WorkflowDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for user management operations.
 * Handles user CRUD and user-workflow relationships.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final WorkflowDefinitionRepository workflowRepository;

    /**
     * Create a new user
     */
    public User createUser(User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);
        log.info("Creating user with email: {}", user.getEmail());
        return userRepository.save(user);
    }

    /**
     * Get user by ID
     */
    public User getUser(String id) {
        return userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> UserNotFoundException.byEmail(email));
    }

    /**
     * Get all active users
     */
    public List<User> getAllUsers() {
        return userRepository.findByActiveTrue();
    }

    /**
     * Update user
     */
    public User updateUser(String id, User userUpdate) {
        User existing = getUser(id);

        if (userUpdate.getName() != null) {
            existing.setName(userUpdate.getName());
        }
        if (userUpdate.getEmail() != null) {
            existing.setEmail(userUpdate.getEmail());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        log.info("Updating user: {}", id);
        return userRepository.save(existing);
    }

    /**
     * Soft delete user
     */
    public void deleteUser(String id) {
        User user = getUser(id);
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Soft deleted user: {}", id);
    }

    /**
     * Get all workflows owned by a user
     */
    public List<WorkflowDefinition> getUserWorkflows(String userId) {
        // Verify user exists
        getUser(userId);
        return workflowRepository.findByOwnerIdAndActiveTrue(userId);
    }

    /**
     * Get or create user by email (useful for auto-registration)
     */
    public User getOrCreateUser(String email, String name) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> createUser(User.builder()
                        .email(email)
                        .name(name)
                        .build()));
    }
}
