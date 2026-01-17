package dev.base.workflow.controller;

import dev.base.workflow.model.dto.ApiResponse;
import dev.base.workflow.mongo.collection.User;
import dev.base.workflow.mongo.collection.WorkflowDefinition;
import dev.base.workflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for user management.
 * All endpoints return standardized ApiResponse wrapper.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<List<User>> getAllUsers() {
        return ApiResponse.success(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUser(@PathVariable String id) {
        return ApiResponse.success(userService.getUser(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<User> createUser(@RequestBody User user) {
        return ApiResponse.success(userService.createUser(user), "User created successfully");
    }

    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(@PathVariable String id, @RequestBody User user) {
        return ApiResponse.success(userService.updateUser(id, user), "User updated successfully");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
    }

    @GetMapping("/{id}/workflows")
    public ApiResponse<List<WorkflowDefinition>> getUserWorkflows(@PathVariable String id) {
        return ApiResponse.success(userService.getUserWorkflows(id));
    }

    @GetMapping("/email/{email}")
    public ApiResponse<User> getUserByEmail(@PathVariable String email) {
        return ApiResponse.success(userService.getUserByEmail(email));
    }
}
