package dev.base.workflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Application-wide configuration properties.
 * Loaded from application.yml under 'app' prefix.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private JwtConfig jwt;
    private AuthRedirectConfig frontend;
    private CorsConfig cors;
    private SecurityPathsConfig security;

    @Data
    public static class JwtConfig {
        private String secret;
        private long expiration;
    }

    @Data
    public static class AuthRedirectConfig {
        private String url;
        private String authCallbackPath;
    }

    @Data
    public static class CorsConfig {
        private List<String> allowedOrigins;
        private List<String> allowedMethods;
        private List<String> allowedHeaders;
        private List<String> exposedHeaders;
    }

    @Data
    public static class SecurityPathsConfig {
        private List<String> publicPaths;
        private List<String> authPaths;
        private List<String> actuatorPaths;
    }
}
