package dev.base.workflow.constant;

/**
 * Security and authentication related constants.
 */
public final class SecurityConstants {

    private SecurityConstants() {
    }

    // OAuth2 Providers
    public static final String PROVIDER_GOOGLE = "google";
    public static final String PROVIDER_GITHUB = "github";

    // OAuth2 Attributes
    public static final String ATTR_EMAIL = "email";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_LOGIN = "login";

    // GitHub fallback email domain
    public static final String GITHUB_EMAIL_FALLBACK_DOMAIN = "@github.user";

    // Auth paths
    public static final String AUTH_CALLBACK_PATH = "/auth/callback?token=";

    // Public endpoint patterns
    public static final String[] PUBLIC_PATHS = {
            "/", "/error", "/favicon.ico"
    };

    public static final String[] AUTH_PATHS = {
            "/auth/**", "/oauth2/**", "/login/**"
    };

    public static final String[] ACTUATOR_PATHS = {
            "/actuator/**"
    };
}
