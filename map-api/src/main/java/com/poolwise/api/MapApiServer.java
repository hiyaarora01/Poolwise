package com.poolwise.api;

import com.google.gson.Gson;
import com.poolwise.api.model.MapPayload;
import com.poolwise.api.model.MapPoint;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * POOLWISE Map API — lightweight HTTP server.
 *
 * <h2>Architecture</h2>
 * <ul>
 *   <li>{@link MapApiServer}  — entry point; binds the HTTP server and registers handlers.</li>
 *   <li>{@link MapDataHandler} — handles {@code GET /api/map-data}.</li>
 *   <li>{@link CorsFilter}    — attaches CORS headers to every response.</li>
 * </ul>
 *
 * <h2>Build &amp; Run</h2>
 * <pre>
 *   mvn clean package
 *   java -jar target/map-api.jar          # local — binds to :8080
 *   PORT=10000 java -jar target/map-api.jar   # Render injects PORT at runtime
 * </pre>
 */
public final class MapApiServer {

    private static final Logger LOG = Logger.getLogger(MapApiServer.class.getName());

    private MapApiServer() { /* utility — not instantiated */ }

    public static void main(String[] args) throws IOException {

        // Render sets the PORT environment variable; fall back to 8080 locally.
        int port = resolvePort();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), /* backlog */ 64);

        // Virtual-thread executor — zero-cost blocked threads on Java 21+.
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        // Register route handlers
        server.createContext("/api/map-data", new MapDataHandler());

        // Health-check endpoint — Render pings this to decide if the service is up.
        server.createContext("/health", exchange -> {
            byte[] body = "OK".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });

        server.start();
        LOG.info("POOLWISE Map API listening on port " + port);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private static int resolvePort() {
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            try {
                return Integer.parseInt(envPort.trim());
            } catch (NumberFormatException e) {
                LOG.warning("Invalid PORT env var '" + envPort + "', falling back to 8080");
            }
        }
        return 8080;
    }

    // ══════════════════════════════════════════════════════════════
    // Inner handler classes (kept here for a compact single-file
    // server; extract to separate files as the project grows).
    // ══════════════════════════════════════════════════════════════

    /**
     * Handles {@code GET /api/map-data}.
     *
     * <p>Returns a sample POOLWISE carpool route around Bareilly, UP as JSON.
     * Wire this up to your real {@code MatchingService} / {@code RouteService}
     * when you are ready to serve live data.</p>
     */
    static final class MapDataHandler implements HttpHandler {

        private static final Gson GSON = new Gson();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // ── 1. Attach CORS header first (before any early return) ──────
            CorsFilter.attach(exchange);

            // ── 2. Handle pre-flight OPTIONS (browsers send this before POST) ─
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // ── 3. Only allow GET ──────────────────────────────────────────
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            // ── 4. Build the map payload ───────────────────────────────────
            MapPayload payload = buildSamplePayload();

            // ── 5. Serialise and respond ───────────────────────────────────
            byte[] json = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, json.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(json);
            }
        }

        // ── Sample data ─────────────────────────────────────────────────────
        // Replace this method body with a call to your RouteService /
        // MatchingService to serve real, dynamic carpool map data.
        private static MapPayload buildSamplePayload() {

            // Sample points around Bareilly, Uttar Pradesh
            List<MapPoint> points = List.of(
                new MapPoint(28.3670, 79.4304, "Alice — Driver Start",
                             "Alice's home — departs 08:00 AM"),
                new MapPoint(28.3821, 79.4512, "Bob — Pickup",
                             "Bob's pickup location — walk < 0.3 km"),
                new MapPoint(28.3950, 79.4710, "Carol — Pickup",
                             "Carol's pickup location — walk < 0.4 km"),
                new MapPoint(28.3568, 79.4190, "Meeting point — Bob",
                             "Suggested meeting point for Bob"),
                new MapPoint(28.4100, 79.4900, "Destination — Civil Lines",
                             "Shared destination: Civil Lines, Bareilly")
            );

            // Route coordinates in GeoJSON order: [longitude, latitude]
            List<double[]> route = List.of(
                new double[]{79.4304, 28.3670},   // driver start
                new double[]{79.4512, 28.3821},   // Bob pickup
                new double[]{79.4710, 28.3950},   // Carol pickup
                new double[]{79.4900, 28.4100}    // destination
            );

            // Map centre = midpoint of driver start and destination
            double[] center = {
                (28.3670 + 28.4100) / 2.0,   // lat
                (79.4304 + 79.4900) / 2.0    // lon
            };

            return new MapPayload(
                "POOLWISE — Sample Carpool Route",
                "Driver: Alice | Passengers: 2 | Extra distance: 1.42 km",
                center,
                12,
                points,
                route
            );
        }

        // ── Utility ─────────────────────────────────────────────────────────

        private static void sendError(HttpExchange exchange, int code, String message) throws IOException {
            byte[] body = ("{\"error\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(code, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        }
    }

    /**
     * Attaches CORS headers so the Vercel-hosted frontend can call this API
     * from any origin without being blocked by the browser's same-origin policy.
     *
     * <p>For a production deployment you should restrict
     * {@code Access-Control-Allow-Origin} to your exact Vercel domain, e.g.:
     * <pre>  https://poolwise-map-viewer.vercel.app</pre>
     * and store it in an environment variable on Render.</p>
     */
    static final class CorsFilter {

        private CorsFilter() { /* static utility */ }

        static void attach(HttpExchange exchange) {
            var headers = exchange.getResponseHeaders();
            headers.set("Access-Control-Allow-Origin",  "*");
            headers.set("Access-Control-Allow-Methods", "GET, OPTIONS");
            headers.set("Access-Control-Allow-Headers", "Content-Type, Accept");
            headers.set("Access-Control-Max-Age",       "86400");
        }
    }
}
