package poolwise;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CarpoolOptimizer {

    private static final double MIN_SCORE_THRESHOLD = 0.3; // below this, not a good match

    /**
     * Main entry point. Calls the right optimizer based on mode.
     */
    public static CarpoolPlan optimize(List<Driver> drivers, List<Passenger> passengers, OptimizationMode mode) {
        // Filter only available drivers/passengers
        List<Driver> availableDrivers = new ArrayList<>();
        for (Driver d : drivers) if (d.isAvailable()) availableDrivers.add(d);

        List<Passenger> availablePassengers = new ArrayList<>();
        for (Passenger p : passengers) if (p.isAvailable()) availablePassengers.add(p);

        CarpoolPlan plan = new CarpoolPlan(mode.getDisplayName(), availablePassengers.size(), availableDrivers.size());

        switch (mode) {
            case MIN_CARS:
                optimizeForMinCars(availableDrivers, availablePassengers, plan);
                break;
            case MIN_DISTANCE:
                optimizeForMinDistance(availableDrivers, availablePassengers, plan);
                break;
            case MIN_CO2:
                optimizeForMinCO2(availableDrivers, availablePassengers, plan);
                break;
            case MIN_TIME:
                optimizeForMinTime(availableDrivers, availablePassengers, plan);
                break;
            case BALANCED:
            default:
                optimizeBalanced(availableDrivers, availablePassengers, plan);
                break;
        }

        return plan;
    }

    // ─── MIN CARS: pack as many passengers per driver as possible ───
    private static void optimizeForMinCars(List<Driver> drivers, List<Passenger> passengers, CarpoolPlan plan) {
        // Sort drivers by capacity descending
        drivers.sort(Comparator.comparingInt(Driver::getAvailableSeats).reversed());
        greedyAssign(drivers, passengers, plan, Comparator.comparingDouble(CompatibilityScore::getScore).reversed());
    }

    // ─── MIN DISTANCE: pick passengers that add least extra km ───
    private static void optimizeForMinDistance(List<Driver> drivers, List<Passenger> passengers, CarpoolPlan plan) {
        greedyAssign(drivers, passengers, plan,
            Comparator.comparingDouble(CompatibilityScore::getDetourScore).reversed());
    }

    // ─── MIN CO2: same as min distance but weighted by number saved ───
    private static void optimizeForMinCO2(List<Driver> drivers, List<Passenger> passengers, CarpoolPlan plan) {
        // More passengers per car = more CO2 saved
        drivers.sort(Comparator.comparingInt(Driver::getAvailableSeats).reversed());
        greedyAssign(drivers, passengers, plan,
            Comparator.comparingDouble((CompatibilityScore cs) -> 
                cs.getDetourScore() * 0.6 + cs.getRouteOverlapScore() * 0.4).reversed());
    }

    // ─── MIN TIME: prioritize best time-compatibility scores ───
    private static void optimizeForMinTime(List<Driver> drivers, List<Passenger> passengers, CarpoolPlan plan) {
        greedyAssign(drivers, passengers, plan,
            Comparator.comparingDouble(CompatibilityScore::getTimeScore).reversed());
    }

    // ─── BALANCED: overall score ───
    private static void optimizeBalanced(List<Driver> drivers, List<Passenger> passengers, CarpoolPlan plan) {
        greedyAssign(drivers, passengers, plan,
            Comparator.comparingDouble(CompatibilityScore::getScore).reversed());
    }

    /**
     * Core greedy assignment algorithm.
     * Builds CompatibilityMatrix, then greedily assigns passengers to drivers
     * in the order determined by the given Comparator.
     */
    private static void greedyAssign(List<Driver> drivers, List<Passenger> passengers,
                                     CarpoolPlan plan, Comparator<CompatibilityScore> comparator) {
        CompatibilityMatrix matrix = new CompatibilityMatrix(drivers, passengers);

        List<CompatibilityScore> ranked = new ArrayList<>(matrix.getAllScores());
        ranked.sort(comparator);

        Set<String> assignedPassengerIds = new HashSet<>();
        List<CarpoolGroup> groups = new ArrayList<>();

        // Create a group for each driver
        int groupNum = 1;
        for (Driver d : drivers) {
            groups.add(new CarpoolGroup("CP-" + groupNum++, d));
        }

        // Greedily assign passengers to groups
        for (CompatibilityScore cs : ranked) {
            if (cs.getScore() < MIN_SCORE_THRESHOLD) continue;
            String pid = cs.getPassenger().getId();
            if (assignedPassengerIds.contains(pid)) continue;

            // Find the group for this driver
            CarpoolGroup group = null;
            for (CarpoolGroup g : groups) {
                if (g.getDriver().getId().equals(cs.getDriver().getId())) {
                    group = g;
                    break;
                }
            }
            if (group == null || group.isAtCapacity()) continue;

            group.addPassenger(cs.getPassenger());
            assignedPassengerIds.add(pid);
        }

        // Optimize routes and calculate distances for each group
        for (CarpoolGroup group : groups) {
            if (group.getPassengerCount() == 0) continue; // skip empty groups

            List<Location> optimizedRoute = RouteOptimizer.optimizePickupOrder(
                group.getDriver(), group.getPassengers());
            group.setRouteOrder(optimizedRoute);

            double extraDist = RouteOptimizer.calculateExtraDistance(group.getDriver(), optimizedRoute);
            group.setTotalExtraDistanceKm(extraDist);

            plan.addGroup(group);
        }

        // Mark unassigned passengers
        for (Passenger p : passengers) {
            if (!assignedPassengerIds.contains(p.getId())) {
                plan.addUnassignedPassenger(p);
            }
        }
    }
}
