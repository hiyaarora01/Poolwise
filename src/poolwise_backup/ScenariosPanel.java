package poolwise;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Scenarios panel — run 3 dynamic scenarios and compare all 5 optimization modes.
 */
public class ScenariosPanel extends JPanel {

    private MainFrame main;
    private List<Driver>    drivers;
    private List<Passenger> passengers;
    private JPanel outputPanel;

    public ScenariosPanel(List<Driver> drivers, List<Passenger> passengers, MainFrame main) {
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
        JPanel h = new JPanel();
        h.setOpaque(false);
        h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
        h.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        h.add(UIConstants.makeLabel("Dynamic Scenarios", UIConstants.FONT_TITLE, UIConstants.TEXT_DARK));
        h.add(Box.createVerticalStrut(2));
        h.add(UIConstants.makeLabel("Test how the optimizer responds to real-world changes", UIConstants.FONT_BODY, UIConstants.TEXT_GRAY));
        return h;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);

        // Left: scenario buttons
        JPanel btnPanel = UIConstants.makeCard(14, 14);
        btnPanel.setPreferredSize(new Dimension(260, 0));
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));

        btnPanel.add(UIConstants.makeLabel("Select a Scenario", UIConstants.FONT_HEADING, UIConstants.TEXT_DARK));
        btnPanel.add(Box.createVerticalStrut(16));

        addScenarioBtn(btnPanel, "🌞", "Normal Day",
            "All drivers available\nBaseline optimization",
            UIConstants.SUCCESS, () -> runScenario1());

        btnPanel.add(Box.createVerticalStrut(10));

        addScenarioBtn(btnPanel, "⚠️", "Driver Cancels",
            "First driver becomes unavailable\nSystem auto-recalculates",
            UIConstants.DANGER, () -> runScenario2());

        btnPanel.add(Box.createVerticalStrut(10));

        addScenarioBtn(btnPanel, "📉", "Reduced Detour",
            "Max detour capped at 2 km\nFewer pickups allowed",
            UIConstants.WARNING, () -> runScenario3());

        btnPanel.add(Box.createVerticalStrut(20));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        btnPanel.add(sep);
        btnPanel.add(Box.createVerticalStrut(14));

        addScenarioBtn(btnPanel, "📊", "Compare All Modes",
            "Run all 5 optimization modes\nSide-by-side comparison",
            UIConstants.ACCENT, () -> runComparison());

        body.add(btnPanel, BorderLayout.WEST);

        // Right: output
        outputPanel = new JPanel();
        outputPanel.setOpaque(false);
        outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(outputPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        body.add(scroll, BorderLayout.CENTER);

        showPlaceholder();
        return body;
    }

    private void addScenarioBtn(JPanel parent, String icon, String title, String desc, Color color, Runnable action) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
            { setOpaque(false); }
        };
        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        card.add(iconLbl, BorderLayout.WEST);

        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        txt.add(UIConstants.makeLabel(title, UIConstants.FONT_SUB, UIConstants.TEXT_DARK));
        for (String line : desc.split("\n")) {
            txt.add(UIConstants.makeLabel(line, UIConstants.FONT_SMALL, UIConstants.TEXT_GRAY));
        }
        card.add(txt, BorderLayout.CENTER);

        JButton btn = UIConstants.makePrimaryButton("Run");
        btn.setPreferredSize(new Dimension(60, 30));
        btn.addActionListener(e -> action.run());
        card.add(btn, BorderLayout.EAST);

        parent.add(card);
    }

    // ── Scenario runners ─────────────────────────────────────────

    private void runScenario1() {
        for (Driver d : drivers) d.setAvailable(true);
        CarpoolPlan plan = CarpoolOptimizer.optimize(
            new java.util.ArrayList<>(drivers),
            new java.util.ArrayList<>(passengers),
            OptimizationMode.BALANCED);
        showResults("🌞 Scenario 1 — Normal Day (All Drivers Available)", plan,
            UIConstants.SUCCESS, "All " + drivers.size() + " drivers available. Baseline optimization.");
    }

    private void runScenario2() {
        for (Driver d : drivers) d.setAvailable(true);
        if (!drivers.isEmpty()) {
            Driver cancelled = drivers.get(0);
            cancelled.setAvailable(false);
            CarpoolPlan plan = CarpoolOptimizer.optimize(
                new java.util.ArrayList<>(drivers),
                new java.util.ArrayList<>(passengers),
                OptimizationMode.BALANCED);
            for (Driver d : drivers) d.setAvailable(true); // restore
            showResults("⚠️ Scenario 2 — Driver Cancelled: " + cancelled.getName(), plan,
                UIConstants.DANGER, cancelled.getName() + " is unavailable. System auto-recalculated with remaining drivers.");
        }
    }

    private void runScenario3() {
        for (Driver d : drivers) d.setAvailable(true);
        // Save original detours
        double[] origDetours = drivers.stream().mapToDouble(Driver::getMaxDetourKm).toArray();
        for (Driver d : drivers) d.setMaxDetourKm(Math.min(d.getMaxDetourKm(), 2.0));
        CarpoolPlan plan = CarpoolOptimizer.optimize(
            new java.util.ArrayList<>(drivers),
            new java.util.ArrayList<>(passengers),
            OptimizationMode.BALANCED);
        // Restore
        for (int i = 0; i < drivers.size(); i++) drivers.get(i).setMaxDetourKm(origDetours[i]);
        showResults("📉 Scenario 3 — Reduced Max Detour (≤ 2 km)", plan,
            UIConstants.WARNING, "All drivers restricted to max 2 km detour. Fewer pickups are possible.");
    }

    private void runComparison() {
        for (Driver d : drivers) d.setAvailable(true);
        outputPanel.removeAll();
        outputPanel.add(Box.createVerticalStrut(10));
        outputPanel.add(UIConstants.makeLabel("📊  Comparing All Optimization Modes", UIConstants.FONT_TITLE, UIConstants.TEXT_DARK));
        outputPanel.add(Box.createVerticalStrut(6));
        outputPanel.add(UIConstants.makeLabel("Same drivers and passengers — different objectives", UIConstants.FONT_BODY, UIConstants.TEXT_GRAY));
        outputPanel.add(Box.createVerticalStrut(20));

        OptimizationMode[] modes = OptimizationMode.values();
        String[] modeIcons = {"🚗", "⛽", "⏱️", "🌱", "⚖️"};
        Color[] colors = {UIConstants.ACCENT, UIConstants.WARNING, UIConstants.SUCCESS, new Color(34,197,94), UIConstants.DANGER};

        for (int i = 0; i < modes.length; i++) {
            CarpoolPlan plan = CarpoolOptimizer.optimize(
                new java.util.ArrayList<>(drivers),
                new java.util.ArrayList<>(passengers),
                modes[i]);

            JPanel row = UIConstants.makeCard(12, 12);
            row.setLayout(new GridLayout(1, 5, 10, 0));
            row.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

            addCompareCell(row, modeIcons[i] + " " + modes[i].getDisplayName(), "Mode",           Color.DARK_GRAY, true);
            addCompareCell(row, String.valueOf(plan.getCarsUsed()),               "Groups Formed",  colors[i], false);
            addCompareCell(row, String.valueOf(plan.getCarsAvoided()),            "Cars Avoided",   UIConstants.ACCENT, false);
            addCompareCell(row, String.format("%.2f km", plan.getTotalExtraDistance()), "Extra km",UIConstants.WARNING, false);
            addCompareCell(row, String.format("%.2f kg", plan.getTotalCO2Saved()),"CO₂ Saved",     UIConstants.SUCCESS, false);

            outputPanel.add(row);
            outputPanel.add(Box.createVerticalStrut(10));
        }
        outputPanel.revalidate();
        outputPanel.repaint();
    }

    private void addCompareCell(JPanel parent, String value, String label, Color valColor, boolean leftAlign) {
        JPanel cell = new JPanel();
        cell.setOpaque(false);
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(leftAlign ? UIConstants.FONT_HEADING : new Font("Segoe UI", Font.BOLD, 18));
        valLbl.setForeground(valColor);
        valLbl.setAlignmentX(leftAlign ? Component.LEFT_ALIGNMENT : Component.CENTER_ALIGNMENT);
        JLabel lblLbl = UIConstants.makeLabel(label, UIConstants.FONT_SMALL, UIConstants.TEXT_GRAY);
        lblLbl.setAlignmentX(leftAlign ? Component.LEFT_ALIGNMENT : Component.CENTER_ALIGNMENT);
        cell.add(valLbl);
        cell.add(Box.createVerticalStrut(2));
        cell.add(lblLbl);
        parent.add(cell);
    }

    private void showResults(String scenarioTitle, CarpoolPlan plan, Color titleColor, String note) {
        outputPanel.removeAll();
        outputPanel.add(Box.createVerticalStrut(10));

        JLabel titleLbl = UIConstants.makeLabel(scenarioTitle, UIConstants.FONT_HEADING, titleColor);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        outputPanel.add(titleLbl);
        outputPanel.add(Box.createVerticalStrut(4));
        JLabel noteLbl = UIConstants.makeLabel(note, UIConstants.FONT_BODY, UIConstants.TEXT_GRAY);
        noteLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        outputPanel.add(noteLbl);
        outputPanel.add(Box.createVerticalStrut(14));

        // Impact row
        JPanel impact = UIConstants.makeCard(12, 12);
        impact.setLayout(new GridLayout(1, 4, 10, 0));
        impact.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        impact.setAlignmentX(Component.LEFT_ALIGNMENT);
        impact.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        addCompareCell(impact, String.valueOf(plan.getCarsUsed()),                       "Groups Formed",  UIConstants.ACCENT, false);
        addCompareCell(impact, String.valueOf(plan.getCarsAvoided()),                    "Cars Avoided",   UIConstants.SUCCESS, false);
        addCompareCell(impact, String.format("%.2f km", plan.getTotalExtraDistance()),   "Extra Distance", UIConstants.WARNING, false);
        addCompareCell(impact, String.format("%.2f kg", plan.getTotalCO2Saved()),        "CO₂ Saved",      UIConstants.DANGER, false);
        outputPanel.add(impact);
        outputPanel.add(Box.createVerticalStrut(14));

        // Groups
        for (CarpoolGroup g : plan.getGroups()) {
            JPanel card = UIConstants.makeCard(12, 12);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

            JLabel groupId = UIConstants.makeLabel("🚗  " + g.getGroupId() + " — Driver: " + g.getDriver().getName(), UIConstants.FONT_SUB, UIConstants.ACCENT);
            groupId.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(groupId);
            card.add(Box.createVerticalStrut(6));

            String pNames = g.getPassengers().stream().map(Passenger::getName).reduce((a,b)->a+", "+b).orElse("—");
            JLabel pLbl = UIConstants.makeLabel("Passengers: " + pNames, UIConstants.FONT_BODY, UIConstants.TEXT_DARK);
            pLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(pLbl);

            if (!g.getRouteOrder().isEmpty()) {
                StringBuilder rt = new StringBuilder("Route: ");
                for (int i = 0; i < g.getRouteOrder().size(); i++) {
                    rt.append(g.getRouteOrder().get(i).getName());
                    if (i < g.getRouteOrder().size()-1) rt.append(" → ");
                }
                JLabel rtLbl = UIConstants.makeLabel(rt.toString(), UIConstants.FONT_SMALL, UIConstants.TEXT_GRAY);
                rtLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.add(Box.createVerticalStrut(4));
                card.add(rtLbl);
            }
            JLabel extraLbl = UIConstants.makeLabel(
                String.format("Extra: %.2f km  |  CO₂ saved: %.2f kg  |  Fuel saved: %.2f L",
                    g.getTotalExtraDistanceKm(), g.getEstimatedCO2SavedKg(), g.getFuelSavedLiters()),
                UIConstants.FONT_SMALL, UIConstants.ACCENT);
            extraLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(Box.createVerticalStrut(4));
            card.add(extraLbl);

            outputPanel.add(card);
            outputPanel.add(Box.createVerticalStrut(10));
        }

        if (!plan.getUnassignedPassengers().isEmpty()) {
            JPanel uCard = UIConstants.makeCard(12, 12);
            uCard.setLayout(new BoxLayout(uCard, BoxLayout.Y_AXIS));
            uCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            uCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            uCard.add(UIConstants.makeLabel("⚠  Unassigned Passengers", UIConstants.FONT_SUB, UIConstants.DANGER));
            uCard.add(Box.createVerticalStrut(6));
            for (Passenger p : plan.getUnassignedPassengers()) {
                uCard.add(UIConstants.makeLabel("  • " + p.getName(), UIConstants.FONT_BODY, UIConstants.TEXT_DARK));
            }
            outputPanel.add(uCard);
        }

        outputPanel.revalidate();
        outputPanel.repaint();
    }

    private void showPlaceholder() {
        outputPanel.add(Box.createVerticalGlue());
        JLabel msg = UIConstants.makeLabel("👈  Select a scenario to run", UIConstants.FONT_HEADING, UIConstants.TEXT_GRAY);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        outputPanel.add(msg);
        outputPanel.add(Box.createVerticalGlue());
    }
}
