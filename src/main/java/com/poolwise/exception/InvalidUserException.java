package com.poolwise.exception;
/** Thrown when user registration data is invalid. */
public class InvalidUserException extends PoolwiseException {
    public InvalidUserException(String field, String reason) {
        super("INVALID_USER", "Invalid field '" + field + "': " + reason);
    }
}
