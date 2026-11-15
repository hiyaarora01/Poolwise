package com.poolwise.algorithm;

import com.poolwise.model.*;
import com.poolwise.service.MatchingStrategy;
import java.time.Duration;
import java.util.*;

/**
 * Greedy matching algorithm — implements MatchingStrategy.
 *
 * For each passenger (sorted by urgency), finds the best compatible driver
 * and assigns greedily.
 *
 * NOTE: Greedy algorithms do not guarantee a mathematically global optimum;
 * they give a good practical solution efficiently (O(P * D) complexity).
 *
 * Compatibility Score Formula:
 *   score = routeOverlap*0.30 + pickupDist*0.25 + detour*0.20 + time*0.15 + reliability*0.10
 *
 * OOP: Implements interface, uses Collections, demonstrates algorithm design.
 */
public class GreedyMatcher implements MatchingStrategy {

    // Scoring weights — must sum to 1.0
    public static double W_ROUTE       = 0.30;
    public static double W_PICKUP      = 0.25;
    public static double W_DETOUR      = 0.20;
    public static double W_TIME        = 0.15;
    public static double W_RELIABILITY = 0.10;

    private static final double MIN_SCORE        = 0.20; // reject below this
    private static final double MAX_PICKUP_KM    = 8.0;  // full score within 8 km
    private static final double AVG_SPEED_KMH    = 40.0; // used for time estimation

    @Override
    public List<Carpool> match(List<Driver> drivers, List<Passenger> passengers,
                               String mode, int carpoolIdStart) {
        List<Carpool> result = new ArrayList<>();
        Set<String>   assignedPassengerIds = new HashSet<>();
        int           idCounter            = carpoolIdStart;

        // Sort passengers by urgency: earliest required arrival first
        List<Passenger> sorted = new ArrayList<>(passengers);
        sorted.sort(Comparator.comparing(Passenger::getRequiredArrivalTime));

        // Filter and sort available drivers
        List<Driver> available = new ArrayList<>();
        for (Driver d : drivers)
            if (d.isAvailable() && d.hasSeats()) available.add(d);

        if ("MIN_CARS".equals(mode))
            // Prioritise drivers with most seats to maximise fill
            available.sort(Comparator.comparingInt(Driver::getAvailableSeats).reversed());

        // Create one Carpool slot per available driver
        Map<String, Carpool> driverSlot = new LinkedHashMap<>();
        for (Driver d : available)
            driverSlot.put(d.getUserId(), new Carpool("CP-" + (idCounter++), d, mode));

        // Greedy assignment
        for (Passenger p : sorted) {
            if (!p.isAvailable() || assignedPassengerIds.contains(p.getUserId())) continue;

            Driver  bestDriver  = null;
            Carpool bestCarpool = null;
            double  bestScore   = -1;

            for (Driver d : available) {
                if (!d.isAvailable() || !d.hasSeats()) continue;
                double score = computeScore(d, p);
                if (score >= MIN_SCORE && score > bestScore) {
                    bestScore   = score;
                    bestDriver  = d;
                    bestCarpool = driverSlot.get(d.getUserId());
                }
            }

            if (bestDriver != null && bestCarpool != null) {
                bestCarpool.addPassenger(p);
                // Running average of compatibility score
                int n = bestCarpool.getPassengerCount();
                bestCarpool.setCompatibilityScore(
                    (bestCarpool.getCompatibilityScore() * (n - 1) + bestScore) / n);
                assignedPassengerIds.add(p.getUserId());
            }
        }

        // Only return non-empty carpools
        for (Carpool cp : driverSlot.values())
            if (cp.getPassengerCount() > 0) result.add(cp);

        return result;
    }

    @Override
    public double computeScore(Driver d, Passenger p) {
        return routeOverlap(d, p)   * W_ROUTE
             + pickupDist(d, p)     * W_PICKUP
             + detourScore(d, p)    * W_DETOUR
             + timeScore(d, p)      * W_TIME
             + reliabilityScore(d,p)* W_RELIABILITY;
    }

    // ── Sub-scores [0.0 – 1.0] ───────────────────────────────────

    private double routeOverlap(Driver d, Passenger p) {
        double dLat = d.getDestLat()  - d.getStartLat();
        double dLon = d.getDestLon()  - d.getStartLon();
        double pLat = p.getDestLat()  - p.getPickupLat();
        double pLon = p.getDestLon()  - p.getPickupLon();
        double dot  = dLat*pLat + dLon*pLon;
        double mD   = Math.sqrt(dLat*dLat + dLon*dLon);
        double mP   = Math.sqrt(pLat*pLat + pLon*pLon);
        if (mD == 0 || mP == 0) return 0.5;
        return (Math.max(-1, Math.min(1, dot/(mD*mP))) + 1.0) / 2.0;
    }

    private double pickupDist(Driver d, Passenger p) {
        double dist = Driver.haversine(d.getStartLat(), d.getStartLon(),
                                       p.getPickupLat(), p.getPickupLon());
        return Math.max(0, 1.0 - dist / MAX_PICKUP_KM);
    }

    private double detourScore(Driver d, Passenger p) {
        double direct = d.directDistanceKm();
        double via    = Driver.haversine(d.getStartLat(), d.getStartLon(), p.getPickupLat(), p.getPickupLon())
                      + Driver.haversine(p.getPickupLat(), p.getPickupLon(), d.getDestLat(), d.getDestLon());
        double extra  = via - direct;
        if (d.getMaxDetourKm() <= 0) return 0;
        return Math.max(0, 1.0 - extra / d.getMaxDetourKm());
    }

    private double timeScore(Driver d, Passenger p) {
        double km      = d.directDistanceKm();
        long   mins    = (long)(km / AVG_SPEED_KMH * 60);
        var    est     = d.getDepartureTime().plusMinutes(mins);
        long   diff    = Duration.between(est, p.getRequiredArrivalTime()).toMinutes();
        if (diff >= 0) return 1.0;
        return Math.max(0, 1.0 + diff / 45.0);
    }

    private double reliabilityScore(Driver d, Passenger p) {
        return (d.getReliabilityScore() + p.getReliabilityScore()) / 2.0;
    }

    // ── Public score breakdown (for UI matrix display) ────────────
    public double[] scoreBreakdown(Driver d, Passenger p) {
        return new double[]{
            routeOverlap(d,p), pickupDist(d,p), detourScore(d,p),
            timeScore(d,p), reliabilityScore(d,p), computeScore(d,p)
        };
    }
}
