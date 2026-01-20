package dev.base.workflow.service;

import dev.base.workflow.model.dto.response.auth.TokenRefreshResponse;
import dev.base.workflow.model.dto.request.auth.TokenValidationRequest;
import dev.base.workflow.model.dto.response.auth.TokenValidationResponse;
import dev.base.workflow.mongo.collection.User;
import dev.base.workflow.security.JwtService;
import dev.base.workflow.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;

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
}
