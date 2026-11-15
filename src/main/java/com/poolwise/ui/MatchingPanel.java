package com.poolwise.ui;

import com.poolwise.model.*;
import com.poolwise.service.*;
import com.poolwise.algorithm.GreedyMatcher;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * Find Carpool panel — runs matching in a background SwingWorker thread.
 * OOP: Demonstrates Multithreading (SwingWorker), ExecutorService, synchronization.
 */
public class MatchingPanel extends JPanel {

    private final MainWindow main;
    private JComboBox<String> modeCombo;
    private JLabel  statusLabel;
    private JPanel  resultsArea;
    private JProgressBar progressBar;
    private JPanel  scoreMatrixCard;

    public MatchingPanel(MainWindow main) {
        this.main = main;
        setBackground(Theme.BG_PAGE);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(28,28,28,28));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    public void refresh() { /* data refreshed on button click */ }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout()); h.setOpaque(false);
        h.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
        JPanel left = new JPanel(); left.setOpaque(false);
        left.setLayout(new BoxLayout(left,BoxLayout.Y_AXIS));
        left.add(Theme.label("Find Best Carpool",Theme.F_TITLE,Theme.TEXT_MAIN));
        left.add(Box.createVerticalStrut(2));
        left.add(Theme.label("Matching runs in a background thread — UI stays responsive",Theme.F_BODY,Theme.TEXT_SUB));
        h.add(left,BorderLayout.WEST);

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0)); ctrl.setOpaque(false);
        modeCombo = Theme.combo("⚖️ Balanced Mode","🚗 Minimum Cars");
        modeCombo.setPreferredSize(new Dimension(190,36));
        JButton runBtn = Theme.primaryButton("⚡ FIND BEST CARPOOL");
        runBtn.setPreferredSize(new Dimension(200,38));
        runBtn.addActionListener(e -> runMatching(runBtn));
        progressBar = new JProgressBar(); progressBar.setIndeterminate(false);
        progressBar.setPreferredSize(new Dimension(120,8));
        progressBar.setVisible(false);

        ctrl.add(Theme.label("Mode:",Theme.F_SUB,Theme.TEXT_MAIN));
        ctrl.add(modeCombo); ctrl.add(runBtn); ctrl.add(progressBar);
        h.add(ctrl,BorderLayout.EAST);
        return h;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0,14)); body.setOpaque(false);

        // Status bar
        JPanel statusBar = Theme.card(10);
        statusBar.setBorder(BorderFactory.createEmptyBorder(10,16,10,16));
        statusBar.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));
        statusLabel = Theme.label("Select a mode and click FIND BEST CARPOOL to run the algorithm.",
            Theme.F_BODY, Theme.TEXT_SUB);
        statusBar.add(statusLabel, BorderLayout.CENTER);
        body.add(statusBar, BorderLayout.NORTH);

        // Results (scrollable)
        resultsArea = new JPanel(); resultsArea.setOpaque(false);
        resultsArea.setLayout(new BoxLayout(resultsArea, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(resultsArea);
        scroll.setBorder(null); scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        body.add(scroll, BorderLayout.CENTER);
        return body;
    }

    // ── Background matching using SwingWorker ─────────────────────
    private void runMatching(JButton runBtn) {
        String modeStr = modeCombo.getSelectedIndex() == 1 ? "MIN_CARS" : "BALANCED";
        runBtn.setEnabled(false);
        progressBar.setVisible(true); progressBar.setIndeterminate(true);
        statusLabel.setText("⏳ Running " + modeCombo.getSelectedItem() + " algorithm in background...");
        statusLabel.setForeground(Theme.PRIMARY);
        resultsArea.removeAll(); resultsArea.revalidate(); resultsArea.repaint();

        // FIX: Removed Thread.sleep() — caused unnecessary blocking.
        // Compat matrix is now built on the background thread to avoid EDT freeze.
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() throws Exception {
                List<Carpool> carpools = main.matchingService.runMatching(main.userService, modeStr);
                // Build matrix data OFF the EDT — avoids OOM from rendering on main thread
                Object[][] matrixData = buildMatrixData();
                return new Object[]{carpools, matrixData};
            }

            @Override protected void done() {
                try {
                    Object[] result   = get();
                    @SuppressWarnings("unchecked")
                    List<Carpool> carpools = (List<Carpool>) result[0];
                    Object[][]   matrix   = (Object[][])    result[1];

                    progressBar.setIndeterminate(false); progressBar.setVisible(false);
                    runBtn.setEnabled(true);
                    displayResults(carpools, modeStr, matrix);

                    // Save in background to avoid blocking UI
                    new Thread(() -> {
                        main.fileManager.saveCarpools(carpools);
                        main.fileManager.saveDrivers(main.userService.getAllDrivers());
                        main.fileManager.savePassengers(main.userService.getAllPassengers());
                    }, "poolwise-save").start();

                } catch (OutOfMemoryError oom) {
                    progressBar.setVisible(false); runBtn.setEnabled(true);
                    statusLabel.setText("⚠ Out of Memory — reduce data size or restart app.");
                    statusLabel.setForeground(Theme.DANGER);
                } catch (Exception ex) {
                    progressBar.setVisible(false); runBtn.setEnabled(true);
                    statusLabel.setText("⚠ Error: " + ex.getMessage());
                    statusLabel.setForeground(Theme.DANGER);
                }
            }
        }.execute();
    }

    /** Build compatibility matrix data array — called on background thread. */
    private Object[][] buildMatrixData() {
        List<Driver>    drivers    = main.userService.getAllDrivers();
        List<Passenger> passengers = main.userService.getAllPassengers();
        if (drivers.isEmpty() || passengers.isEmpty()) return new Object[0][0];

        GreedyMatcher gm = new GreedyMatcher();
        java.util.List<double[]>  scores = new java.util.ArrayList<>();
        java.util.List<String[]>  labels = new java.util.ArrayList<>();

        for (Driver d : drivers) {
            for (Passenger p : passengers) {
                scores.add(gm.scoreBreakdown(d, p));
                labels.add(new String[]{d.getName(), p.getName()});
            }
        }

        // Sort descending by overall score, cap at 12 rows max (prevents OOM from large table)
        java.util.List<Integer> idx = new java.util.ArrayList<>();
        for (int i = 0; i < scores.size(); i++) idx.add(i);
        idx.sort((a, b) -> Double.compare(scores.get(b)[5], scores.get(a)[5]));

        int show = Math.min(12, idx.size());
        Object[][] data = new Object[show][8];
        for (int i = 0; i < show; i++) {
            int ri = idx.get(i);
            double[] bd = scores.get(ri);
            String[] lb = labels.get(ri);
            data[i] = new Object[]{
                lb[0], lb[1],
                String.format("%.3f", bd[5]),
                String.format("%.3f", bd[0]),
                String.format("%.3f", bd[1]),
                String.format("%.3f", bd[2]),
                String.format("%.3f", bd[3]),
                String.format("%.3f", bd[4])
            };
        }
        return data;
    }

    private void displayResults(List<Carpool> carpools, String mode, Object[][] matrixData) {
        int matched   = carpools.stream().mapToInt(Carpool::getPassengerCount).sum();
        int unmatched = main.userService.passengerCount() - matched;
        statusLabel.setText(String.format(
            "✅  Mode: %s  |  Groups: %d  |  Matched: %d  |  Unmatched: %d  |  CO₂ Saved: %.2f kg",
            mode.replace("_"," "), carpools.size(), matched, unmatched,
            carpools.stream().mapToDouble(Carpool::getEstimatedCO2SavedKg).sum()));
        statusLabel.setForeground(Theme.SUCCESS);

        resultsArea.removeAll();
        resultsArea.add(Box.createVerticalStrut(10));

        if (carpools.isEmpty()) {
            JLabel none = Theme.label("⚠  No compatible carpool groups could be formed with current data.",Theme.F_HEADING,Theme.DANGER);
            none.setAlignmentX(Component.LEFT_ALIGNMENT);
            resultsArea.add(none);
        }

        for (Carpool cp : carpools) { resultsArea.add(buildGroupCard(cp)); resultsArea.add(Box.createVerticalStrut(14)); }

        // Unmatched
        List<Passenger> unmatcList = main.userService.getUnmatchedPassengers();
        if (!unmatcList.isEmpty()) {
            JPanel uCard = Theme.card(12);
            uCard.setBorder(BorderFactory.createEmptyBorder(14,16,14,16));
            uCard.setLayout(new BoxLayout(uCard,BoxLayout.Y_AXIS));
            uCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            uCard.add(Theme.label("⚠  Unassigned Passengers (no compatible driver found)",Theme.F_HEADING,Theme.DANGER));
            uCard.add(Box.createVerticalStrut(8));
            for (Passenger p : unmatcList)
                uCard.add(Theme.label("  • "+p.getName()+" — "+p.getPickupLocation(),Theme.F_BODY,Theme.TEXT_MAIN));
            resultsArea.add(uCard); resultsArea.add(Box.createVerticalStrut(14));
        }

        // Compatibility matrix — built from pre-computed data (no EDT blocking)
        resultsArea.add(buildCompatMatrixFromData(matrixData));

        resultsArea.revalidate(); resultsArea.repaint();
        main.refreshAll();
    }

    private JPanel buildGroupCard(Carpool cp) {
        JPanel card = Theme.card(14);
        card.setLayout(new BorderLayout(16,0));
        card.setBorder(BorderFactory.createEmptyBorder(16,20,16,20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,180));

        // Left — driver badge
        JPanel left = new JPanel(); left.setOpaque(false);
        left.setLayout(new BoxLayout(left,BoxLayout.Y_AXIS));
        left.setPreferredSize(new Dimension(200,0));

        JLabel badge = Theme.label("🚗 "+cp.getCarpoolId(), Theme.F_SUB, Theme.PRIMARY);
        badge.setOpaque(true); badge.setBackground(Theme.PRIMARY_LIGHT);
        badge.setBorder(BorderFactory.createEmptyBorder(3,8,3,8));
        left.add(badge); left.add(Box.createVerticalStrut(8));
        left.add(Theme.label(cp.getDriver().getName(), Theme.F_HEADING, Theme.TEXT_MAIN));
        left.add(Theme.label("From: "+cp.getDriver().getStartLocation(), Theme.F_SMALL, Theme.TEXT_SUB));
        left.add(Theme.label("To:   "+cp.getDriver().getDestination(),   Theme.F_SMALL, Theme.TEXT_SUB));
        left.add(Theme.label("Departs: "+cp.getDriver().getDepartureTime(), Theme.F_SMALL, Theme.TEXT_SUB));
        if (cp.getDriver().getVehicle() != null)
            left.add(Theme.label("🚙 "+cp.getDriver().getVehicle(), Theme.F_SMALL, Theme.TEXT_SUB));
        card.add(left, BorderLayout.WEST);

        // Middle — passengers + route
        JPanel mid = new JPanel(); mid.setOpaque(false);
        mid.setLayout(new BoxLayout(mid,BoxLayout.Y_AXIS));
        mid.add(Theme.label("Passengers ("+cp.getPassengerCount()+"/"+cp.getDriver().getAvailableSeats()+")", Theme.F_SUB, Theme.TEXT_MAIN));
        mid.add(Box.createVerticalStrut(6));
        for (Passenger p : cp.getPassengers())
            mid.add(Theme.label("  👤 "+p.getName()+"  ← "+p.getPickupLocation(), Theme.F_BODY, Theme.TEXT_MAIN));
        if (!cp.getRouteOrder().isEmpty()) {
            mid.add(Box.createVerticalStrut(8));
            mid.add(Theme.label("Route: "+cp.getRouteString(), Theme.F_SMALL, Theme.TEXT_SUB));
        }
        mid.add(Box.createVerticalStrut(4));
        mid.add(Theme.label(String.format("Score: %.2f | Compatibility: %.0f%%",
            cp.getCompatibilityScore(), cp.getCompatibilityScore()*100), Theme.F_SMALL, Theme.TEXT_SUB));
        card.add(mid, BorderLayout.CENTER);

        // Right — stats
        JPanel right = new JPanel(); right.setOpaque(false);
        right.setLayout(new BoxLayout(right,BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(150,0));
        addStat(right,"Extra km",    String.format("%.2f km",cp.getExtraDistanceKm()),  Theme.PRIMARY);
        addStat(right,"CO₂ Saved",   String.format("%.2f kg",cp.getEstimatedCO2SavedKg()), Theme.SUCCESS);
        addStat(right,"Fuel Saved",  String.format("%.2f L", cp.getEstimatedFuelSavedL()), Theme.WARNING);
        addStat(right,"Cars Avoided",String.valueOf(cp.getCarsAvoided()),             Theme.ACCENT);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private void addStat(JPanel p, String label, String value, Color color) {
        JPanel row = new JPanel(new BorderLayout(4,0)); row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(3,0,3,0));
        row.add(Theme.label(label,Theme.F_SMALL,Theme.TEXT_SUB),BorderLayout.WEST);
        JLabel v = Theme.label(value,Theme.F_SUB,color); v.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(v,BorderLayout.EAST); p.add(row);
    }

    /** Build the compat matrix JPanel from pre-computed data (runs on EDT — data already ready). */
    private JPanel buildCompatMatrixFromData(Object[][] data) {
        JPanel card = Theme.card(14);
        card.setLayout(new BorderLayout(0,10));
        card.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Theme.label("Compatibility Matrix (top 12 pairs)",Theme.F_HEADING,Theme.TEXT_MAIN), BorderLayout.NORTH);

        if (data == null || data.length == 0) {
            card.add(Theme.label("Add drivers and passengers, then run matching.",Theme.F_BODY,Theme.TEXT_SUB),BorderLayout.CENTER);
            return card;
        }

        String[] cols = {"Driver","Passenger","Overall","Route","Pickup","Detour","Time","Reliability"};
        JTable t = new JTable(new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        }) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row%2==0 ? Color.WHITE : Theme.ROW_ALT);
                if (col==2) {
                    try {
                        double v = Double.parseDouble(c instanceof JLabel ? ((JLabel)c).getText() : getValueAt(row,col).toString());
                        c.setForeground(v>=0.65 ? Theme.SUCCESS : v>=0.4 ? Theme.WARNING : Theme.DANGER);
                    } catch(Exception e2){ c.setForeground(Theme.TEXT_MAIN); }
                } else { c.setForeground(Theme.TEXT_MAIN); }
                return c;
            }
        };
        t.setFont(Theme.F_TABLE); t.setRowHeight(28);
        t.getTableHeader().setFont(Theme.F_TABLE_H);
        t.getTableHeader().setBackground(new Color(239,246,255));
        t.getTableHeader().setForeground(Theme.PRIMARY);
        t.setShowGrid(false); t.setIntercellSpacing(new Dimension(0,1));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i=2; i<cols.length; i++) t.getColumnModel().getColumn(i).setCellRenderer(center);
        JScrollPane sp = new JScrollPane(t); sp.setPreferredSize(new Dimension(0,220));
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private String f(double v) { return String.format("%.3f",v); }
}
