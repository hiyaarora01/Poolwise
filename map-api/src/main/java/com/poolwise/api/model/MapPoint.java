package com.poolwise.api.model;

/**
 * Represents a single labelled map pin (marker).
 *
 * <p>Serialised to JSON by Gson — field names become the JSON keys.</p>
 */
public final class MapPoint {

    private final double lat;
    private final double lon;
    private final String title;
    private final String popup;

    public MapPoint(double lat, double lon, String title, String popup) {
        this.lat   = lat;
        this.lon   = lon;
        this.title = title;
        this.popup = popup;
    }

    // ── Getters (Gson uses field access, but explicit getters aid readability) ──

    public double getLat()    { return lat; }
    public double getLon()    { return lon; }
    public String getTitle()  { return title; }
    public String getPopup()  { return popup; }
}
