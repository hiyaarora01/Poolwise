package com.poolwise.model;

import java.time.LocalTime;

/**
 * Represents a carpool driver.
 * Demonstrates: Inheritance, Encapsulation, Aggregation (has a Vehicle).
 */
public class Driver extends User {

    private String  startLocation;
    private double  startLat, startLon;
    private String  destination;
    private double  destLat,  destLon;
    private LocalTime departureTime;
    private int     totalSeats;
    private int     availableSeats;
    private double  maxDetourKm;
    private Vehicle vehicle; // Aggregation

    public Driver(String userId, String name, String phone, double reliabilityScore,
                  String startLocation, double startLat, double startLon,
                  String destination,   double destLat,  double destLon,
                  LocalTime departureTime, int totalSeats, double maxDetourKm,
                  Vehicle vehicle) {
        super(userId, name, phone, reliabilityScore);
        this.startLocation  = startLocation;
        this.startLat       = startLat;
        this.startLon       = startLon;
        this.destination    = destination;
        this.destLat        = destLat;
        this.destLon        = destLon;
        this.departureTime  = departureTime;
        this.totalSeats     = totalSeats;
        this.availableSeats = totalSeats;
        this.maxDetourKm    = maxDetourKm;
        this.vehicle        = vehicle;
    }

    @Override public String getRole() { return "DRIVER"; }

    @Override
    public String getSummary() {
        return String.format("Driver %s: %s → %s, departs %s, %d/%d seats, max detour %.1f km",
            getName(), startLocation, destination, departureTime, availableSeats, totalSeats, maxDetourKm);
    }

    // Getters
    public String    getStartLocation()  { return startLocation; }
    public double    getStartLat()       { return startLat; }
    public double    getStartLon()       { return startLon; }
    public String    getDestination()    { return destination; }
    public double    getDestLat()        { return destLat; }
    public double    getDestLon()        { return destLon; }
    public LocalTime getDepartureTime()  { return departureTime; }
    public int       getTotalSeats()     { return totalSeats; }
    public int       getAvailableSeats() { return availableSeats; }
    public double    getMaxDetourKm()    { return maxDetourKm; }
    public Vehicle   getVehicle()        { return vehicle; }

    // Setters
    public void setStartLocation(String s){ this.startLocation = s; }
    public void setStartLat(double v)     { this.startLat = v; }
    public void setStartLon(double v)     { this.startLon = v; }
    public void setDestination(String s)  { this.destination = s; }
    public void setDestLat(double v)      { this.destLat = v; }
    public void setDestLon(double v)      { this.destLon = v; }
    public void setDepartureTime(LocalTime t){ this.departureTime = t; }
    public void setTotalSeats(int s)      { this.totalSeats = s; this.availableSeats = Math.min(availableSeats,s); }
    public void setAvailableSeats(int s)  { this.availableSeats = s; }
    public void setMaxDetourKm(double d)  { this.maxDetourKm = d; }
    public void setVehicle(Vehicle v)     { this.vehicle = v; }

    public boolean hasSeats()             { return availableSeats > 0; }
    public void decrementSeat()           { if (availableSeats > 0) availableSeats--; }
    public void incrementSeat()           { if (availableSeats < totalSeats) availableSeats++; }
    public double directDistanceKm() {
        return haversine(startLat, startLon, destLat, destLon);
    }

    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                 + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon/2)*Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    /** CSV row for file storage */
    public String toCsv() {
        String veh = vehicle != null ? vehicle.toCsv() : ",,";
        return String.join(",", getUserId(), getName(), getPhone(),
            String.valueOf(getReliabilityScore()),
            startLocation, String.valueOf(startLat), String.valueOf(startLon),
            destination,   String.valueOf(destLat),  String.valueOf(destLon),
            departureTime.toString(), String.valueOf(totalSeats), String.valueOf(maxDetourKm),
            String.valueOf(isAvailable()), veh);
    }
}
