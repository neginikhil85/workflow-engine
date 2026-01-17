package dev.base.workflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Application-wide configuration properties.
 * Grouped configs loaded from application.yml under 'app' prefix.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Frontend frontend = new Frontend();
    private Cors cors = new Cors();
    private Security security = new Security();

    @Data
    public static class Jwt {
        private String secret = "workflow-studio-secret-key-change-in-production-must-be-256-bits-long";
        private long expiration = 86400000; // 24 hours
    }

    @Data
    public static class Frontend {
        private String url = "http://localhost:5173";
        private String authCallbackPath = "/auth/callback?token=";
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:5173", "http://localhost:3000");
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
        private List<String> allowedHeaders = List.of("*");
        private List<String> exposedHeaders = List.of("Authorization");
    }

    @Data
    public static class Security {
        private List<String> publicPaths = List.of("/", "/error", "/favicon.ico");
        private List<String> authPaths = List.of("/auth/**", "/oauth2/**", "/login/**");
        private List<String> actuatorPaths = List.of("/actuator/**");
    }
}
