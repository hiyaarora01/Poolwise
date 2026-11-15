package com.poolwise.model;

import java.time.LocalTime;

/**
 * Represents a carpool passenger.
 * Demonstrates: Inheritance, polymorphism via getSummary/getRole.
 */
public class Passenger extends User {

    private String    pickupLocation;
    private double    pickupLat, pickupLon;
    private String    destination;
    private double    destLat,   destLon;
    private LocalTime requiredArrivalTime;
    private double    maxWaitMinutes;
    private double    maxDetourKm;
    private boolean   matched;
    private String    assignedDriverId; // which driver they're matched to

    public Passenger(String userId, String name, String phone, double reliabilityScore,
                     String pickupLocation, double pickupLat, double pickupLon,
                     String destination,   double destLat,   double destLon,
                     LocalTime requiredArrivalTime, double maxWaitMinutes, double maxDetourKm) {
        super(userId, name, phone, reliabilityScore);
        this.pickupLocation       = pickupLocation;
        this.pickupLat            = pickupLat;
        this.pickupLon            = pickupLon;
        this.destination          = destination;
        this.destLat              = destLat;
        this.destLon              = destLon;
        this.requiredArrivalTime  = requiredArrivalTime;
        this.maxWaitMinutes       = maxWaitMinutes;
        this.maxDetourKm          = maxDetourKm;
        this.matched              = false;
        this.assignedDriverId     = null;
    }

    @Override public String getRole() { return "PASSENGER"; }

    @Override
    public String getSummary() {
        return String.format("Passenger %s: pickup %s → %s, arrive by %s, max wait %.0f min",
            getName(), pickupLocation, destination, requiredArrivalTime, maxWaitMinutes);
    }

    // Getters
    public String    getPickupLocation()       { return pickupLocation; }
    public double    getPickupLat()            { return pickupLat; }
    public double    getPickupLon()            { return pickupLon; }
    public String    getDestination()          { return destination; }
    public double    getDestLat()              { return destLat; }
    public double    getDestLon()              { return destLon; }
    public LocalTime getRequiredArrivalTime()  { return requiredArrivalTime; }
    public double    getMaxWaitMinutes()       { return maxWaitMinutes; }
    public double    getMaxDetourKm()          { return maxDetourKm; }
    public boolean   isMatched()               { return matched; }
    public String    getAssignedDriverId()     { return assignedDriverId; }

    // Setters
    public void setPickupLocation(String s)        { this.pickupLocation = s; }
    public void setPickupLat(double v)             { this.pickupLat = v; }
    public void setPickupLon(double v)             { this.pickupLon = v; }
    public void setDestination(String s)           { this.destination = s; }
    public void setDestLat(double v)               { this.destLat = v; }
    public void setDestLon(double v)               { this.destLon = v; }
    public void setRequiredArrivalTime(LocalTime t){ this.requiredArrivalTime = t; }
    public void setMaxWaitMinutes(double w)        { this.maxWaitMinutes = w; }
    public void setMaxDetourKm(double d)           { this.maxDetourKm = d; }
    public void setMatched(boolean m)              { this.matched = m; }
    public void setAssignedDriverId(String id)     { this.assignedDriverId = id; }
    public void clearAssignment()                  { this.matched = false; this.assignedDriverId = null; }

    /** CSV row for file storage */
    public String toCsv() {
        return String.join(",", getUserId(), getName(), getPhone(),
            String.valueOf(getReliabilityScore()),
            pickupLocation, String.valueOf(pickupLat), String.valueOf(pickupLon),
            destination,    String.valueOf(destLat),   String.valueOf(destLon),
            requiredArrivalTime.toString(), String.valueOf(maxWaitMinutes),
            String.valueOf(maxDetourKm), String.valueOf(isAvailable()),
            matched ? (assignedDriverId != null ? assignedDriverId : "") : "");
    }
}
