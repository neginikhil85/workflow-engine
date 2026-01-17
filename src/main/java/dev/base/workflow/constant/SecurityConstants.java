package dev.base.workflow.constant;

/**
 * Truly immutable security constants.
 * Configurable values should be in application.yml via AppConfig.
 */
public final class SecurityConstants {

        private SecurityConstants() {
        }

        // OAuth2 Provider identifiers (immutable - part of OAuth2 spec)
        public static final String PROVIDER_GOOGLE = "google";
        public static final String PROVIDER_GITHUB = "github";

        // OAuth2 Attribute keys (immutable - defined by providers)
        public static final String ATTR_EMAIL = "email";
        public static final String ATTR_NAME = "name";
        public static final String ATTR_LOGIN = "login";

        // GitHub fallback email domain (business constant)
        public static final String GITHUB_EMAIL_FALLBACK_DOMAIN = "@github.user";
}
