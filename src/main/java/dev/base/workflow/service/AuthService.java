package dev.base.workflow.service;

import dev.base.workflow.model.dto.request.auth.LoginRequest;
import dev.base.workflow.model.dto.request.auth.RegisterRequest;
import dev.base.workflow.model.dto.request.auth.TokenValidationRequest;
import dev.base.workflow.model.dto.response.auth.AuthResponse;
import dev.base.workflow.model.dto.response.auth.TokenRefreshResponse;
import dev.base.workflow.model.dto.response.auth.TokenValidationResponse;
import dev.base.workflow.mongo.collection.AuthProvider;
import dev.base.workflow.mongo.collection.User;
import dev.base.workflow.mongo.repository.UserRepository;
import dev.base.workflow.security.JwtService;
import dev.base.workflow.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TokenValidationResponse validateToken(TokenValidationRequest request) {
        String token = request.getToken();

        if (!jwtService.validateToken(token)) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .build();
        }

        return TokenValidationResponse.builder()
                .valid(true)
                .userId(jwtService.getUserIdFromToken(token))
                .email(jwtService.getEmailFromToken(token))
                .build();
    }

    public TokenRefreshResponse refreshToken(String userId) {
        User user = userService.getUser(userId);
        return TokenRefreshResponse.builder()
                .token(jwtService.generateToken(user.getId(), user.getEmail(), user.getName()))
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.LOCAL)
                .active(true)
                .build();

        user = userService.createUser(user);
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getName());

        return AuthResponse.builder()
                .token(token)
                .user(user)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userService.getUserByEmail(request.getEmail());

        if (user.getProvider() != AuthProvider.LOCAL && user.getPassword() == null) {
            throw new RuntimeException(
                    "Please login with " + (user.getProvider() != null ? user.getProvider() : "your social account"));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getName());

        return AuthResponse.builder()
                .token(token)
                .user(user)
                .build();
    }
}
