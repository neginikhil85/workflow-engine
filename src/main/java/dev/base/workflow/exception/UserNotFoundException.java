package dev.base.workflow.exception;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String userId) {
        super("User not found with ID: " + userId);
    }

    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException("User not found with email: " + email) {
            @Override
            public String getMessage() {
                return "User not found with email: " + email;
            }
        };
    }
}
