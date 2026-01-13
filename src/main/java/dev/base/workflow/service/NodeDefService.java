package dev.base.workflow.service;

import dev.base.workflow.mongo.collection.NodeDefinition;
import dev.base.workflow.mongo.repository.NodeDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NodeDefService {
    private final NodeDefinitionRepository repository;

    public List<NodeDefinition> getAll() {
        return repository.findAll();
    }
}
