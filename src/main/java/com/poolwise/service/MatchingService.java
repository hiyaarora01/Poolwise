package com.poolwise.service;

import com.poolwise.algorithm.GreedyMatcher;
import com.poolwise.model.*;
import java.util.*;

/**
 * Orchestrates the full carpool matching pipeline.
 * Uses MatchingStrategy (pluggable) and RouteService for route optimization.
 * OOP: Strategy pattern, service orchestration, encapsulation.
 */
public class MatchingService {

    private MatchingStrategy matchingStrategy;
    private final RouteService     routeService;
    private List<Carpool>    lastCarpools;
    private int              carpoolIdCounter = 1;

    public MatchingService(RouteService routeService) {
        this.matchingStrategy = new GreedyMatcher();
        this.routeService     = routeService;
        this.lastCarpools     = new ArrayList<>();
    }

    /**
     * Full matching pipeline:
     * 1. Clear previous matches
     * 2. Run greedy matcher
     * 3. Optimize pickup routes
     * 4. Calculate extra distances
     */
    public List<Carpool> runMatching(UserService userService, String mode) {
        userService.clearAllMatches();

        List<Driver>    drivers    = userService.getAllDrivers();
        List<Passenger> passengers = userService.getAllPassengers();

        lastCarpools = matchingStrategy.match(drivers, passengers, mode, carpoolIdCounter);
        carpoolIdCounter += lastCarpools.size();

        for (Carpool cp : lastCarpools) {
            List<String> route = routeService.optimizePickupOrder(cp.getDriver(), cp.getPassengers());
            cp.setRouteOrder(route);
            double routeKm  = routeService.calculateRouteDistance(route);
            if (routeKm == Double.MAX_VALUE) routeKm = estimateRouteKm(cp);
            cp.setTotalRouteKm(routeKm);
            cp.setExtraDistanceKm(Math.max(0, routeKm - cp.getDriver().directDistanceKm()));
        }

        return Collections.unmodifiableList(lastCarpools);
    }

    /** Fallback: estimate route km using haversine when graph path not found. */
    private double estimateRouteKm(Carpool cp) {
        List<String> route = cp.getRouteOrder();
        if (route == null || route.size() < 2) return cp.getDriver().directDistanceKm();
        return cp.getDriver().directDistanceKm() + 2.0 * cp.getPassengerCount();
    }

    public List<Carpool>    getLastCarpools()    { return lastCarpools; }
    public void setMatchingStrategy(MatchingStrategy s) { this.matchingStrategy = s; }

    public double computeScore(Driver d, Passenger p) {
        return matchingStrategy.computeScore(d, p);
    }

    public double[] scoreBreakdown(Driver d, Passenger p) {
        if (matchingStrategy instanceof GreedyMatcher gm) return gm.scoreBreakdown(d, p);
        return new double[]{0,0,0,0,0, matchingStrategy.computeScore(d,p)};
    }
}
