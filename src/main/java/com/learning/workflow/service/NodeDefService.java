package com.learning.workflow.service;

import com.learning.workflow.model.core.NodeDefinition;
import com.learning.workflow.repository.NodeDefinitionRepository;
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
