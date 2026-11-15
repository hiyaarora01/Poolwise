package com.poolwise.service;

import com.poolwise.exception.*;
import com.poolwise.model.*;
import java.util.*;

/**
 * Service layer for managing drivers and passengers.
 * OOP: Collections (ArrayList, HashMap, HashSet), validation, service pattern.
 */
public class UserService {

    private final Map<String, Driver>    driverMap    = new LinkedHashMap<>();
    private final Map<String, Passenger> passengerMap = new LinkedHashMap<>();
    private final Set<String>            usedIds      = new HashSet<>();
    private int driverCounter    = 1;
    private int passengerCounter = 1;

    // ── Driver CRUD ───────────────────────────────────────────────

    public void addDriver(Driver d) throws InvalidUserException {
        validateDriver(d);
        if (usedIds.contains(d.getUserId()))
            throw new InvalidUserException("userId", "ID already exists: " + d.getUserId());
        driverMap.put(d.getUserId(), d);
        usedIds.add(d.getUserId());
    }

    public void updateDriver(Driver d) throws InvalidUserException {
        if (!driverMap.containsKey(d.getUserId()))
            throw new InvalidUserException("userId", "Driver not found: " + d.getUserId());
        validateDriver(d);
        driverMap.put(d.getUserId(), d);
    }

    public boolean removeDriver(String id) {
        Driver d = driverMap.remove(id);
        if (d != null) { usedIds.remove(id); return true; }
        return false;
    }

    public Optional<Driver> findDriver(String id) { return Optional.ofNullable(driverMap.get(id)); }
    public List<Driver>     getAllDrivers()        { return new ArrayList<>(driverMap.values()); }
    public int              driverCount()          { return driverMap.size(); }

    public List<Driver> getAvailableDrivers() {
        List<Driver> r = new ArrayList<>();
        for (Driver d : driverMap.values()) if (d.isAvailable() && d.hasSeats()) r.add(d);
        return r;
    }

    // ── Passenger CRUD ────────────────────────────────────────────

    public void addPassenger(Passenger p) throws InvalidUserException {
        validatePassenger(p);
        if (usedIds.contains(p.getUserId()))
            throw new InvalidUserException("userId", "ID already exists: " + p.getUserId());
        passengerMap.put(p.getUserId(), p);
        usedIds.add(p.getUserId());
    }

    public void updatePassenger(Passenger p) throws InvalidUserException {
        if (!passengerMap.containsKey(p.getUserId()))
            throw new InvalidUserException("userId", "Passenger not found: " + p.getUserId());
        validatePassenger(p);
        passengerMap.put(p.getUserId(), p);
    }

    public boolean removePassenger(String id) {
        Passenger p = passengerMap.remove(id);
        if (p != null) { usedIds.remove(id); return true; }
        return false;
    }

    public Optional<Passenger> findPassenger(String id) { return Optional.ofNullable(passengerMap.get(id)); }
    public List<Passenger>     getAllPassengers()        { return new ArrayList<>(passengerMap.values()); }
    public int                 passengerCount()         { return passengerMap.size(); }

    public List<Passenger> getUnmatchedPassengers() {
        List<Passenger> r = new ArrayList<>();
        for (Passenger p : passengerMap.values()) if (p.isAvailable() && !p.isMatched()) r.add(p);
        return r;
    }

    public int countMatched()          { return (int) passengerMap.values().stream().filter(Passenger::isMatched).count(); }
    public int countAvailableVehicles(){ return (int) driverMap.values().stream().filter(d -> d.isAvailable() && d.hasSeats()).count(); }

    // ── ID generation ─────────────────────────────────────────────
    public String nextDriverId()    { return "D" + String.format("%03d", driverCounter++); }
    public String nextPassengerId() { return "P" + String.format("%03d", passengerCounter++); }

    /** Reset all matches so the optimizer can run fresh. */
    public void clearAllMatches() {
        for (Passenger p : passengerMap.values()) p.clearAssignment();
        for (Driver    d : driverMap.values())    d.setAvailableSeats(d.getTotalSeats());
    }

    /** Replace all stored data (called after loading from file). */
    public void loadAll(List<Driver> drivers, List<Passenger> passengers) {
        driverMap.clear(); passengerMap.clear(); usedIds.clear();
        for (Driver    d : drivers)    { driverMap.put(d.getUserId(), d);    usedIds.add(d.getUserId()); }
        for (Passenger p : passengers) { passengerMap.put(p.getUserId(), p); usedIds.add(p.getUserId()); }
        driverCounter    = drivers.stream().mapToInt(d -> parseIdNum(d.getUserId())).max().orElse(0) + 1;
        passengerCounter = passengers.stream().mapToInt(p -> parseIdNum(p.getUserId())).max().orElse(0) + 1;
    }

    private int parseIdNum(String id) {
        try { return Integer.parseInt(id.substring(1)); } catch (Exception e) { return 0; }
    }

    // ── Validation ────────────────────────────────────────────────

    private void validateDriver(Driver d) throws InvalidUserException {
        if (d.getUserId()  == null || d.getUserId().isBlank())  throw new InvalidUserException("userId",    "cannot be empty");
        if (d.getName()    == null || d.getName().isBlank())    throw new InvalidUserException("name",      "cannot be empty");
        if (d.getPhone()   == null || !d.getPhone().matches("\\d{10}")) throw new InvalidUserException("phone", "must be 10 digits");
        if (d.getTotalSeats() < 1 || d.getTotalSeats() > 8)    throw new InvalidUserException("seats",     "must be 1–8");
        if (d.getMaxDetourKm() < 0)                             throw new InvalidUserException("maxDetour", "cannot be negative");
        if (d.getDepartureTime() == null)                       throw new InvalidUserException("departureTime", "required");
        validateCoords(d.getStartLat(), d.getStartLon(), "startLocation");
        validateCoords(d.getDestLat(),  d.getDestLon(),  "destination");
    }

    private void validatePassenger(Passenger p) throws InvalidUserException {
        if (p.getUserId() == null || p.getUserId().isBlank())   throw new InvalidUserException("userId",    "cannot be empty");
        if (p.getName()   == null || p.getName().isBlank())     throw new InvalidUserException("name",      "cannot be empty");
        if (p.getPhone()  == null || !p.getPhone().matches("\\d{10}")) throw new InvalidUserException("phone", "must be 10 digits");
        if (p.getMaxWaitMinutes() < 0)                          throw new InvalidUserException("maxWait",   "cannot be negative");
        if (p.getMaxDetourKm()    < 0)                          throw new InvalidUserException("maxDetour", "cannot be negative");
        if (p.getRequiredArrivalTime() == null)                 throw new InvalidUserException("arrivalTime", "required");
        validateCoords(p.getPickupLat(), p.getPickupLon(), "pickupLocation");
        validateCoords(p.getDestLat(),   p.getDestLon(),   "destination");
    }

    private void validateCoords(double lat, double lon, String field) throws InvalidUserException {
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180)
            throw new InvalidUserException(field, "coordinates out of range");
    }
}
