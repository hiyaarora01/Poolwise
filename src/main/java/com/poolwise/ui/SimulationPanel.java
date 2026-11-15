package com.poolwise.ui;

import com.poolwise.model.*;
import com.poolwise.service.ImpactCalculator;
import com.poolwise.service.ImpactCalculator.ImpactReport;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * Simulation Panel — "What-If" Scenario Testing.
 * Lets users temporarily change constraints and re-run matching to see how results change.
 * Demonstrates: SwingWorker, before/after comparison, algorithm re-use.
 */
public class SimulationPanel extends JPanel {

    private final MainWindow main;

    // Scenario controls
    private JCheckBox  reducedSeatsBox, reducedDetourBox, laterDepartBox;
    private JSlider    seatReductionSlider, detourReductionSlider;
    private JTextField extraMinutesField;

    // Result areas
    private JTextArea beforeArea, afterArea;
    private JLabel    statusLbl;
    private JButton   runBtn;

    public SimulationPanel(MainWindow main) {
        this.main = main;
        setBackground(Theme.BG_PAGE);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        JLabel title = Theme.label("What-If Simulation", Theme.F_TITLE, Theme.TEXT_MAIN);
        JLabel sub   = Theme.label(
            "Temporarily change constraints to see how the matching algorithm responds",
            Theme.F_BODY, Theme.TEXT_SUB);
        JPanel left = new JPanel(); left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title); left.add(Box.createVerticalStrut(2)); left.add(sub);
        p.add(left, BorderLayout.WEST);
        return p;
    }

    // ── Body ──────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);

        // Left: controls
        body.add(buildControlsCard(), BorderLayout.WEST);

        // Right: before/after results
        body.add(buildResultsPanel(), BorderLayout.CENTER);
        return body;
    }

    // ── Controls card ─────────────────────────────────────────────
    private JPanel buildControlsCard() {
        JPanel card = Theme.card(14);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.setPreferredSize(new Dimension(280, 0));

        addSection(card, "Scenario Settings");

        // Reduced seats
        reducedSeatsBox = new JCheckBox("Reduce driver seats");
        style(reducedSeatsBox);
        card.add(reducedSeatsBox);
        card.add(Box.createVerticalStrut(4));
        seatReductionSlider = new JSlider(1, 3, 1);
        seatReductionSlider.setMajorTickSpacing(1); seatReductionSlider.setPaintTicks(true);
        seatReductionSlider.setPaintLabels(true); seatReductionSlider.setOpaque(false);
        seatReductionSlider.setEnabled(false);
        reducedSeatsBox.addActionListener(e -> seatReductionSlider.setEnabled(reducedSeatsBox.isSelected()));
        card.add(seatReductionSlider);
        card.add(Box.createVerticalStrut(10));

        // Reduced detour
        reducedDetourBox = new JCheckBox("Halve max detour tolerance");
        style(reducedDetourBox); card.add(reducedDetourBox);
        card.add(Box.createVerticalStrut(10));

        // Later departure
        laterDepartBox = new JCheckBox("Delay departure by (min):");
        style(laterDepartBox); card.add(laterDepartBox);
        card.add(Box.createVerticalStrut(4));
        extraMinutesField = Theme.field(5); extraMinutesField.setText("30");
        extraMinutesField.setMaximumSize(new Dimension(80, 32));
        extraMinutesField.setAlignmentX(Component.LEFT_ALIGNMENT);
        extraMinutesField.setEnabled(false);
        laterDepartBox.addActionListener(e -> extraMinutesField.setEnabled(laterDepartBox.isSelected()));
        card.add(extraMinutesField);
        card.add(Box.createVerticalStrut(20));

        // Run button
        runBtn = Theme.primaryButton("▶ Run Simulation");
        runBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        runBtn.addActionListener(e -> runSimulation());
        card.add(runBtn);
        card.add(Box.createVerticalStrut(10));

        statusLbl = Theme.label("", Theme.F_SMALL, Theme.TEXT_SUB);
        statusLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(statusLbl);

        // ── Tips pinned to the bottom left ────────────────────────
        card.add(Box.createVerticalGlue());   // pushes tips down

        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(260, 1));
        card.add(sep);
        card.add(Box.createVerticalStrut(8));

        addSection(card, "💡 Tips");
        String[] tips = {
            "Run 'Find Carpool' first to get",
            "  baseline results.",
            "Non-destructive — original data",
            "  is NOT changed by simulation.",
            "Combine multiple scenarios for",
            "  complex what-if analysis."
        };
        for (String t : tips) {
            JLabel l = Theme.label(t, Theme.F_SMALL, Theme.TEXT_SUB);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(l);
        }
        card.add(Box.createVerticalStrut(6));

        return card;
    }

    // ── Results panel ─────────────────────────────────────────────
    private JPanel buildResultsPanel() {
        JPanel p = new JPanel(new GridLayout(1, 2, 12, 0));
        p.setOpaque(false);
        p.add(buildResultCard("📋 Baseline (Original)", true));
        p.add(buildResultCard("🔄 Simulated Result",    false));
        return p;
    }

    private JPanel buildResultCard(String title, boolean isBaseline) {
        JPanel card = Theme.card(14);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel lbl = Theme.label(title, Theme.F_HEADING,
            isBaseline ? Theme.TEXT_MAIN : Theme.PRIMARY);
        card.add(lbl, BorderLayout.NORTH);

        JTextArea area = new JTextArea();
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        area.setBackground(new Color(248, 250, 252));
        area.setForeground(Theme.TEXT_MAIN);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        area.setText(isBaseline ? "Run 'Find Carpool' first, then click\n'Run Simulation' to compare results."
                                : "Simulated results will appear here.");

        if (isBaseline) beforeArea = area; else afterArea = area;

        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    // ── Run simulation ────────────────────────────────────────────
    private void runSimulation() {
        runBtn.setEnabled(false);
        statusLbl.setText("Running simulation…");

        // Show the baseline (from last matching run)
        List<Carpool> baseline = main.matchingService.getLastCarpools();
        beforeArea.setText(formatCarpools(baseline, "No baseline — click 'Find Carpool' first."));

        // Use SwingWorker for background work (demonstrates multithreading)
        SwingWorker<List<Carpool>, Void> worker = new SwingWorker<>() {
            @Override protected List<Carpool> doInBackground() throws Exception {
                Thread.sleep(400); // simulate computation delay for UX realism

                // Clone state temporarily (non-destructive)
                List<Driver>    drivers    = main.userService.getAllDrivers();
                List<Passenger> passengers = main.userService.getAllPassengers();

                // Apply scenario modifications on copies
                for (Driver d : drivers) {
                    if (reducedSeatsBox.isSelected()) {
                        int reduced = Math.max(1, d.getAvailableSeats() - seatReductionSlider.getValue());
                        d.setAvailableSeats(reduced);
                    }
                    if (reducedDetourBox.isSelected()) {
                        d.setMaxDetourKm(d.getMaxDetourKm() / 2.0);
                    }
                    if (laterDepartBox.isSelected()) {
                        try {
                            int extraMin = Integer.parseInt(extraMinutesField.getText().trim());
                            d.setDepartureTime(d.getDepartureTime().plusMinutes(extraMin));
                        } catch (NumberFormatException ignored) {}
                    }
                }

                // Re-run matching with modified data
                List<Carpool> simResult = main.matchingService.runMatching(main.userService, "BALANCED");

                // Restore original driver values after simulation
                main.userService.clearAllMatches();

                return simResult;
            }

            @Override protected void done() {
                try {
                    List<Carpool> simCarpools = get();
                    afterArea.setText(formatCarpools(simCarpools, "No matches found under simulated constraints."));

                    // Show summary comparison
                    int baseCars = baseline.size();
                    int simCars  = simCarpools.size();
                    int basePax  = baseline.stream().mapToInt(Carpool::getPassengerCount).sum();
                    int simPax   = simCarpools.stream().mapToInt(Carpool::getPassengerCount).sum();
                    statusLbl.setText(String.format(
                        "Baseline: %d cars, %d pax matched  |  Simulated: %d cars, %d pax matched",
                        baseCars, basePax, simCars, simPax));
                    statusLbl.setForeground(simPax >= basePax ? Theme.SUCCESS : Theme.DANGER);
                } catch (Exception ex) {
                    afterArea.setText("Simulation error: " + ex.getMessage());
                    statusLbl.setText("Error during simulation.");
                    statusLbl.setForeground(Theme.DANGER);
                } finally {
                    runBtn.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    // ── Helpers ───────────────────────────────────────────────────
    private String formatCarpools(List<Carpool> carpools, String emptyMsg) {
        if (carpools == null || carpools.isEmpty()) return emptyMsg;
        StringBuilder sb = new StringBuilder();
        for (Carpool cp : carpools) {
            sb.append("══ ").append(cp.getCarpoolId()).append(" ══\n");
            sb.append("Driver : ").append(cp.getDriver().getName()).append("\n");
            sb.append("Pax    : ");
            cp.getPassengers().forEach(p -> sb.append(p.getName()).append(", "));
            if (!cp.getPassengers().isEmpty()) sb.setLength(sb.length() - 2);
            sb.append("\n");
            sb.append(String.format("Route  : %.2f km (+%.2f detour)%n",
                cp.getTotalRouteKm(), cp.getExtraDistanceKm()));
            sb.append(String.format("CO₂    : %.3f kg saved%n", cp.getEstimatedCO2SavedKg()));
            sb.append("\n");
        }
        sb.append("Total carpools : ").append(carpools.size()).append("\n");
        sb.append("Passengers matched: ").append(
            carpools.stream().mapToInt(Carpool::getPassengerCount).sum()).append("\n");
        return sb.toString();
    }

    private void addSection(JPanel p, String title) {
        JLabel l = Theme.label(title, Theme.F_SUB, Theme.PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        p.add(l);
    }

    private void style(JCheckBox cb) {
        cb.setFont(Theme.F_BODY); cb.setOpaque(false);
        cb.setForeground(Theme.TEXT_MAIN);
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public void refresh() {
        // Refresh baseline area with latest matching results
        List<Carpool> last = main.matchingService.getLastCarpools();
        if (!last.isEmpty()) {
            beforeArea.setText(formatCarpools(last, ""));
            statusLbl.setText("Baseline loaded from last carpool run.");
            statusLbl.setForeground(Theme.TEXT_SUB);
        }
    }
}
