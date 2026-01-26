package dev.base.workflow.service;

import dev.base.workflow.mongo.collection.EnvironmentVariable;
import dev.base.workflow.mongo.repository.EnvironmentVariableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnvironmentService {

    private final EnvironmentVariableRepository repository;

    /**
     * Get all environment variables as a Map for execution context.
     * Key: Variable ID (e.g. "API_KEY")
     * Value: Variable Value
     */
    public Map<String, String> getGlobalContext() {
        return repository.findAll().stream()
                .collect(Collectors.toMap(EnvironmentVariable::getId, EnvironmentVariable::getValue));
    }

    public List<EnvironmentVariable> getAllVariables() {
        return repository.findAll();
    }

    public EnvironmentVariable saveVariable(EnvironmentVariable variable) {
        if (variable.getId() == null || variable.getId().isBlank()) {
            throw new IllegalArgumentException("Variable ID/Key cannot be empty");
        }
        // Normalize key to uppercase
        variable.setId(variable.getId().toUpperCase().trim());
        variable.setUpdatedAt(LocalDateTime.now());
        if (variable.getCreatedAt() == null) {
            variable.setCreatedAt(LocalDateTime.now());
        }
        return repository.save(variable);
    }

    public void deleteVariable(String id) {
        repository.deleteById(id);
    }

    public Optional<EnvironmentVariable> getVariable(String id) {
        return repository.findById(id);
    }
}
