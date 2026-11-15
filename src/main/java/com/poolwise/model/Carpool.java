package com.poolwise.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents one carpool group.
 * Demonstrates: Composition — owns its passenger list and manages their lifecycle.
 */
public class Carpool {
    private static final double CO2_KG_PER_KM   = 0.21;  // kg CO2 per km per car
    private static final double FUEL_L_PER_KM   = 0.08;  // litres per km

    private String       carpoolId;
    private Driver       driver;
    private List<Passenger> passengers; // Composition
    private List<String> routeOrder;    // ordered location names
    private double       totalRouteKm;
    private double       extraDistanceKm;
    private double       compatibilityScore;
    private String       optimizationMode;
    private LocalDateTime createdAt;
    private boolean      active;

    public Carpool(String carpoolId, Driver driver, String optimizationMode) {
        this.carpoolId         = carpoolId;
        this.driver            = driver;
        this.passengers        = new ArrayList<>();
        this.routeOrder        = new ArrayList<>();
        this.optimizationMode  = optimizationMode;
        this.createdAt         = LocalDateTime.now();
        this.active            = true;
    }

    // ── Passenger management (Composition lifecycle) ──────────────
    public void addPassenger(Passenger p) {
        if (!passengers.contains(p)) {
            passengers.add(p);
            p.setMatched(true);
            p.setAssignedDriverId(driver.getUserId());
            driver.decrementSeat();
        }
    }

    public void removePassenger(Passenger p) {
        if (passengers.remove(p)) {
            p.clearAssignment();
            driver.incrementSeat();
        }
    }

    // ── Getters ───────────────────────────────────────────────────
    public String          getCarpoolId()         { return carpoolId; }
    public Driver          getDriver()             { return driver; }
    public List<Passenger> getPassengers()         { return Collections.unmodifiableList(passengers); }
    public List<String>    getRouteOrder()         { return routeOrder; }
    public double          getTotalRouteKm()       { return totalRouteKm; }
    public double          getExtraDistanceKm()    { return extraDistanceKm; }
    public double          getCompatibilityScore() { return compatibilityScore; }
    public String          getOptimizationMode()   { return optimizationMode; }
    public LocalDateTime   getCreatedAt()          { return createdAt; }
    public boolean         isActive()              { return active; }
    public int             getPassengerCount()     { return passengers.size(); }

    // ── Setters ───────────────────────────────────────────────────
    public void setRouteOrder(List<String> r)       { this.routeOrder = new ArrayList<>(r); }
    public void setTotalRouteKm(double km)          { this.totalRouteKm = km; }
    public void setExtraDistanceKm(double km)       { this.extraDistanceKm = km; }
    public void setCompatibilityScore(double score) { this.compatibilityScore = score; }
    public void setActive(boolean a)                { this.active = a; }

    // ── Environmental calculations ─────────────────────────────────
    public double getEstimatedCO2SavedKg() {
        double direct = driver.directDistanceKm();
        int carsAvoided = passengers.size();
        return carsAvoided * direct * CO2_KG_PER_KM - extraDistanceKm * CO2_KG_PER_KM;
    }

    public double getEstimatedFuelSavedL() {
        double direct = driver.directDistanceKm();
        int carsAvoided = passengers.size();
        return carsAvoided * direct * FUEL_L_PER_KM - extraDistanceKm * FUEL_L_PER_KM;
    }

    public int getCarsAvoided() { return passengers.size(); }

    public String getRouteString() {
        return String.join(" → ", routeOrder);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CARPOOL [").append(carpoolId).append("]\n");
        sb.append("  Driver    : ").append(driver.getName()).append("\n");
        sb.append("  Passengers: ");
        passengers.forEach(p -> sb.append(p.getName()).append(", "));
        sb.append("\n  Extra km  : ").append(String.format("%.2f", extraDistanceKm)).append("\n");
        sb.append("  CO2 Saved : ").append(String.format("%.2f kg", getEstimatedCO2SavedKg())).append("\n");
        sb.append("  Route     : ").append(getRouteString());
        return sb.toString();
    }
}
