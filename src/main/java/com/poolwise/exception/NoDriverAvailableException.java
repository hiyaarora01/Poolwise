package com.poolwise.exception;
/** Thrown when no compatible driver can be found for a passenger. */
public class NoDriverAvailableException extends PoolwiseException {
    public NoDriverAvailableException(String passengerId) {
        super("NO_DRIVER", "No compatible driver found for passenger: " + passengerId);
    }
}
