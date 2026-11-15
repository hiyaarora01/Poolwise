package com.poolwise.ui;

import com.poolwise.model.*;

import java.awt.Desktop;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Map Generator — Google Maps JavaScript API + OpenStreetMap fallback.
 *
 * Uses Google Maps JS API (requires free API key from console.cloud.google.com).
 * Falls back to OpenStreetMap/Leaflet if no key is set.
 *
 * API key is stored in Java Preferences (persisted between app restarts).
 * User is prompted once; key can be changed from the Map panel.
 *
 * Features (Google Maps mode):
 *   - Real satellite + street map of exact GPS coordinates
 *   - Actual road routing via Google Directions API
 *   - Custom coloured markers for driver / passengers / meeting points
 *   - Info-windows with full details on click
 *   - Meeting point calculation (nearest point on driver route)
 */
public class MapGenerator {

    private static final Logger LOG = Logger.getLogger(MapGenerator.class.getName());
    private static final Preferences PREFS = Preferences.userNodeForPackage(MapGenerator.class);
    private static final String PREF_API_KEY = "google_maps_api_key";

    // ── API Key management ────────────────────────────────────────

    public static String getApiKey() {
        return PREFS.get(PREF_API_KEY, "");
    }

    public static void setApiKey(String key) {
        PREFS.put(PREF_API_KEY, key == null ? "" : key.trim());
    }

    public static boolean hasApiKey() {
        String k = getApiKey();
        return k != null && !k.isBlank();
    }

    // ── Public entry points ───────────────────────────────────────

    private static final String HOSTED_MAP_URL =
        "https://poolwise-map-viewer-alpyxf04a-hiya23.vercel.app/";

    public static void openCarpoolMap(Carpool carpool) {
        try {
            Driver d = carpool.getDriver();
            List<Passenger> passengers = carpool.getPassengers();
            double centerLat = d.getStartLat() + d.getDestLat();
            double centerLon = d.getStartLon() + d.getDestLon();
            StringBuilder points = new StringBuilder();
            StringBuilder route = new StringBuilder();
            addMapPoint(points, d.getStartLat(), d.getStartLon(), d.getName() + " — Start", d.getStartLocation());
            appendRoutePoint(route, d.getStartLon(), d.getStartLat());
            addMapPoint(points, d.getDestLat(), d.getDestLon(), "Destination", d.getDestination());
            for (Passenger p : passengers) {
                centerLat += p.getPickupLat();
                centerLon += p.getPickupLon();
                addMapPoint(points, p.getPickupLat(), p.getPickupLon(), p.getName() + " — Pickup", p.getPickupLocation());
                double[] meet = nearestPointOnSegment(d.getStartLat(), d.getStartLon(), d.getDestLat(), d.getDestLon(),
                    p.getPickupLat(), p.getPickupLon());
                addMapPoint(points, meet[0], meet[1], "Meeting point — " + p.getName(), "Suggested meeting point");
                appendRoutePoint(route, p.getPickupLon(), p.getPickupLat());
            }
            appendRoutePoint(route, d.getDestLon(), d.getDestLat());
            route.append("]");
            centerLat /= passengers.size() + 2;
            centerLon /= passengers.size() + 2;
            String subtitle = "Driver: " + d.getName() + " | Passengers: " + passengers.size()
                + " | Extra distance: " + String.format("%.2f km", carpool.getExtraDistanceKm());
            openHostedMap("POOLWISE — " + carpool.getCarpoolId(), subtitle,
                centerLat, centerLon, 12, points.toString(), route.toString());
        } catch (Exception e) {
            LOG.severe("Map error: " + e.getMessage());
        }
    }

    public static void openMeetingPointMap(Driver driver, Passenger passenger) {
        try {
            double[] meet = nearestPointOnSegment(driver.getStartLat(), driver.getStartLon(),
                driver.getDestLat(), driver.getDestLon(), passenger.getPickupLat(), passenger.getPickupLon());
            StringBuilder points = new StringBuilder();
            addMapPoint(points, driver.getStartLat(), driver.getStartLon(), driver.getName() + " — Start", driver.getStartLocation());
            addMapPoint(points, driver.getDestLat(), driver.getDestLon(), "Destination", driver.getDestination());
            addMapPoint(points, passenger.getPickupLat(), passenger.getPickupLon(), passenger.getName() + " — Pickup", passenger.getPickupLocation());
            addMapPoint(points, meet[0], meet[1], "Suggested Meeting Point", "Meeting point for " + passenger.getName());
            String route = "[" + routePoint(driver.getStartLon(), driver.getStartLat()) + ","
                + routePoint(meet[1], meet[0]) + "," + routePoint(driver.getDestLon(), driver.getDestLat()) + "]";
            openHostedMap("POOLWISE — Meeting Point", driver.getName() + " & " + passenger.getName(),
                (driver.getStartLat() + driver.getDestLat() + passenger.getPickupLat()) / 3,
                (driver.getStartLon() + driver.getDestLon() + passenger.getPickupLon()) / 3,
                12, points.toString(), route);
        } catch (Exception e) {
            LOG.severe("Meeting-point map error: " + e.getMessage());
        }
    }

    public static void openDriverOverviewMap(Driver driver, List<Passenger> nearby) {
        try {
            double centerLat = driver.getStartLat() + driver.getDestLat();
            double centerLon = driver.getStartLon() + driver.getDestLon();
            StringBuilder points = new StringBuilder();
            StringBuilder route = new StringBuilder();
            addMapPoint(points, driver.getStartLat(), driver.getStartLon(), driver.getName() + " — Start", driver.getStartLocation());
            addMapPoint(points, driver.getDestLat(), driver.getDestLon(), "Destination", driver.getDestination());
            appendRoutePoint(route, driver.getStartLon(), driver.getStartLat());
            for (Passenger p : nearby) {
                centerLat += p.getPickupLat();
                centerLon += p.getPickupLon();
                addMapPoint(points, p.getPickupLat(), p.getPickupLon(), p.getName() + " — Pickup", p.getPickupLocation());
                double[] meet = nearestPointOnSegment(driver.getStartLat(), driver.getStartLon(),
                    driver.getDestLat(), driver.getDestLon(), p.getPickupLat(), p.getPickupLon());
                addMapPoint(points, meet[0], meet[1], "Meeting point — " + p.getName(), "Suggested meeting point");
            }
            appendRoutePoint(route, driver.getDestLon(), driver.getDestLat());
            route.append("]");
            centerLat /= nearby.size() + 2;
            centerLon /= nearby.size() + 2;
            openHostedMap("POOLWISE — Driver Route: " + driver.getName(),
                driver.getStartLocation() + " → " + driver.getDestination() + " | " + nearby.size() + " nearby passengers",
                centerLat, centerLon, 12, points.toString(), route.toString());
        } catch (Exception e) {
            LOG.severe("Driver map error: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // GOOGLE MAPS HTML BUILDERS
    // ══════════════════════════════════════════════════════════════

    private static String buildGoogleCarpoolHtml(Carpool cp) {
        Driver d = cp.getDriver();
        List<Passenger> passengers = cp.getPassengers();

        double centerLat = d.getStartLat(), centerLon = d.getStartLon();
        for (Passenger p : passengers) { centerLat += p.getPickupLat(); centerLon += p.getPickupLon(); }
        centerLat = (centerLat + d.getDestLat()) / (passengers.size() + 2);
        centerLon = (centerLon + d.getDestLon()) / (passengers.size() + 2);

        StringBuilder markers = new StringBuilder();

        // Driver start
        markers.append(googleMarker(d.getStartLat(), d.getStartLon(),
            "http://maps.google.com/mapfiles/ms/icons/blue-dot.png",
            d.getName() + " — Start",
            "<div style='font-family:Segoe UI,sans-serif;min-width:200px'>"
            + "<h3 style='color:#1d4ed8;margin:0 0 6px'>🚗 " + esc(d.getName()) + "</h3>"
            + "<b>From:</b> " + esc(d.getStartLocation()) + "<br>"
            + "<b>To:</b> " + esc(d.getDestination()) + "<br>"
            + "<b>Departs:</b> " + d.getDepartureTime() + "<br>"
            + "<b>Seats:</b> " + d.getAvailableSeats() + "/" + d.getTotalSeats() + "<br>"
            + "<b>Max Detour:</b> " + d.getMaxDetourKm() + " km</div>"));

        // Driver destination
        markers.append(googleMarker(d.getDestLat(), d.getDestLon(),
            "http://maps.google.com/mapfiles/ms/icons/red-dot.png",
            "Destination: " + d.getDestination(),
            "<div style='font-family:Segoe UI,sans-serif'>"
            + "<h3 style='color:#dc2626;margin:0 0 6px'>🏁 Destination</h3>"
            + esc(d.getDestination()) + "</div>"));

        // Passengers
        String[] paxIcons = {
            "http://maps.google.com/mapfiles/ms/icons/orange-dot.png",
            "http://maps.google.com/mapfiles/ms/icons/purple-dot.png",
            "http://maps.google.com/mapfiles/ms/icons/green-dot.png",
            "http://maps.google.com/mapfiles/ms/icons/yellow-dot.png",
            "http://maps.google.com/mapfiles/ms/icons/pink-dot.png"
        };
        int ci = 0;
        for (Passenger p : passengers) {
            String icon = paxIcons[ci % paxIcons.length]; ci++;
            double[] meet = nearestPointOnSegment(
                d.getStartLat(), d.getStartLon(), d.getDestLat(), d.getDestLon(),
                p.getPickupLat(), p.getPickupLon());
            double walkKm = haversine(p.getPickupLat(), p.getPickupLon(), meet[0], meet[1]);

            markers.append(googleMarker(p.getPickupLat(), p.getPickupLon(), icon,
                p.getName() + " — Pickup",
                "<div style='font-family:Segoe UI,sans-serif;min-width:200px'>"
                + "<h3 style='color:#ea580c;margin:0 0 6px'>👤 " + esc(p.getName()) + "</h3>"
                + "<b>Pickup:</b> " + esc(p.getPickupLocation()) + "<br>"
                + "<b>Arrive by:</b> " + p.getRequiredArrivalTime() + "<br>"
                + "<b>Max wait:</b> " + (int)p.getMaxWaitMinutes() + " min<br>"
                + "<b>Walk to route:</b> " + String.format("%.2f km", walkKm) + "</div>"));

            // Meeting point
            markers.append(googleMarker(meet[0], meet[1],
                "http://maps.google.com/mapfiles/ms/icons/ltblue-dot.png",
                "Meeting point for " + p.getName(),
                "<div style='font-family:Segoe UI,sans-serif'>"
                + "<h3 style='color:#0891b2;margin:0 0 6px'>🤝 Meeting Point</h3>"
                + "<b>For:</b> " + esc(p.getName()) + "<br>"
                + String.format("<b>Walk from home:</b> %.2f km<br>", walkKm)
                + String.format("<b>Coords:</b> %.5f, %.5f", meet[0], meet[1]) + "</div>"));
        }

        // Directions waypoints (pickup order)
        StringBuilder waypoints = new StringBuilder();
        for (int i = 0; i < passengers.size() - 1; i++) {
            Passenger p = passengers.get(i);
            if (i > 0) waypoints.append("|");
            waypoints.append(p.getPickupLat()).append(",").append(p.getPickupLon());
        }
        String lastPax = passengers.isEmpty() ? ""
            : passengers.get(passengers.size()-1).getPickupLat() + "," + passengers.get(passengers.size()-1).getPickupLon();

        // Build directions JS
        String directionsJs = buildGoogleDirectionsJs(
            d.getStartLat() + "," + d.getStartLon(),
            passengers.isEmpty() ? d.getDestLat()+","+d.getDestLon() : lastPax,
            waypoints.toString(),
            d.getDestLat() + "," + d.getDestLon(),
            passengers.size() > 1);

        String title = "POOLWISE — " + cp.getCarpoolId() + " Route";
        String subtitle = "Driver: " + d.getName() + "  |  Passengers: " + passengers.size()
            + "  |  Extra: " + String.format("%.2f km", cp.getExtraDistanceKm())
            + "  |  CO₂ saved: " + String.format("%.2f kg", cp.getEstimatedCO2SavedKg());

        return buildGoogleHtml(title, subtitle, centerLat, centerLon, 13, markers.toString(), directionsJs);
    }

    private static String buildGoogleMeetingHtml(Driver d, Passenger p) {
        double[] meet = nearestPointOnSegment(
            d.getStartLat(), d.getStartLon(), d.getDestLat(), d.getDestLon(),
            p.getPickupLat(), p.getPickupLon());
        double detour = haversine(d.getStartLat(), d.getStartLon(), meet[0], meet[1])
                      + haversine(meet[0], meet[1], d.getDestLat(), d.getDestLon())
                      - haversine(d.getStartLat(), d.getStartLon(), d.getDestLat(), d.getDestLon());
        double walkKm = haversine(p.getPickupLat(), p.getPickupLon(), meet[0], meet[1]);

        double centerLat = (d.getStartLat() + d.getDestLat() + p.getPickupLat()) / 3;
        double centerLon = (d.getStartLon() + d.getDestLon() + p.getPickupLon()) / 3;

        StringBuilder markers = new StringBuilder();
        markers.append(googleMarker(d.getStartLat(), d.getStartLon(),
            "http://maps.google.com/mapfiles/ms/icons/blue-dot.png", d.getName() + " Start",
            "<div style='font-family:Segoe UI,sans-serif'><h3 style='color:#1d4ed8;margin:0 0 6px'>🚗 " + esc(d.getName()) + "</h3>"
            + esc(d.getStartLocation()) + "<br>Departs: " + d.getDepartureTime() + "</div>"));
        markers.append(googleMarker(d.getDestLat(), d.getDestLon(),
            "http://maps.google.com/mapfiles/ms/icons/red-dot.png", "Destination",
            "<div style='font-family:Segoe UI,sans-serif'><h3 style='color:#dc2626;margin:0'>🏁 " + esc(d.getDestination()) + "</h3></div>"));
        markers.append(googleMarker(p.getPickupLat(), p.getPickupLon(),
            "http://maps.google.com/mapfiles/ms/icons/orange-dot.png", p.getName() + " Home",
            "<div style='font-family:Segoe UI,sans-serif'><h3 style='color:#ea580c;margin:0 0 6px'>👤 " + esc(p.getName()) + "</h3>"
            + esc(p.getPickupLocation()) + "<br>Arrive by: " + p.getRequiredArrivalTime() + "</div>"));
        markers.append(googleMarker(meet[0], meet[1],
            "http://maps.google.com/mapfiles/ms/icons/green-dot.png", "🤝 Meeting Point",
            "<div style='font-family:Segoe UI,sans-serif;min-width:200px'>"
            + "<h3 style='color:#16a34a;margin:0 0 6px'>🤝 Suggested Meeting Point</h3>"
            + String.format("<b>Driver detour:</b> %.2f km<br>", detour)
            + String.format("<b>Passenger walks:</b> %.2f km<br>", walkKm)
            + String.format("<b>GPS:</b> %.5f, %.5f</div>", meet[0], meet[1])));

        String directionsJs = buildGoogleDirectionsJs(
            d.getStartLat() + "," + d.getStartLon(),
            d.getDestLat() + "," + d.getDestLon(),
            meet[0] + "," + meet[1], "", false);

        String title = "POOLWISE — Meeting Point: " + d.getName() + " & " + p.getName();
        String subtitle = String.format("Driver detour: %.2f km  |  Passenger walks: %.2f km  |  GPS: %.4f, %.4f",
            detour, walkKm, meet[0], meet[1]);

        return buildGoogleHtml(title, subtitle, centerLat, centerLon, 13, markers.toString(), directionsJs);
    }

    private static String buildGoogleDriverHtml(Driver d, List<Passenger> nearby) {
        double centerLat = d.getStartLat(), centerLon = d.getStartLon();
        for (Passenger p : nearby) { centerLat += p.getPickupLat(); centerLon += p.getPickupLon(); }
        int total = nearby.size() + 1;
        centerLat /= total; centerLon /= total;

        StringBuilder markers = new StringBuilder();
        markers.append(googleMarker(d.getStartLat(), d.getStartLon(),
            "http://maps.google.com/mapfiles/ms/icons/blue-dot.png", d.getName() + " (you)",
            "<div style='font-family:Segoe UI,sans-serif'><h3 style='color:#1d4ed8;margin:0 0 6px'>🚗 " + esc(d.getName()) + "</h3>"
            + esc(d.getStartLocation()) + " → " + esc(d.getDestination()) + "<br>"
            + "Departs: " + d.getDepartureTime() + " | Seats: " + d.getAvailableSeats() + "</div>"));
        markers.append(googleMarker(d.getDestLat(), d.getDestLon(),
            "http://maps.google.com/mapfiles/ms/icons/red-dot.png", "Your Destination",
            "<div style='font-family:Segoe UI,sans-serif'><h3 style='color:#dc2626;margin:0'>🏁 " + esc(d.getDestination()) + "</h3></div>"));

        String[] icons = {
            "http://maps.google.com/mapfiles/ms/icons/orange-dot.png",
            "http://maps.google.com/mapfiles/ms/icons/purple-dot.png",
            "http://maps.google.com/mapfiles/ms/icons/yellow-dot.png",
            "http://maps.google.com/mapfiles/ms/icons/pink-dot.png"
        };
        int ci = 0;
        for (Passenger p : nearby) {
            double[] meet = nearestPointOnSegment(
                d.getStartLat(), d.getStartLon(), d.getDestLat(), d.getDestLon(),
                p.getPickupLat(), p.getPickupLon());
            double walkKm = haversine(p.getPickupLat(), p.getPickupLon(), meet[0], meet[1]);
            String icon = icons[ci++ % icons.length];
            markers.append(googleMarker(p.getPickupLat(), p.getPickupLon(), icon, p.getName(),
                "<div style='font-family:Segoe UI,sans-serif'><h3 style='color:#ea580c;margin:0 0 6px'>👤 " + esc(p.getName()) + "</h3>"
                + esc(p.getPickupLocation()) + "<br>Arrive by: " + p.getRequiredArrivalTime()
                + "<br>Walk to your route: " + String.format("%.2f km", walkKm) + "</div>"));
            markers.append(googleMarker(meet[0], meet[1],
                "http://maps.google.com/mapfiles/ms/icons/ltblue-dot.png",
                "Meetup for " + p.getName(),
                "<div style='font-family:Segoe UI,sans-serif'>🤝 Suggested pickup spot for <b>" + esc(p.getName()) + "</b></div>"));
        }

        String directionsJs = buildGoogleDirectionsJs(
            d.getStartLat() + "," + d.getStartLon(),
            d.getDestLat() + "," + d.getDestLon(), "", "", false);

        String title = "POOLWISE — Driver Route: " + d.getName();
        String subtitle = d.getStartLocation() + " → " + d.getDestination() + "  |  " + nearby.size() + " nearby passengers";

        return buildGoogleHtml(title, subtitle, centerLat, centerLon, 12, markers.toString(), directionsJs);
    }

    // ── Google Maps HTML template ─────────────────────────────────

    private static String buildGoogleHtml(String title, String subtitle,
            double lat, double lon, int zoom, String markersJs, String directionsJs) {
        String apiKey = getApiKey();
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='en'><head>\n");
        sb.append("<meta charset='UTF-8'>\n");
        sb.append("<meta name='viewport' content='width=device-width,initial-scale=1'>\n");
        sb.append("<title>").append(esc(title)).append("</title>\n");
        sb.append("<style>\n");
        sb.append("*{margin:0;padding:0;box-sizing:border-box}\n");
        sb.append("body{font-family:'Segoe UI',sans-serif;background:#0f172a}\n");
        sb.append("#header{background:linear-gradient(135deg,#1e40af,#0369a1);padding:12px 20px;");
        sb.append("display:flex;align-items:center;gap:12px;box-shadow:0 2px 8px rgba(0,0,0,.4)}\n");
        sb.append(".logo{font-size:20px;font-weight:800;color:#93c5fd}\n");
        sb.append(".info h1{font-size:14px;color:#fff;margin:0}\n");
        sb.append(".info p{font-size:11px;color:#94a3b8;margin-top:2px}\n");
        sb.append("#map{width:100%;height:calc(100vh - 46px)}\n");
        sb.append(".gm-style .gm-style-iw-c{border-radius:10px!important;padding:4px!important}\n");
        sb.append("</style>\n</head><body>\n");
        sb.append("<div id='header'><div class='logo'>&#x1F697; POOLWISE</div><div class='info'>");
        sb.append("<h1>").append(esc(title)).append("</h1>");
        sb.append("<p>").append(esc(subtitle)).append("</p></div></div>\n");
        sb.append("<div id='map'></div>\n");
        sb.append("<script>\n");
        sb.append("var map, directionsService, directionsRenderer;\n");
        sb.append("function initMap(){\n");
        sb.append("  map = new google.maps.Map(document.getElementById('map'),{\n");
        sb.append("    center:{lat:").append(lat).append(",lng:").append(lon).append("},\n");
        sb.append("    zoom:").append(zoom).append(",\n");
        sb.append("    mapTypeId:'roadmap',\n");
        sb.append("    mapTypeControl:true,\n");
        sb.append("    mapTypeControlOptions:{style:google.maps.MapTypeControlStyle.HORIZONTAL_BAR,");
        sb.append("mapTypeIds:['roadmap','satellite','hybrid','terrain']}\n");
        sb.append("  });\n");
        sb.append("  directionsService = new google.maps.DirectionsService();\n");
        sb.append("  directionsRenderer = new google.maps.DirectionsRenderer({suppressMarkers:true,");
        sb.append("polylineOptions:{strokeColor:'#2563eb',strokeWeight:5,strokeOpacity:0.85}});\n");
        sb.append("  directionsRenderer.setMap(map);\n");
        sb.append("  var infoWindow = new google.maps.InfoWindow();\n");
        sb.append(markersJs);
        sb.append(directionsJs);
        sb.append("}\n");
        sb.append("function addMarker(lat,lng,iconUrl,title,content,iw){\n");
        sb.append("  var m=new google.maps.Marker({position:{lat:lat,lng:lng},map:map,");
        sb.append("icon:iconUrl,title:title,animation:google.maps.Animation.DROP});\n");
        sb.append("  m.addListener('click',function(){iw.setContent(content);iw.open(map,m);});\n");
        sb.append("  return m;\n}\n");
        sb.append("</script>\n");
        sb.append("<script async defer src='https://maps.googleapis.com/maps/api/js?key=");
        sb.append(apiKey).append("&callback=initMap'></script>\n");
        sb.append("</body></html>\n");
        return sb.toString();
    }

    private static String googleMarker(double lat, double lon, String iconUrl, String title, String popup) {
        return "addMarker(" + lat + "," + lon + ",'" + iconUrl + "','" + jsEsc(title)
            + "','" + jsEsc(popup) + "',infoWindow);\n";
    }

    private static String buildGoogleDirectionsJs(String origin, String dest,
            String waypointsStr, String finalDest, boolean hasWaypoints) {
        StringBuilder sb = new StringBuilder();
        sb.append("  // Google Directions API — real road route\n");
        sb.append("  var req = {origin:'").append(origin).append("',\n");
        sb.append("             destination:'").append(dest).append("',\n");
        sb.append("             travelMode:google.maps.TravelMode.DRIVING");
        if (hasWaypoints && !waypointsStr.isBlank()) {
            sb.append(",\n             waypoints:[");
            for (String wp : waypointsStr.split("\\|")) {
                if (!wp.isBlank())
                    sb.append("{location:'").append(wp).append("',stopover:true},");
            }
            sb.append("]");
        }
        sb.append("};\n");
        sb.append("  directionsService.route(req,function(result,status){\n");
        sb.append("    if(status==='OK') directionsRenderer.setDirections(result);\n");
        sb.append("    else console.warn('Directions failed: '+status);\n");
        sb.append("  });\n");
        return sb.toString();
    }

    // ══════════════════════════════════════════════════════════════
    // OPENSTREETMAP / LEAFLET FALLBACK BUILDERS (no API key)
    // ══════════════════════════════════════════════════════════════

    private static String buildLeafletCarpoolHtml(Carpool cp) {
        Driver d = cp.getDriver();
        List<Passenger> passengers = cp.getPassengers();
        double avgLat = d.getStartLat() + d.getDestLat();
        double avgLon = d.getStartLon() + d.getDestLon();
        for (Passenger p : passengers) { avgLat += p.getPickupLat(); avgLon += p.getPickupLon(); }
        avgLat /= (passengers.size() + 2); avgLon /= (passengers.size() + 2);

        StringBuilder mk = new StringBuilder();
        mk.append(lMarker(d.getStartLat(), d.getStartLon(), "#2563eb", "D", d.getName() + " Start",
            "<b>" + d.getName() + "</b><br>" + d.getStartLocation() + "<br>Departs " + d.getDepartureTime()));
        mk.append(lMarker(d.getDestLat(), d.getDestLon(), "#dc2626", "X", "Destination",
            "<b>Destination</b><br>" + d.getDestination()));
        String[] cols = {"#f97316","#7c3aed","#0891b2","#166534","#991b1b"};
        int ci = 0;
        for (Passenger p : passengers) {
            String col = cols[ci++ % cols.length];
            double[] m = nearestPointOnSegment(d.getStartLat(),d.getStartLon(),d.getDestLat(),d.getDestLon(),p.getPickupLat(),p.getPickupLon());
            double w = haversine(p.getPickupLat(),p.getPickupLon(),m[0],m[1]);
            mk.append(lMarker(p.getPickupLat(),p.getPickupLon(),col,"P",p.getName()+" pickup",
                "<b>"+p.getName()+"</b><br>"+p.getPickupLocation()+"<br>Walk to route: "+String.format("%.2fkm",w)));
            mk.append(lCircle(m[0],m[1],col,"Meet "+p.getName(),"Meeting point for "+p.getName()));
        }
        StringBuilder wp = new StringBuilder("[");
        wp.append("[").append(d.getStartLon()).append(",").append(d.getStartLat()).append("]");
        for (Passenger p : passengers) wp.append(",[").append(p.getPickupLon()).append(",").append(p.getPickupLat()).append("]");
        wp.append(",[").append(d.getDestLon()).append(",").append(d.getDestLat()).append("]").append("]");
        return buildLeafletHtml("POOLWISE — " + cp.getCarpoolId(),
            "Driver: "+d.getName()+" | Passengers: "+passengers.size()+" | Extra: "+String.format("%.2f km",cp.getExtraDistanceKm()),
            avgLat, avgLon, 12, mk.toString(), wp.toString());
    }

    private static String buildLeafletMeetingHtml(Driver d, Passenger p) {
        double[] m = nearestPointOnSegment(d.getStartLat(),d.getStartLon(),d.getDestLat(),d.getDestLon(),p.getPickupLat(),p.getPickupLon());
        double detour = haversine(d.getStartLat(),d.getStartLon(),m[0],m[1])+haversine(m[0],m[1],d.getDestLat(),d.getDestLon())-haversine(d.getStartLat(),d.getStartLon(),d.getDestLat(),d.getDestLon());
        double walk = haversine(p.getPickupLat(),p.getPickupLon(),m[0],m[1]);
        double cLat=(d.getStartLat()+d.getDestLat()+p.getPickupLat())/3, cLon=(d.getStartLon()+d.getDestLon()+p.getPickupLon())/3;
        StringBuilder mk = new StringBuilder();
        mk.append(lMarker(d.getStartLat(),d.getStartLon(),"#2563eb","D",d.getName(),"<b>"+d.getName()+"</b><br>"+d.getStartLocation()));
        mk.append(lMarker(d.getDestLat(),d.getDestLon(),"#dc2626","X","Destination","<b>Destination</b><br>"+d.getDestination()));
        mk.append(lMarker(p.getPickupLat(),p.getPickupLon(),"#f97316","P",p.getName(),"<b>"+p.getName()+"</b><br>"+p.getPickupLocation()));
        mk.append(lMarker(m[0],m[1],"#16a34a","M","Meeting Point","<b>Meeting Point</b><br>Detour: "+String.format("%.2f km",detour)+"<br>Walk: "+String.format("%.2f km",walk)));
        String wp="[["+d.getStartLon()+","+d.getStartLat()+"],["+m[1]+","+m[0]+"],["+d.getDestLon()+","+d.getDestLat()+"]]";
        return buildLeafletHtml("Meeting: "+d.getName()+" & "+p.getName(),
            String.format("Detour %.2f km | Walk %.2f km | GPS %.4f,%.4f",detour,walk,m[0],m[1]),
            cLat,cLon,12,mk.toString(),wp);
    }

    private static String buildLeafletDriverHtml(Driver d, List<Passenger> nearby) {
        double cLat=d.getStartLat(),cLon=d.getStartLon();
        for(Passenger p:nearby){cLat+=p.getPickupLat();cLon+=p.getPickupLon();}
        cLat/=(nearby.size()+1); cLon/=(nearby.size()+1);
        StringBuilder mk=new StringBuilder();
        mk.append(lMarker(d.getStartLat(),d.getStartLon(),"#2563eb","D",d.getName(),"<b>"+d.getName()+"</b><br>"+d.getStartLocation()+" → "+d.getDestination()));
        mk.append(lMarker(d.getDestLat(),d.getDestLon(),"#dc2626","X","Destination","<b>Destination</b><br>"+d.getDestination()));
        String[]cs={"#f97316","#7c3aed","#0891b2","#166534"};int ci=0;
        for(Passenger p:nearby){
            String c=cs[ci++%cs.length];
            double[]m=nearestPointOnSegment(d.getStartLat(),d.getStartLon(),d.getDestLat(),d.getDestLon(),p.getPickupLat(),p.getPickupLon());
            double w=haversine(p.getPickupLat(),p.getPickupLon(),m[0],m[1]);
            mk.append(lMarker(p.getPickupLat(),p.getPickupLon(),c,"P",p.getName(),"<b>"+p.getName()+"</b><br>"+p.getPickupLocation()+"<br>Walk: "+String.format("%.2f km",w)));
            mk.append(lCircle(m[0],m[1],c,"Meetup "+p.getName(),"Meeting for "+p.getName()));
        }
        String wp="[["+d.getStartLon()+","+d.getStartLat()+"],["+d.getDestLon()+","+d.getDestLat()+"]]";
        return buildLeafletHtml("Driver: "+d.getName()+" Route",d.getStartLocation()+" → "+d.getDestination()+" | "+nearby.size()+" passengers",cLat,cLon,12,mk.toString(),wp);
    }

    // ── Leaflet template ──────────────────────────────────────────

    private static String buildLeafletHtml(String title, String subtitle,
            double lat, double lon, int zoom, String markersJs, String waypointsJson) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>");
        sb.append("<meta name='viewport' content='width=device-width,initial-scale=1'>");
        sb.append("<title>").append(esc(title)).append("</title>");
        sb.append("<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>");
        sb.append("<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>");
        sb.append("<style>*{margin:0;padding:0;box-sizing:border-box}body{font-family:'Segoe UI',sans-serif;background:#0f172a}");
        sb.append("#header{background:linear-gradient(135deg,#1e40af,#0369a1);padding:12px 20px;display:flex;align-items:center;gap:12px}");
        sb.append(".logo{font-size:20px;font-weight:800;color:#93c5fd}.info h1{font-size:14px;color:#fff}.info p{font-size:11px;color:#94a3b8;margin-top:2px}");
        sb.append("#map{width:100%;height:calc(100vh - 46px)}</style></head><body>");
        sb.append("<div id='header'><div class='logo'>&#x1F697; POOLWISE</div><div class='info'><h1>").append(esc(title)).append("</h1><p>").append(esc(subtitle)).append("</p></div></div>");
        sb.append("<div id='map'></div><script>");
        sb.append("var map=L.map('map').setView([").append(lat).append(",").append(lon).append("],").append(zoom).append(");");
        sb.append("L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png',{attribution:'&copy; OpenStreetMap contributors',maxZoom:19}).addTo(map);");
        sb.append("function pwIcon(c,l){return L.divIcon({html:'<div style=\"background:'+c+';width:30px;height:30px;border-radius:50% 50% 50% 0;transform:rotate(-45deg);border:2px solid #fff;box-shadow:0 2px 4px rgba(0,0,0,.4)\"><span style=\"transform:rotate(45deg);display:block;text-align:center;font-size:10px;font-weight:bold;line-height:26px;color:#fff\">'+l+'</span></div>',iconSize:[30,30],iconAnchor:[15,30],popupAnchor:[0,-32],className:''});}");
        sb.append(markersJs);
        sb.append("var wp=").append(waypointsJson).append(";");
        sb.append("fetch('https://router.project-osrm.org/route/v1/driving/'+wp.map(w=>w[0]+','+w[1]).join(';')+'?overview=full&geometries=geojson')");
        sb.append(".then(r=>r.json()).then(d=>{if(d.code==='Ok'){var r=L.geoJSON(d.routes[0].geometry,{style:{color:'#2563eb',weight:5,opacity:.85}}).addTo(map);map.fitBounds(r.getBounds().pad(.1));}})");
        sb.append(".catch(()=>{var ll=wp.map(w=>[w[1],w[0]]);L.polyline(ll,{color:'#2563eb',weight:4,dashArray:'8 5'}).addTo(map);map.fitBounds(L.polyline(ll).getBounds().pad(.1));});");
        sb.append("</script></body></html>");
        return sb.toString();
    }

    private static String lMarker(double lat, double lon, String color, String label, String title, String popup) {
        return "L.marker(["+lat+","+lon+"],{icon:pwIcon('"+color+"','"+label+"'),title:'"+jsEsc(title)+"'}).addTo(map).bindPopup('"+jsEsc(popup)+"');\n";
    }
    private static String lCircle(double lat, double lon, String color, String title, String popup) {
        return "L.circleMarker(["+lat+","+lon+"],{radius:8,color:'"+color+"',fillColor:'"+color+"',fillOpacity:.7,weight:2}).addTo(map).bindPopup('"+jsEsc(popup)+"').bindTooltip('"+jsEsc(title)+"');\n";
    }

    // ── Hosted Vercel map viewer ──────────────────────────────────

    private static void addMapPoint(StringBuilder points, double lat, double lon, String title, String popup) {
        if (points.length() > 0) points.append(",");
        points.append("{\"lat\":").append(lat)
            .append(",\"lon\":").append(lon)
            .append(",\"title\":\"").append(jsonEsc(title))
            .append("\",\"popup\":\"").append(jsonEsc(popup)).append("\"}");
    }

    private static void appendRoutePoint(StringBuilder route, double lon, double lat) {
        if (route.length() == 0) route.append("[");
        else route.append(",");
        route.append(routePoint(lon, lat));
    }

    private static String routePoint(double lon, double lat) {
        return "[" + lon + "," + lat + "]";
    }

    private static String jsonEsc(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "")
            .replace("\n", "\\n")
            .replace("\t", "\\t");
    }

    private static void openHostedMap(String title, String subtitle, double lat, double lon,
            int zoom, String pointsJson, String routeJson) throws Exception {
        String data = "{\"title\":\"" + jsonEsc(title)
            + "\",\"subtitle\":\"" + jsonEsc(subtitle)
            + "\",\"center\":[" + lat + "," + lon + "]"
            + ",\"zoom\":" + zoom
            + ",\"points\":[" + pointsJson + "]"
            + ",\"route\":" + routeJson + "]}";
        String url = HOSTED_MAP_URL + "?data="
            + java.net.URLEncoder.encode(data, StandardCharsets.UTF_8);
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(java.net.URI.create(url));
        } else {
            throw new IOException("No supported browser is available to open the hosted map.");
        }
        LOG.info("Opened hosted POOLWISE map: " + url);
    }

    // ── Geometry ──────────────────────────────────────────────────

    public static double[] nearestPointOnSegment(
            double lat1, double lon1, double lat2, double lon2, double qLat, double qLon) {
        double dx = lat2-lat1, dy = lon2-lon1;
        double lenSq = dx*dx + dy*dy;
        if (lenSq < 1e-12) return new double[]{lat1, lon1};
        double t = Math.max(0, Math.min(1, ((qLat-lat1)*dx + (qLon-lon1)*dy) / lenSq));
        return new double[]{lat1+t*dx, lon1+t*dy};
    }

    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2-lat1), dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                 + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon/2)*Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    // ── I/O ───────────────────────────────────────────────────────

    private static void openHtml(String html, String name) throws Exception {
        Path dir = Paths.get(System.getProperty("user.dir"), "data", "maps");
        Files.createDirectories(dir);
        Path file = dir.resolve(name + ".html");
        Files.writeString(file, html, StandardCharsets.UTF_8);
        LOG.info("Map saved: " + file.toAbsolutePath());
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
            Desktop.getDesktop().browse(file.toUri());
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }
    private static String jsEsc(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("'","\\'").replace("\n","<br>").replace("\r","");
    }
}
