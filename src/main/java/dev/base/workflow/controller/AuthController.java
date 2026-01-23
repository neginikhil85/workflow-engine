package dev.base.workflow.controller;

import dev.base.workflow.model.dto.request.auth.LoginRequest;
import dev.base.workflow.model.dto.request.auth.RegisterRequest;
import dev.base.workflow.model.dto.request.auth.TokenValidationRequest;
import dev.base.workflow.model.dto.response.auth.AuthResponse;
import dev.base.workflow.model.dto.response.auth.TokenRefreshResponse;
import dev.base.workflow.model.dto.response.auth.TokenValidationResponse;
import dev.base.workflow.model.dto.response.common.ApiResponse;
import dev.base.workflow.mongo.collection.User;
import dev.base.workflow.security.AuthenticatedUser;
import dev.base.workflow.service.AuthService;
import dev.base.workflow.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller for OAuth2 related endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

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
    public ApiResponse<TokenValidationResponse> validateToken(@RequestBody TokenValidationRequest request) {
        TokenValidationResponse response = authService.validateToken(request);
        return ApiResponse.success(response);
    }

    /**
     * Refresh JWT token
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponse> refreshToken(@AuthenticationPrincipal AuthenticatedUser auth) {
        TokenRefreshResponse response = authService.refreshToken(auth.getUserId());
        return ApiResponse.success(response);
    }

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ApiResponse.success(response);
    }

    /**
     * Login with email and password
     */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ApiResponse.success(response);
    }
}
