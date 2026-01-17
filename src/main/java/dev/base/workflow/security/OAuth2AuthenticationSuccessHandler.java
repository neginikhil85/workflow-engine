package dev.base.workflow.security;

import dev.base.workflow.mongo.collection.User;
import dev.base.workflow.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles successful OAuth2 authentication.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserService userService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        User user = processOAuthUser(oauthToken);
        redirectWithToken(request, response, user);
    }

    private User processOAuthUser(OAuth2AuthenticationToken oauthToken) {
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        log.info("OAuth2 login success from provider: {}", provider);

        String email = extractEmail(oAuth2User, provider);
        String name = extractName(oAuth2User, provider);
        log.info("OAuth2 user: email={}, name={}", email, name);

        return userService.getOrCreateUser(email, name);
    }

    private void redirectWithToken(HttpServletRequest request,
            HttpServletResponse response,
            User user) throws IOException {
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getName());
        String redirectUrl = frontendUrl + "/auth/callback?token=" + token;
        log.info("Redirecting to: {}", redirectUrl);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String extractEmail(OAuth2User oAuth2User, String provider) {
        return switch (provider) {
            case "google" -> oAuth2User.getAttribute("email");
            case "github" -> extractGithubEmail(oAuth2User);
            default -> oAuth2User.getAttribute("email");
        };
    }

    private String extractGithubEmail(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            String login = oAuth2User.getAttribute("login");
            return login + "@github.user";
        }
        return email;
    }

    private String extractName(OAuth2User oAuth2User, String provider) {
        return switch (provider) {
            case "google" -> oAuth2User.getAttribute("name");
            case "github" -> extractGithubName(oAuth2User);
            default -> oAuth2User.getAttribute("name");
        };
    }

    private String extractGithubName(OAuth2User oAuth2User) {
        String name = oAuth2User.getAttribute("name");
        return name != null ? name : oAuth2User.getAttribute("login");
    }
}
