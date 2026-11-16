package com.poolwise.api.model;

import java.util.List;

/**
 * Top-level JSON payload returned by {@code GET /api/map-data}.
 *
 * <pre>
 * {
 *   "title":    "POOLWISE — Sample Route",
 *   "subtitle": "Driver: Alice | Passengers: 2",
 *   "center":   [28.3670, 79.4304],
 *   "zoom":     12,
 *   "points":   [ { "lat":…, "lon":…, "title":…, "popup":… }, … ],
 *   "route":    [ [lon, lat], … ]   // GeoJSON coordinate order [lon, lat]
 * }
 * </pre>
 *
 * <p>Gson serialises every field automatically.</p>
 */
public final class MapPayload {

    private final String       title;
    private final String       subtitle;
    private final double[]     center;
    private final int          zoom;
    private final List<MapPoint>    points;
    private final List<double[]>    route;

    public MapPayload(
            String title,
            String subtitle,
            double[] center,
            int zoom,
            List<MapPoint>  points,
            List<double[]>  route) {

        this.title    = title;
        this.subtitle = subtitle;
        this.center   = center;
        this.zoom     = zoom;
        this.points   = points;
        this.route    = route;
    }

    // ── Getters ───────────────────────────────────────────────────

    public String          getTitle()    { return title; }
    public String          getSubtitle() { return subtitle; }
    public double[]        getCenter()   { return center; }
    public int             getZoom()     { return zoom; }
    public List<MapPoint>  getPoints()   { return points; }
    public List<double[]>  getRoute()    { return route; }
}
