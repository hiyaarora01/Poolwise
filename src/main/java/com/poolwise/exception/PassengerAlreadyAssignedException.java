package com.poolwise.exception;
/** Thrown when attempting to assign an already-matched passenger. */
public class PassengerAlreadyAssignedException extends PoolwiseException {
    public PassengerAlreadyAssignedException(String passengerId, String driverId) {
        super("ALREADY_ASSIGNED",
            "Passenger " + passengerId + " is already assigned to driver " + driverId);
    }
}
