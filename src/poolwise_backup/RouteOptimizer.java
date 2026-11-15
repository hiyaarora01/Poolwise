package poolwise;

import java.util.ArrayList;
import java.util.List;

/**
 * Optimizes the pickup route within a CarpoolGroup.
 * Uses nearest-neighbor greedy algorithm (practical for small groups).
 * For larger groups this approximates Dijkstra's shortest-path principle.
 */
public class RouteOptimizer {

    /**
     * Returns an optimized list of Locations: 
     *   driverStart → [pickup order] → destination
     */
    public static List<Location> optimizePickupOrder(Driver driver, List<Passenger> passengers) {
        List<Location> route = new ArrayList<>();
        route.add(driver.getStartLocation());

        List<Passenger> remaining = new ArrayList<>(passengers);
        Location current = driver.getStartLocation();

        // Nearest-neighbor greedy pickup
        while (!remaining.isEmpty()) {
            Passenger nearest = null;
            double minDist = Double.MAX_VALUE;
            for (Passenger p : remaining) {
                double dist = current.distanceTo(p.getStartLocation());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = p;
                }
            }
            route.add(nearest.getStartLocation());
            current = nearest.getStartLocation();
            remaining.remove(nearest);
        }

        route.add(driver.getDestination());
        return route;
    }

    /**
     * Calculate total distance of a given route.
     */
    public static double calculateRouteDistance(List<Location> route) {
        double total = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            total += route.get(i).distanceTo(route.get(i + 1));
        }
        return total;
    }

    /**
     * Extra distance compared to driver's direct route.
     */
    public static double calculateExtraDistance(Driver driver, List<Location> optimizedRoute) {
        double direct = driver.getStartLocation().distanceTo(driver.getDestination());
        double withPickups = calculateRouteDistance(optimizedRoute);
        return Math.max(0, withPickups - direct);
    }

    /**
     * Display step-by-step route.
     */
    public static void displayRoute(List<Location> route) {
        System.out.print("  Route: ");
        for (int i = 0; i < route.size(); i++) {
            System.out.print(route.get(i).getName());
            if (i < route.size() - 1) System.out.print(" → ");
        }
        System.out.printf("%n  Total distance: %.2f km%n", calculateRouteDistance(route));
    }
}
