package com.poolwise.exception;
/** Thrown when a vehicle has no available seats. */
public class InsufficientSeatsException extends PoolwiseException {
    public InsufficientSeatsException(String driverId) {
        super("NO_SEATS", "Driver " + driverId + " has no available seats.");
    }
}
