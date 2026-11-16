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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * POOLWISE Map API — lightweight HTTP server.
 *
 * <h2>Architecture</h2>
 * <ul>
 *   <li>{@link MapApiServer}  — entry point; binds the HTTP server and registers handlers.</li>
 *   <li>{@link MapDataHandler} — handles GET and POST on {@code /api/map-data}.</li>
 *   <li>{@link CorsFilter}    — attaches CORS headers to every response.</li>
 * </ul>
 *
 * <h2>API contract</h2>
 * <pre>
 *   POST /api/map-data          body: JSON map payload
 *                               response: {"id":"<uuid>"}
 *
 *   GET  /api/map-data?id=<uuid> response: the payload stored by the matching POST
 *   GET  /api/map-data           response: built-in sample payload (demo / health check)
 *
 *   GET  /health                 response: "OK"
 * </pre>
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
    // MapDataHandler
    // ══════════════════════════════════════════════════════════════

    /**
     * Handles both POST and GET on {@code /api/map-data}.
     *
     * <p><b>POST</b> — The desktop app (MapGenerator.java) sends the full JSON payload
     * for one carpool/driver map here. The server stores it in memory under a UUID
     * and returns {@code {"id":"<uuid>"}}. The desktop app then opens the browser at
     * {@code https://poolwise-map-viewer.vercel.app/?id=<uuid>}.</p>
     *
     * <p><b>GET ?id=&lt;uuid&gt;</b> — The Vercel frontend fetches the stored payload
     * for that specific carpool. This avoids the ~2 KB Windows URL-length limit that
     * broke the old {@code ?data=<full-json>} approach.</p>
     *
     * <p><b>GET</b> (no id) — Returns the built-in sample payload (demo / standalone mode).</p>
     */
    static final class MapDataHandler implements HttpHandler {

        private static final Gson GSON = new Gson();

        /**
         * In-memory store: UUID → raw JSON string of the map payload.
         * Bounded at MAX_ENTRIES to prevent unbounded memory growth.
         * Entries are evicted FIFO when the cap is reached.
         */
        private static final ConcurrentHashMap<String, String> STORE = new ConcurrentHashMap<>();
        private static final int MAX_ENTRIES = 100;

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 1. Attach CORS headers first (before any early return)
            CorsFilter.attach(exchange);

            // 2. Handle pre-flight OPTIONS (browsers send this before cross-origin POST)
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            switch (exchange.getRequestMethod().toUpperCase()) {
                case "POST" -> handlePost(exchange);
                case "GET"  -> handleGet(exchange);
                default     -> sendError(exchange, 405, "Method Not Allowed");
            }
        }

        // ── POST /api/map-data ────────────────────────────────────────────────

        /**
         * Receives a JSON map payload from the desktop app, stores it under a fresh UUID,
         * and returns {@code {"id":"<uuid>"}}.
         */
        private static void handlePost(HttpExchange exchange) throws IOException {
            byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
            if (bodyBytes.length == 0) {
                sendError(exchange, 400, "Empty request body");
                return;
            }

            String json = new String(bodyBytes, StandardCharsets.UTF_8);
            String id   = UUID.randomUUID().toString();

            // Evict oldest entry when store is full
            if (STORE.size() >= MAX_ENTRIES) {
                STORE.keySet().stream().findFirst().ifPresent(STORE::remove);
            }
            STORE.put(id, json);

            byte[] response = ("{\"id\":\"" + id + "\"}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(response);
            }
            LOG.info("Stored map payload id=" + id + " (" + bodyBytes.length + " bytes)");
        }

        // ── GET /api/map-data[?id=<uuid>] ────────────────────────────────────

        /**
         * Returns the payload stored for the given UUID, or the built-in sample
         * if no ID is provided (standalone / demo mode).
         */
        private static void handleGet(HttpExchange exchange) throws IOException {
            String id = extractQueryParam(exchange.getRequestURI().getQuery(), "id");

            byte[] json;
            if (id != null && STORE.containsKey(id)) {
                // Specific carpool/driver data stored by the desktop app
                json = STORE.get(id).getBytes(StandardCharsets.UTF_8);
                LOG.info("Serving stored payload id=" + id);
            } else {
                // Demo / standalone mode — return the built-in sample
                json = GSON.toJson(buildSamplePayload()).getBytes(StandardCharsets.UTF_8);
            }

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, json.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(json);
            }
        }

        // ── Sample payload (demo mode) ────────────────────────────────────────

        private static MapPayload buildSamplePayload() {
            List<MapPoint> points = List.of(
                new MapPoint(28.6139, 77.2090, "Driver — Connaught Place",
                             "Sample driver start — Connaught Place, New Delhi"),
                new MapPoint(28.6519, 77.1902, "Passenger — Karol Bagh",
                             "Sample pickup — Karol Bagh"),
                new MapPoint(28.5459, 77.1926, "Destination — IIT Delhi",
                             "Shared destination: IIT Delhi")
            );
            List<double[]> route = List.of(
                new double[]{77.2090, 28.6139},
                new double[]{77.1902, 28.6519},
                new double[]{77.1926, 28.5459}
            );
            double[] center = {
                (28.6139 + 28.5459) / 2.0,
                (77.2090 + 77.1926) / 2.0
            };
            return new MapPayload(
                "POOLWISE — Sample Route (Demo)",
                "Open the app, click 'Find Carpool', then 'View Route' to see real data",
                center, 12, points, route
            );
        }

        // ── Utilities ─────────────────────────────────────────────────────────

        /** Extracts a named parameter from a URL query string. Returns null if absent. */
        private static String extractQueryParam(String query, String name) {
            if (query == null || query.isBlank()) return null;
            for (String param : query.split("&")) {
                if (param.startsWith(name + "=")) {
                    return param.substring(name.length() + 1);
                }
            }
            return null;
        }

        private static void sendError(HttpExchange exchange, int code, String message) throws IOException {
            byte[] body = ("{\"error\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(code, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // CorsFilter
    // ══════════════════════════════════════════════════════════════

    /**
     * Attaches CORS headers so the Vercel-hosted frontend can call this API
     * from any origin without being blocked by the browser's same-origin policy.
     */
    static final class CorsFilter {

        private CorsFilter() { /* static utility */ }

        static void attach(HttpExchange exchange) {
            var headers = exchange.getResponseHeaders();
            headers.set("Access-Control-Allow-Origin",  "*");
            headers.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            headers.set("Access-Control-Allow-Headers", "Content-Type, Accept");
            headers.set("Access-Control-Max-Age",       "86400");
        }
    }
}
