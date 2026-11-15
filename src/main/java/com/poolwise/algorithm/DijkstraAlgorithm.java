package com.poolwise.algorithm;

import java.util.*;

/**
 * Dijkstra's shortest-path algorithm using a PriorityQueue.
 * Time complexity: O((V + E) log V)
 * OOP: PriorityQueue, HashMap, algorithm design.
 *
 * NOTE: Dijkstra works only on non-negative edge weights (guaranteed here — distances ≥ 0).
 */
public class DijkstraAlgorithm {

    /** Result returned by a Dijkstra run. */
    public static class Result {
        public final double       totalDistance; // km
        public final List<String> path;          // ordered node names
        public final boolean      reachable;

        public Result(double totalDistance, List<String> path, boolean reachable) {
            this.totalDistance = totalDistance;
            this.path          = Collections.unmodifiableList(path);
            this.reachable     = reachable;
        }
    }

    /** Internal priority-queue entry. */
    private static class NodeEntry implements Comparable<NodeEntry> {
        final String node; final double cost;
        NodeEntry(String node, double cost) { this.node = node; this.cost = cost; }
        @Override public int compareTo(NodeEntry o) { return Double.compare(cost, o.cost); }
    }

    /**
     * Find shortest path from {@code source} to {@code target} in {@code graph}.
     */
    public static Result shortestPath(Graph graph, String source, String target) {
        if (!graph.hasNode(source) || !graph.hasNode(target))
            return new Result(Double.MAX_VALUE, new ArrayList<>(), false);
        if (source.equals(target))
            return new Result(0.0, List.of(source), true);

        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        for (String n : graph.getAllNodes()) dist.put(n, Double.MAX_VALUE);
        dist.put(source, 0.0);

        PriorityQueue<NodeEntry> pq = new PriorityQueue<>();
        pq.offer(new NodeEntry(source, 0.0));
        Set<String> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            NodeEntry cur = pq.poll();
            if (visited.contains(cur.node)) continue;
            visited.add(cur.node);
            if (cur.node.equals(target)) break;

            for (Graph.Edge edge : graph.getNeighbours(cur.node)) {
                if (visited.contains(edge.node)) continue;
                double newDist = dist.get(cur.node) + edge.weight;
                if (newDist < dist.getOrDefault(edge.node, Double.MAX_VALUE)) {
                    dist.put(edge.node, newDist);
                    prev.put(edge.node, cur.node);
                    pq.offer(new NodeEntry(edge.node, newDist));
                }
            }
        }

        double total = dist.getOrDefault(target, Double.MAX_VALUE);
        if (total == Double.MAX_VALUE)
            return new Result(Double.MAX_VALUE, new ArrayList<>(), false);

        // Reconstruct path
        List<String> path = new ArrayList<>();
        for (String at = target; at != null; at = prev.get(at)) path.add(at);
        Collections.reverse(path);
        return new Result(total, path, true);
    }

    /** Sum of shortest paths between consecutive nodes in a list. */
    public static double pathDistance(Graph graph, List<String> nodes) {
        if (nodes == null || nodes.size() < 2) return 0.0;
        double total = 0.0;
        for (int i = 0; i < nodes.size() - 1; i++) {
            Result r = shortestPath(graph, nodes.get(i), nodes.get(i + 1));
            if (!r.reachable) return Double.MAX_VALUE;
            total += r.totalDistance;
        }
        return total;
    }
}
