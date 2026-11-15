package com.poolwise.ui;

import com.poolwise.model.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Map & Route Visualiser Panel.
 *
 * Supports two map modes:
 *  1. Google Maps JS API  — real GPS satellite/street map, Directions API routing
 *                           (requires free API key from console.cloud.google.com)
 *  2. OpenStreetMap/Leaflet — free fallback, no key needed, real OSM tiles + OSRM routing
 *
 * API key is stored in Java Preferences and persists between app restarts.
 */
public class MapPanel extends JPanel {

    private final MainWindow main;
    private JPanel carpoolListArea;
    private JLabel statusLbl;
    private JLabel apiKeyStatusLbl;

    public MapPanel(MainWindow main) {
        this.main = main;
        setBackground(Theme.BG_PAGE);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JPanel left = new JPanel(); left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(Theme.label("Map & Route Visualiser", Theme.F_TITLE, Theme.TEXT_MAIN));
        left.add(Box.createVerticalStrut(2));
        left.add(Theme.label(
            "View carpool routes on an interactive map  •  Google Maps (with API key) or OpenStreetMap (free)",
            Theme.F_BODY, Theme.TEXT_SUB));
        p.add(left, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);

        JButton keyBtn = new JButton("🔑 Set Google Maps API Key");
        keyBtn.setFont(Theme.F_SMALL);
        keyBtn.setForeground(Theme.PRIMARY);
        keyBtn.setBackground(Theme.PRIMARY_LIGHT);
        keyBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.PRIMARY, 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        keyBtn.setFocusPainted(false);
        keyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        keyBtn.addActionListener(e -> showApiKeyDialog());

        JButton refreshBtn = Theme.primaryButton("⟳ Refresh");
        refreshBtn.setPreferredSize(new Dimension(110, 34));
        refreshBtn.addActionListener(e -> refresh());

        btnRow.add(keyBtn);
        btnRow.add(refreshBtn);
        p.add(btnRow, BorderLayout.EAST);
        return p;
    }

    // ── Body ──────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);

        body.add(buildModeBanner(), BorderLayout.NORTH);

        carpoolListArea = new JPanel();
        carpoolListArea.setOpaque(false);
        carpoolListArea.setLayout(new BoxLayout(carpoolListArea, BoxLayout.Y_AXIS));

        JScrollPane sp = new JScrollPane(carpoolListArea);
        sp.setOpaque(false); sp.getViewport().setOpaque(false); sp.setBorder(null);
        body.add(sp, BorderLayout.CENTER);

        statusLbl = Theme.label("", Theme.F_SMALL, Theme.TEXT_SUB);
        body.add(statusLbl, BorderLayout.SOUTH);
        return body;
    }

    // ── Mode banner ───────────────────────────────────────────────
    private JPanel buildModeBanner() {
        boolean hasKey = MapGenerator.hasApiKey();

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hasKey ? new Color(220, 252, 231) : new Color(219, 234, 254));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(hasKey ? new Color(134, 239, 172) : new Color(147, 197, 253));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 10, 10));
                g2.dispose(); super.paintComponent(g);
            }
            { setOpaque(false); }
        };
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        Color textColor = hasKey ? new Color(21, 128, 61) : new Color(29, 78, 216);
        String mode = hasKey
            ? "🟢  Google Maps mode — real GPS satellite/street view with Directions API routing"
            : "🔵  OpenStreetMap mode — free map tiles + OSRM road routing (no API key needed)";
        String hint = hasKey
            ? "API key set  •  Click maps to open in browser with full Google Maps satellite view"
            : "No API key set  •  Click '🔑 Set Google Maps API Key' above for real GPS map  •  OpenStreetMap is active";

        apiKeyStatusLbl = Theme.label(mode, Theme.F_SUB, textColor);
        JLabel hintLbl   = Theme.label(hint, Theme.F_SMALL, Theme.TEXT_SUB);

        JPanel txt = new JPanel(); txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        txt.add(apiKeyStatusLbl); txt.add(Box.createVerticalStrut(2)); txt.add(hintLbl);
        card.add(txt, BorderLayout.CENTER);
        return card;
    }

    // ── Refresh ───────────────────────────────────────────────────
    public void refresh() {
        carpoolListArea.removeAll();
        carpoolListArea.add(Box.createVerticalStrut(10));

        List<Carpool>   carpools = main.matchingService.getLastCarpools();
        List<Driver>    drivers  = main.userService.getAllDrivers();
        List<Passenger> allPax   = main.userService.getAllPassengers();

        // ── Carpool route maps ────────────────────────────────────
        addSectionHeader("🚗  Carpool Route Maps");
        if (carpools.isEmpty()) {
            JLabel warn = Theme.label(
                "⚠  No carpool groups yet — go to 'Find Carpool', click FIND BEST CARPOOL, then return here.",
                Theme.F_BODY, Theme.WARNING);
            warn.setAlignmentX(Component.LEFT_ALIGNMENT);
            warn.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            carpoolListArea.add(warn);
        } else {
            for (Carpool cp : carpools) {
                carpoolListArea.add(buildCarpoolCard(cp));
                carpoolListArea.add(Box.createVerticalStrut(10));
            }
        }

        // ── Driver overview maps ──────────────────────────────────
        carpoolListArea.add(Box.createVerticalStrut(6));
        addSectionHeader("👤  Driver Route Overview (with nearby passengers)");
        if (drivers.isEmpty()) {
            carpoolListArea.add(Theme.label("No drivers registered yet.", Theme.F_BODY, Theme.TEXT_SUB));
        } else {
            for (Driver d : drivers) {
                carpoolListArea.add(buildDriverCard(d, allPax));
                carpoolListArea.add(Box.createVerticalStrut(8));
            }
        }

        // ── Meeting-point finder ──────────────────────────────────
        carpoolListArea.add(Box.createVerticalStrut(6));
        addSectionHeader("🤝  Custom Meeting-Point Finder");
        carpoolListArea.add(buildMeetingPointFinder(drivers, allPax));
        carpoolListArea.add(Box.createVerticalStrut(14));

        boolean hasKey = MapGenerator.hasApiKey();
        statusLbl.setText(hasKey
            ? "Google Maps mode — maps open in browser with real satellite/street view"
            : "OpenStreetMap mode — real map tiles via OpenStreetMap, routing via OSRM (both free)");
        statusLbl.setForeground(hasKey ? Theme.SUCCESS : Theme.PRIMARY);

        carpoolListArea.revalidate();
        carpoolListArea.repaint();
    }

    // ── Carpool card ──────────────────────────────────────────────
    private JPanel buildCarpoolCard(Carpool cp) {
        JPanel card = Theme.card(12);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        Driver d = cp.getDriver();

        // Info
        JPanel info = new JPanel(); info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(Theme.label("🚗 " + cp.getCarpoolId() + "  —  " + d.getName(),
            Theme.F_HEADING, Theme.PRIMARY));
        info.add(Box.createVerticalStrut(3));
        info.add(Theme.label("Route: " + d.getStartLocation() + "  →  " + d.getDestination(),
            Theme.F_BODY, Theme.TEXT_MAIN));

        String paxNames = cp.getPassengers().stream()
            .map(p -> p.getName() + " (" + p.getPickupLocation() + ")")
            .reduce("", (a, b) -> a.isEmpty() ? b : a + ",  " + b);
        info.add(Theme.label("Passengers: " + (paxNames.isEmpty() ? "none" : paxNames),
            Theme.F_SMALL, Theme.TEXT_SUB));
        info.add(Theme.label(String.format(
            "Extra distance: %.2f km  |  CO₂ saved: %.2f kg  |  Departs: %s",
            cp.getExtraDistanceKm(), cp.getEstimatedCO2SavedKg(), d.getDepartureTime()),
            Theme.F_SMALL, Theme.TEXT_SUB));
        card.add(info, BorderLayout.CENTER);

        // Buttons
        JPanel btns = new JPanel();
        btns.setOpaque(false);
        btns.setLayout(new BoxLayout(btns, BoxLayout.Y_AXIS));

        JButton routeBtn = Theme.primaryButton("🗺️  View Route");
        routeBtn.setPreferredSize(new Dimension(155, 36));
        routeBtn.setMaximumSize(new Dimension(155, 36));
        routeBtn.addActionListener(e -> {
            statusLbl.setText("Opening map in browser…");
            new Thread(() -> MapGenerator.openCarpoolMap(cp), "map-open").start();
        });

        JButton meetBtn = new JButton("🤝  Meeting Points");
        styleSecondaryBtn(meetBtn, Theme.SUCCESS);
        meetBtn.addActionListener(e -> {
            if (cp.getPassengers().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No passengers in this carpool.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            showPassengerPicker(cp);
        });

        btns.add(routeBtn);
        btns.add(Box.createVerticalStrut(6));
        btns.add(meetBtn);
        card.add(btns, BorderLayout.EAST);
        return card;
    }

    // ── Driver overview card ──────────────────────────────────────
    private JPanel buildDriverCard(Driver d, List<Passenger> allPax) {
        JPanel card = Theme.card(12);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel info = new JPanel(); info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(Theme.label("🚗 " + d.getName(), Theme.F_SUB, Theme.TEXT_MAIN));
        info.add(Theme.label(
            d.getStartLocation() + "  →  " + d.getDestination()
            + "  |  Departs: " + d.getDepartureTime()
            + "  |  Seats: " + d.getAvailableSeats(),
            Theme.F_SMALL, Theme.TEXT_SUB));
        card.add(info, BorderLayout.CENTER);

        JButton btn = Theme.primaryButton("🗺️  Map + Nearby Passengers");
        btn.setPreferredSize(new Dimension(230, 36));
        btn.addActionListener(e -> {
            statusLbl.setText("Opening driver overview map…");
            List<Passenger> nearby = allPax.stream()
                .filter(p -> isNearRoute(d, p, d.getMaxDetourKm() + 5))
                .toList();
            new Thread(() -> MapGenerator.openDriverOverviewMap(d, nearby), "map-driver").start();
        });
        card.add(btn, BorderLayout.EAST);
        return card;
    }

    // ── Meeting-point finder ──────────────────────────────────────
    private JPanel buildMeetingPointFinder(List<Driver> drivers, List<Passenger> passengers) {
        JPanel card = Theme.card(12);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel desc = Theme.label(
            "Pick any driver and passenger to calculate and map the ideal meeting point on the driver's route.",
            Theme.F_SMALL, Theme.TEXT_SUB);
        card.add(desc, BorderLayout.NORTH);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);

        JComboBox<String> driverCB = new JComboBox<>();
        driverCB.setFont(Theme.F_BODY);
        driverCB.setPreferredSize(new Dimension(195, 34));
        drivers.forEach(d -> driverCB.addItem(d.getName() + "  [" + d.getUserId() + "]"));

        JComboBox<String> paxCB = new JComboBox<>();
        paxCB.setFont(Theme.F_BODY);
        paxCB.setPreferredSize(new Dimension(195, 34));
        passengers.forEach(p -> paxCB.addItem(p.getName() + "  [" + p.getUserId() + "]"));

        JButton findBtn = Theme.primaryButton("🤝  Find Meeting Point");
        findBtn.setPreferredSize(new Dimension(195, 36));
        findBtn.addActionListener(e -> {
            if (drivers.isEmpty() || passengers.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please add at least one driver and one passenger first.");
                return;
            }
            int di = driverCB.getSelectedIndex();
            int pi = paxCB.getSelectedIndex();
            if (di < 0 || pi < 0) return;
            Driver    sel_d = drivers.get(di);
            Passenger sel_p = passengers.get(pi);
            statusLbl.setText("Opening meeting-point map for " + sel_d.getName() + " & " + sel_p.getName() + "…");
            new Thread(() -> MapGenerator.openMeetingPointMap(sel_d, sel_p), "map-meet").start();
        });

        row.add(Theme.label("Driver:", Theme.F_SMALL, Theme.TEXT_MAIN));
        row.add(driverCB);
        row.add(Box.createHorizontalStrut(4));
        row.add(Theme.label("Passenger:", Theme.F_SMALL, Theme.TEXT_MAIN));
        row.add(paxCB);
        row.add(Box.createHorizontalStrut(4));
        row.add(findBtn);
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    // ── API key dialog ────────────────────────────────────────────
    private void showApiKeyDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            "Google Maps API Key", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(560, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel body = new JPanel();
        body.setBackground(Theme.BG_PAGE);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));

        body.add(Theme.label("Enter Your Google Maps API Key", Theme.F_HEADING, Theme.TEXT_MAIN));
        body.add(Box.createVerticalStrut(8));

        // Instructions
        String[] steps = {
            "1. Go to  console.cloud.google.com  and sign in",
            "2. Create a project → Enable 'Maps JavaScript API' and 'Directions API'",
            "3. Go to APIs & Services → Credentials → Create API Key",
            "4. Paste the key below (starts with 'AIza...')"
        };
        for (String s : steps) {
            JLabel l = Theme.label(s, Theme.F_SMALL, Theme.TEXT_SUB);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            body.add(l);
        }
        body.add(Box.createVerticalStrut(14));

        JTextField keyField = Theme.field(30);
        keyField.setText(MapGenerator.getApiKey());
        keyField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        keyField.setAlignmentX(Component.LEFT_ALIGNMENT);
        keyField.setFont(new Font("Monospaced", Font.PLAIN, 13));
        body.add(keyField);
        body.add(Box.createVerticalStrut(8));

        JLabel hint = Theme.label(
            "Leave blank to use OpenStreetMap mode (free, no key needed).",
            Theme.F_SMALL, Theme.TEXT_SUB);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(hint);
        dlg.add(body, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btns.setBackground(Theme.BG_PAGE);
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        JButton clearBtn = new JButton("Clear Key");
        clearBtn.setFont(Theme.F_BODY);
        clearBtn.addActionListener(e -> {
            keyField.setText("");
            MapGenerator.setApiKey("");
            dlg.dispose();
            rebuild();
            JOptionPane.showMessageDialog(this,
                "API key cleared. Now using OpenStreetMap mode (free).",
                "Key Cleared", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(Theme.F_BODY);
        cancelBtn.addActionListener(e -> dlg.dispose());

        JButton saveBtn = Theme.primaryButton("Save & Apply");
        saveBtn.addActionListener(e -> {
            String key = keyField.getText().trim();
            MapGenerator.setApiKey(key);
            dlg.dispose();
            rebuild();
            if (key.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No key set — using free OpenStreetMap mode.",
                    "OpenStreetMap Mode", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Google Maps API key saved!\nAll map buttons will now open real Google Maps.",
                    "Key Saved", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btns.add(clearBtn); btns.add(cancelBtn); btns.add(saveBtn);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── Helpers ───────────────────────────────────────────────────

    /** Rebuild the entire panel (banner + list) after API key change. */
    private void rebuild() {
        removeAll();
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
        refresh();
        revalidate(); repaint();
    }

    private void showPassengerPicker(Carpool cp) {
        List<Passenger> passengers = cp.getPassengers();
        String[] names = passengers.stream()
            .map(p -> p.getName() + " (" + p.getPickupLocation() + ")")
            .toArray(String[]::new);
        String chosen = (String) JOptionPane.showInputDialog(
            this, "Select passenger for meeting-point map:",
            "Choose Passenger", JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
        if (chosen == null) return;
        int idx = java.util.Arrays.asList(names).indexOf(chosen);
        if (idx < 0) return;
        Passenger sel = passengers.get(idx);
        statusLbl.setText("Opening meeting-point map for " + sel.getName() + "…");
        new Thread(() -> MapGenerator.openMeetingPointMap(cp.getDriver(), sel), "map-meet").start();
    }

    private void addSectionHeader(String title) {
        JLabel lbl = Theme.label(title, Theme.F_HEADING, Theme.PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 0, 6, 0));
        carpoolListArea.add(lbl);
    }

    private void styleSecondaryBtn(JButton btn, Color color) {
        btn.setFont(Theme.F_BODY);
        btn.setForeground(color);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createLineBorder(color, 1));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(155, 32));
        btn.setMaximumSize(new Dimension(155, 32));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private boolean isNearRoute(Driver d, Passenger p, double maxKm) {
        double[] meet = MapGenerator.nearestPointOnSegment(
            d.getStartLat(), d.getStartLon(), d.getDestLat(), d.getDestLon(),
            p.getPickupLat(), p.getPickupLon());
        return MapGenerator.haversine(
            p.getPickupLat(), p.getPickupLon(), meet[0], meet[1]) <= maxKm;
    }
}
