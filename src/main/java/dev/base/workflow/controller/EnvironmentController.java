package dev.base.workflow.controller;

import dev.base.workflow.mongo.collection.EnvironmentVariable;
import dev.base.workflow.service.EnvironmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/env")
@RequiredArgsConstructor
public class EnvironmentController {

    private final EnvironmentService environmentService;

    @GetMapping
    public ResponseEntity<List<EnvironmentVariable>> getAllVariables() {
        return ResponseEntity.ok(environmentService.getAllVariables());
    }

    @PostMapping
    public ResponseEntity<EnvironmentVariable> createOrUpdateVariable(@RequestBody EnvironmentVariable variable) {
        return ResponseEntity.ok(environmentService.saveVariable(variable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVariable(@PathVariable String id) {
        environmentService.deleteVariable(id);
        return ResponseEntity.noContent().build();
    }
}
