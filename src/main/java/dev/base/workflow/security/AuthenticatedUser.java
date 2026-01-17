package dev.base.workflow.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents an authenticated user in the security context.
 */
@Data
@AllArgsConstructor
public class AuthenticatedUser {
    private String userId;
    private String email;
}
