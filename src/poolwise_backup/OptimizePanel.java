package poolwise;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Optimize panel — select mode, run optimizer, display results with carpool groups.
 */
public class OptimizePanel extends JPanel {

    private MainFrame main;
    private List<Driver>    drivers;
    private List<Passenger> passengers;

    private JPanel resultsArea;
    private JComboBox<String> modeCombo;
    private JLabel  summaryLabel;

    public OptimizePanel(List<Driver> drivers, List<Passenger> passengers, MainFrame main) {
        this.drivers    = drivers;
        this.passengers = passengers;
        this.main       = main;

        setBackground(UIConstants.BG_LIGHT);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setOpaque(false);
        h.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = UIConstants.makeLabel("Run Optimization", UIConstants.FONT_TITLE, UIConstants.TEXT_DARK);
        JLabel sub   = UIConstants.makeLabel("Select a mode and generate the best carpool plan", UIConstants.FONT_BODY, UIConstants.TEXT_GRAY);
        JPanel left  = new JPanel(); left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title); left.add(Box.createVerticalStrut(2)); left.add(sub);
        h.add(left, BorderLayout.WEST);

        // Mode selector + Run button
        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        ctrl.setOpaque(false);

        modeCombo = new JComboBox<>(new String[]{
            "⚖️  Balanced",
            "🚗  Minimum Cars",
            "⛽  Minimum Distance",
            "⏱️  Minimum Time",
            "🌱  Minimum CO₂"
        });
        modeCombo.setFont(UIConstants.FONT_BODY);
        modeCombo.setPreferredSize(new Dimension(200, 38));

        JButton runBtn = UIConstants.makePrimaryButton("▶  Run Optimizer");
        runBtn.setPreferredSize(new Dimension(170, 38));
        runBtn.addActionListener(e -> runOptimization());

        ctrl.add(UIConstants.makeLabel("Mode:", UIConstants.FONT_SUB, UIConstants.TEXT_DARK));
        ctrl.add(modeCombo);
        ctrl.add(runBtn);
        h.add(ctrl, BorderLayout.EAST);

        return h;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);

        // Summary bar
        summaryLabel = UIConstants.makeLabel("Run an optimization to see results.", UIConstants.FONT_BODY, UIConstants.TEXT_GRAY);
        JPanel summaryBar = UIConstants.makeCard(10, 10);
        summaryBar.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 10));
        summaryBar.add(summaryLabel);
        summaryBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Results area (scrollable)
        resultsArea = new JPanel();
        resultsArea.setOpaque(false);
        resultsArea.setLayout(new BoxLayout(resultsArea, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(resultsArea);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);

        body.add(summaryBar, BorderLayout.NORTH);
        body.add(scroll, BorderLayout.CENTER);

        JPanel wrap = new JPanel(new BorderLayout(0, 10));
        wrap.setOpaque(false);
        wrap.add(summaryBar, BorderLayout.NORTH);
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private void runOptimization() {
        int idx = modeCombo.getSelectedIndex();
        OptimizationMode[] modes = {
            OptimizationMode.BALANCED,
            OptimizationMode.MIN_CARS,
            OptimizationMode.MIN_DISTANCE,
            OptimizationMode.MIN_TIME,
            OptimizationMode.MIN_CO2
        };
        OptimizationMode mode = modes[idx];

        CarpoolPlan plan = CarpoolOptimizer.optimize(
            new java.util.ArrayList<>(drivers),
            new java.util.ArrayList<>(passengers),
            mode);

        // Update summary
        summaryLabel.setText(String.format(
            "✅  Mode: %s   |   Groups: %d   |   Passengers Served: %d   |   Cars Avoided: %d   |   Extra Distance: %.2f km   |   CO₂ Saved: %.2f kg   |   Fuel Saved: %.2f L",
            mode.getDisplayName(),
            plan.getCarsUsed(),
            plan.getTotalPassengersServed(),
            plan.getCarsAvoided(),
            plan.getTotalExtraDistance(),
            plan.getTotalCO2Saved(),
            plan.getTotalFuelSaved()));
        summaryLabel.setForeground(UIConstants.SUCCESS);

        // Clear and rebuild results
        resultsArea.removeAll();
        resultsArea.add(Box.createVerticalStrut(14));

        if (plan.getGroups().isEmpty()) {
            JLabel none = UIConstants.makeLabel("No compatible groups could be formed with current data.", UIConstants.FONT_BODY, UIConstants.TEXT_GRAY);
            none.setAlignmentX(Component.LEFT_ALIGNMENT);
            resultsArea.add(none);
        }

        for (CarpoolGroup g : plan.getGroups()) {
            resultsArea.add(buildGroupCard(g));
            resultsArea.add(Box.createVerticalStrut(14));
        }

        if (!plan.getUnassignedPassengers().isEmpty()) {
            resultsArea.add(buildUnassignedCard(plan.getUnassignedPassengers()));
            resultsArea.add(Box.createVerticalStrut(14));
        }

        resultsArea.add(buildImpactCard(plan));
        resultsArea.add(Box.createVerticalStrut(14));

        resultsArea.revalidate();
        resultsArea.repaint();
    }

    private JPanel buildGroupCard(CarpoolGroup g) {
        JPanel card = UIConstants.makeCard(14, 14);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        // Left: driver info
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setPreferredSize(new Dimension(200, 0));

        JLabel badge = new JLabel("🚗  " + g.getGroupId());
        badge.setFont(UIConstants.FONT_SUB);
        badge.setForeground(UIConstants.ACCENT);
        badge.setOpaque(true);
        badge.setBackground(new Color(204, 251, 241));
        badge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        JLabel driverLbl = UIConstants.makeLabel(g.getDriver().getName(), UIConstants.FONT_HEADING, UIConstants.TEXT_DARK);
        JLabel fromLbl   = UIConstants.makeLabel("From: " + g.getDriver().getStartLocation().getName(), UIConstants.FONT_SMALL, UIConstants.TEXT_GRAY);
        JLabel toLbl     = UIConstants.makeLabel("To:   " + g.getDriver().getDestination().getName(),   UIConstants.FONT_SMALL, UIConstants.TEXT_GRAY);
        JLabel seatLbl   = UIConstants.makeLabel("Seats: " + g.getPassengerCount() + "/" + g.getDriver().getAvailableSeats(), UIConstants.FONT_SMALL, UIConstants.TEXT_GRAY);

        left.add(badge);
        left.add(Box.createVerticalStrut(8));
        left.add(driverLbl);
        left.add(Box.createVerticalStrut(2));
        left.add(fromLbl);
        left.add(toLbl);
        left.add(seatLbl);
        card.add(left, BorderLayout.WEST);

        // Middle: passengers + route
        JPanel mid = new JPanel();
        mid.setOpaque(false);
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));

        mid.add(UIConstants.makeLabel("Passengers", UIConstants.FONT_SUB, UIConstants.TEXT_DARK));
        mid.add(Box.createVerticalStrut(6));
        for (Passenger ps : g.getPassengers()) {
            JLabel pl = UIConstants.makeLabel("  👤 " + ps.getName() + "  (" + ps.getStartLocation().getName() + ")", UIConstants.FONT_BODY, UIConstants.TEXT_DARK);
            mid.add(pl);
        }

        // Route
        if (!g.getRouteOrder().isEmpty()) {
            mid.add(Box.createVerticalStrut(8));
            StringBuilder routeStr = new StringBuilder("Route: ");
            for (int i = 0; i < g.getRouteOrder().size(); i++) {
                routeStr.append(g.getRouteOrder().get(i).getName());
                if (i < g.getRouteOrder().size() - 1) routeStr.append(" → ");
            }
            JLabel routeLbl = UIConstants.makeLabel(routeStr.toString(), UIConstants.FONT_SMALL, UIConstants.TEXT_GRAY);
            mid.add(routeLbl);
        }
        card.add(mid, BorderLayout.CENTER);

        // Right: stats
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(150, 0));

        addStatRow(right, "Extra km",    String.format("%.2f km",  g.getTotalExtraDistanceKm()),  UIConstants.ACCENT);
        addStatRow(right, "CO₂ Saved",   String.format("%.2f kg",  g.getEstimatedCO2SavedKg()),   UIConstants.SUCCESS);
        addStatRow(right, "Fuel Saved",  String.format("%.2f L",   g.getFuelSavedLiters()),        UIConstants.WARNING);
        card.add(right, BorderLayout.EAST);

        return card;
    }

    private void addStatRow(JPanel parent, String label, String value, Color valColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        row.add(UIConstants.makeLabel(label, UIConstants.FONT_SMALL, UIConstants.TEXT_GRAY), BorderLayout.WEST);
        JLabel valLbl = UIConstants.makeLabel(value, UIConstants.FONT_SUB, valColor);
        valLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(valLbl, BorderLayout.EAST);
        parent.add(row);
    }

    private JPanel buildUnassignedCard(List<Passenger> unassigned) {
        JPanel card = UIConstants.makeCard(14, 14);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = UIConstants.makeLabel("⚠  Unassigned Passengers", UIConstants.FONT_HEADING, UIConstants.DANGER);
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        for (Passenger p : unassigned) {
            card.add(UIConstants.makeLabel("  • " + p.getName() + " — no compatible driver found", UIConstants.FONT_BODY, UIConstants.TEXT_DARK));
        }
        return card;
    }

    private JPanel buildImpactCard(CarpoolPlan plan) {
        JPanel card = UIConstants.makeCard(14, 14);
        card.setOpaque(false);
        card.setLayout(new GridLayout(1, 4, 0, 0));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        addImpact(card, String.valueOf(plan.getCarsAvoided()),                           "Cars Avoided",        UIConstants.ACCENT);
        addImpact(card, String.format("%.2f km", plan.getTotalExtraDistance()),          "Total Extra Distance", UIConstants.WARNING);
        addImpact(card, String.format("%.2f kg", plan.getTotalCO2Saved()),               "CO₂ Saved",           UIConstants.SUCCESS);
        addImpact(card, String.format("%.2f L",  plan.getTotalFuelSaved()),              "Fuel Saved",          UIConstants.DANGER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = UIConstants.makeLabel("🌱  Environmental Impact Summary", UIConstants.FONT_HEADING, UIConstants.TEXT_DARK);
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titleBar.setOpaque(false);
        titleBar.add(title);

        JPanel outer = UIConstants.makeCard(14, 14);
        outer.setLayout(new BorderLayout(0, 10));
        outer.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.add(title, BorderLayout.NORTH);
        outer.add(card,  BorderLayout.CENTER);
        return outer;
    }

    private void addImpact(JPanel parent, String value, String label, Color color) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valLbl.setForeground(color);
        valLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblLbl = UIConstants.makeLabel(label, UIConstants.FONT_SMALL, UIConstants.TEXT_GRAY);
        lblLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(valLbl);
        p.add(Box.createVerticalStrut(2));
        p.add(lblLbl);
        parent.add(p);
    }

    public void refresh(List<Driver> d, List<Passenger> p) {
        this.drivers    = d;
        this.passengers = p;
    }
}
