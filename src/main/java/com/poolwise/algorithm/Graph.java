package com.poolwise.algorithm;

import java.util.*;

/**
 * Weighted undirected graph using adjacency list.
 * Used for the Delhi road network in route optimization.
 * OOP: Collections (HashMap, ArrayList), Graph data structure.
 */
public class Graph {

    /** An edge connecting to a node with distance (km). */
    public static class Edge {
        public final String node;
        public final double weight;
        public Edge(String node, double weight) { this.node = node; this.weight = weight; }
    }

    private final Map<String, List<Edge>> adjacencyList = new HashMap<>();

    public void addNode(String node) {
        adjacencyList.putIfAbsent(node, new ArrayList<>());
    }

    /** Add undirected weighted edge (distance in km). */
    public void addEdge(String from, String to, double km) {
        addNode(from); addNode(to);
        adjacencyList.get(from).add(new Edge(to,   km));
        adjacencyList.get(to  ).add(new Edge(from, km));
    }

    public List<Edge>  getNeighbours(String node) { return adjacencyList.getOrDefault(node, Collections.emptyList()); }
    public Set<String> getAllNodes()               { return adjacencyList.keySet(); }
    public boolean     hasNode(String node)        { return adjacencyList.containsKey(node); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Road Network:\n");
        adjacencyList.forEach((n, edges) -> {
            sb.append("  ").append(n).append(" → ");
            edges.forEach(e -> sb.append(e.node).append("(").append(String.format("%.1f",e.weight)).append("km) "));
            sb.append("\n");
        });
        return sb.toString();
    }
}
