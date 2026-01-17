package dev.base.workflow.security;

import dev.base.workflow.config.AppConfig;
import dev.base.workflow.mongo.collection.User;
import dev.base.workflow.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static dev.base.workflow.constant.SecurityConstants.*;

/**
 * Handles successful OAuth2 authentication.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserService userService;
    private final AppConfig appConfig;

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
        var frontend = appConfig.getFrontend();
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getName());
        String redirectUrl = frontend.getUrl() + frontend.getAuthCallbackPath() + token;
        log.info("Redirecting to: {}", redirectUrl);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String extractEmail(OAuth2User oAuth2User, String provider) {
        return switch (provider) {
            case PROVIDER_GOOGLE -> oAuth2User.getAttribute(ATTR_EMAIL);
            case PROVIDER_GITHUB -> extractGithubEmail(oAuth2User);
            default -> oAuth2User.getAttribute(ATTR_EMAIL);
        };
    }

    private String extractGithubEmail(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute(ATTR_EMAIL);
        if (email == null) {
            String login = oAuth2User.getAttribute(ATTR_LOGIN);
            return login + GITHUB_EMAIL_FALLBACK_DOMAIN;
        }
        return email;
    }

    private String extractName(OAuth2User oAuth2User, String provider) {
        return switch (provider) {
            case PROVIDER_GOOGLE -> oAuth2User.getAttribute(ATTR_NAME);
            case PROVIDER_GITHUB -> extractGithubName(oAuth2User);
            default -> oAuth2User.getAttribute(ATTR_NAME);
        };
    }

    private String extractGithubName(OAuth2User oAuth2User) {
        String name = oAuth2User.getAttribute(ATTR_NAME);
        return name != null ? name : oAuth2User.getAttribute(ATTR_LOGIN);
    }
}
