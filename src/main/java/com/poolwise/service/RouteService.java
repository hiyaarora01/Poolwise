package com.poolwise.service;

import com.poolwise.algorithm.*;
import com.poolwise.model.*;
import java.util.*;

/**
 * Route optimization service. Implements RouteOptimizerInterface.
 * Uses Dijkstra on a predefined Delhi road-network graph.
 * For small groups (≤4), evaluates all permutations for best order.
 * OOP: Implements interface, uses algorithm classes, Graph data structure.
 */
public class RouteService implements RouteOptimizerInterface {

    private final Graph roadNetwork;

    public RouteService() {
        this.roadNetwork = buildDelhiNetwork();
    }

    @Override
    public List<String> optimizeRoute(Driver driver, List<Passenger> passengers) {
        return optimizePickupOrder(driver, passengers);
    }

    @Override
    public double calculateRouteDistance(List<String> routeNodes) {
        return DijkstraAlgorithm.pathDistance(roadNetwork, routeNodes);
    }

    public List<String> optimizePickupOrder(Driver driver, List<Passenger> passengers) {
        if (passengers.isEmpty())
            return List.of(driver.getStartLocation(), driver.getDestination());

        List<String> pickups = new ArrayList<>();
        for (Passenger p : passengers) pickups.add(p.getPickupLocation());

        List<String> bestOrder;
        if (passengers.size() <= 4) {
            // Exhaustive permutation search for small groups
            bestOrder = nearestNeighbour(driver.getStartLocation(), pickups);
            double bestDist = routeDist(driver.getStartLocation(), bestOrder, driver.getDestination());
            for (List<String> perm : permutations(pickups)) {
                double d = routeDist(driver.getStartLocation(), perm, driver.getDestination());
                if (d < bestDist) { bestDist = d; bestOrder = perm; }
            }
        } else {
            bestOrder = nearestNeighbour(driver.getStartLocation(), pickups);
        }

        List<String> route = new ArrayList<>();
        route.add(driver.getStartLocation());
        route.addAll(bestOrder);
        route.add(driver.getDestination());
        return route;
    }

    public DijkstraAlgorithm.Result shortestPath(String from, String to) {
        return DijkstraAlgorithm.shortestPath(roadNetwork, from, to);
    }

    public Graph getRoadNetwork() { return roadNetwork; }

    // ── Helpers ───────────────────────────────────────────────────

    private List<String> nearestNeighbour(String start, List<String> nodes) {
        List<String> remaining = new ArrayList<>(nodes), ordered = new ArrayList<>();
        String cur = start;
        while (!remaining.isEmpty()) {
            String nearest = null; double minD = Double.MAX_VALUE;
            for (String n : remaining) {
                double d = DijkstraAlgorithm.shortestPath(roadNetwork, cur, n).totalDistance;
                if (d < minD) { minD = d; nearest = n; }
            }
            ordered.add(nearest); remaining.remove(nearest); cur = nearest;
        }
        return ordered;
    }

    private double routeDist(String start, List<String> stops, String end) {
        List<String> full = new ArrayList<>();
        full.add(start); full.addAll(stops); full.add(end);
        return DijkstraAlgorithm.pathDistance(roadNetwork, full);
    }

    private List<List<String>> permutations(List<String> items) {
        List<List<String>> result = new ArrayList<>();
        permute(new ArrayList<>(items), 0, result);
        return result;
    }

    private void permute(List<String> arr, int start, List<List<String>> out) {
        if (start == arr.size()) { out.add(new ArrayList<>(arr)); return; }
        for (int i = start; i < arr.size(); i++) {
            Collections.swap(arr, start, i);
            permute(arr, start + 1, out);
            Collections.swap(arr, start, i);
        }
    }

    // ── Delhi road network ────────────────────────────────────────
    private Graph buildDelhiNetwork() {
        Graph g = new Graph();
        g.addEdge("Connaught Place",  "Karol Bagh",       3.5);
        g.addEdge("Connaught Place",  "Kashmiri Gate",    4.2);
        g.addEdge("Connaught Place",  "Lajpat Nagar",     8.5);
        g.addEdge("Connaught Place",  "IIT Delhi",       12.0);
        g.addEdge("Connaught Place",  "AIIMS",            9.5);
        g.addEdge("Connaught Place",  "Dwarka",          18.0);
        g.addEdge("Connaught Place",  "Vasant Kunj",     14.5);
        g.addEdge("Connaught Place",  "Rajouri Garden",   7.5);
        g.addEdge("Karol Bagh",       "Rajouri Garden",   5.5);
        g.addEdge("Karol Bagh",       "Dwarka",          16.0);
        g.addEdge("Karol Bagh",       "IIT Delhi",       10.0);
        g.addEdge("Kashmiri Gate",    "Lajpat Nagar",    11.0);
        g.addEdge("Lajpat Nagar",     "Saket",            5.0);
        g.addEdge("Lajpat Nagar",     "AIIMS",            4.5);
        g.addEdge("Lajpat Nagar",     "IIT Delhi",        7.5);
        g.addEdge("AIIMS",            "IIT Delhi",        3.0);
        g.addEdge("AIIMS",            "Vasant Kunj",      6.5);
        g.addEdge("AIIMS",            "Saket",            6.5);
        g.addEdge("Saket",            "IIT Delhi",        5.5);
        g.addEdge("Saket",            "Vasant Kunj",      4.5);
        g.addEdge("Vasant Kunj",      "IIT Delhi",        4.0);
        g.addEdge("Vasant Kunj",      "Dwarka",          12.0);
        g.addEdge("Dwarka",           "Rajouri Garden",  11.0);
        g.addEdge("Dwarka",           "IIT Delhi",       16.5);
        g.addEdge("Rajouri Garden",   "IIT Delhi",       11.0);
        g.addEdge("Ghaziabad",        "Kashmiri Gate",   16.0);
        g.addEdge("Ghaziabad",        "Lajpat Nagar",    24.0);
        g.addEdge("Ghaziabad",        "Connaught Place", 19.0);
        return g;
    }
}
