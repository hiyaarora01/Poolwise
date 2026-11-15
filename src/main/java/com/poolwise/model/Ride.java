package com.poolwise.model;

import java.time.LocalDateTime;

/**
 * Represents a ride request or completed ride record.
 * Demonstrates: Association between Passenger and Carpool.
 */
public class Ride {
    public enum Status { PENDING, MATCHED, IN_PROGRESS, COMPLETED, CANCELLED }

    private String        rideId;
    private Passenger     passenger;    // Association
    private Carpool       carpool;      // Association (nullable until matched)
    private Status        status;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private double        fareEstimate;

    public Ride(String rideId, Passenger passenger) {
        this.rideId      = rideId;
        this.passenger   = passenger;
        this.status      = Status.PENDING;
        this.requestedAt = LocalDateTime.now();
    }

    public String        getRideId()      { return rideId; }
    public Passenger     getPassenger()   { return passenger; }
    public Carpool       getCarpool()     { return carpool; }
    public Status        getStatus()      { return status; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public double        getFareEstimate(){ return fareEstimate; }

    public void setCarpool(Carpool c)        { this.carpool = c; this.status = Status.MATCHED; }
    public void setStatus(Status s)          { this.status = s; }
    public void setCompletedAt(LocalDateTime t){ this.completedAt = t; }
    public void setFareEstimate(double f)    { this.fareEstimate = f; }

    @Override
    public String toString() {
        return String.format("Ride[%s] Passenger=%s Status=%s", rideId, passenger.getName(), status);
    }
}
