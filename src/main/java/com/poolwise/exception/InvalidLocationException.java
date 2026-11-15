package com.poolwise.exception;
/** Thrown when location coordinates are outside valid ranges. */
public class InvalidLocationException extends PoolwiseException {
    public InvalidLocationException(String locationName) {
        super("INVALID_LOCATION", "Location '" + locationName + "' has invalid coordinates.");
    }
}
