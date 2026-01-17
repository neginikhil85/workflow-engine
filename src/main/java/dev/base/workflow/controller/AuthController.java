package dev.base.workflow.controller;

import dev.base.workflow.model.dto.ApiResponse;
import dev.base.workflow.mongo.collection.User;
import dev.base.workflow.security.AuthenticatedUser;
import dev.base.workflow.security.JwtService;
import dev.base.workflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication controller for OAuth2 related endpoints.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    /**
     * Get current authenticated user info
     */
    @GetMapping("/me")
    public ApiResponse<User> getCurrentUser(@AuthenticationPrincipal AuthenticatedUser auth) {
        User user = userService.getUser(auth.getUserId());
        return ApiResponse.success(user);
    }

    /**
     * Validate a JWT token
     */
    @PostMapping("/validate")
    public ApiResponse<Map<String, Object>> validateToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        boolean valid = jwtService.validateToken(token);

        if (valid) {
            String userId = jwtService.getUserIdFromToken(token);
            String email = jwtService.getEmailFromToken(token);
            return ApiResponse.success(Map.of(
                    "valid", true,
                    "userId", userId,
                    "email", email));
        } else {
            return ApiResponse.success(Map.of("valid", false));
        }
    }

    /**
     * Refresh JWT token
     */
    @PostMapping("/refresh")
    public ApiResponse<Map<String, String>> refreshToken(@AuthenticationPrincipal AuthenticatedUser auth) {
        User user = userService.getUser(auth.getUserId());
        String newToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getName());
        return ApiResponse.success(Map.of("token", newToken));
    }
}
