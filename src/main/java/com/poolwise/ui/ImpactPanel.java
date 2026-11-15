package com.poolwise.ui;

import com.poolwise.model.*;
import com.poolwise.service.ImpactCalculator;
import com.poolwise.service.ImpactCalculator.ImpactReport;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Environmental Impact Panel.
 * Shows CO2, fuel, cost savings compared to solo travel.
 * Uses ImpactCalculator (Strategy/Calculator pattern).
 * Demonstrates: record types (ImpactReport), service layer, stat cards.
 */
public class ImpactPanel extends JPanel {

    private final MainWindow main;

    // Dynamic labels for the 4 hero stat cards
    private JLabel carsAvoidedLbl, co2Lbl, fuelLbl, costLbl;
    // Detail table
    private DefaultTableModel detailModel;

    public ImpactPanel(MainWindow main) {
        this.main = main;
        setBackground(Theme.BG_PAGE);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(buildHeroCards());
        body.add(Box.createVerticalStrut(20));
        body.add(buildDetailCard());
        body.add(Box.createVerticalStrut(20));
        body.add(buildAssumptionsCard());

        JScrollPane sp = new JScrollPane(body);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        add(sp, BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        JLabel title = Theme.label("Environmental Impact", Theme.F_TITLE, Theme.TEXT_MAIN);
        JLabel sub   = Theme.label("How much CO₂, fuel and money POOLWISE saves vs individual travel",
                                    Theme.F_BODY, Theme.TEXT_SUB);
        JPanel left = new JPanel(); left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title); left.add(Box.createVerticalStrut(2)); left.add(sub);
        p.add(left, BorderLayout.WEST);
        JButton refreshBtn = Theme.primaryButton("⟳ Recalculate");
        refreshBtn.addActionListener(e -> refresh());
        p.add(refreshBtn, BorderLayout.EAST);
        return p;
    }

    // ── Hero stat cards ───────────────────────────────────────────
    private JPanel buildHeroCards() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 16, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        carsAvoidedLbl = new JLabel("—");
        co2Lbl         = new JLabel("—");
        fuelLbl        = new JLabel("—");
        costLbl        = new JLabel("—");

        grid.add(heroCard("🚗  Cars Avoided",   carsAvoidedLbl, Theme.PRIMARY));
        grid.add(heroCard("🌱  CO₂ Saved (kg)", co2Lbl,         Theme.SUCCESS));
        grid.add(heroCard("⛽  Fuel Saved (L)",  fuelLbl,        Theme.WARNING));
        grid.add(heroCard("💰  Cost Saved (₹)",  costLbl,        Theme.ACCENT));
        return grid;
    }

    private JPanel heroCard(String title, JLabel valueLbl, Color barColor) {
        JPanel card = Theme.statCard("—", title, barColor);
        // Replace inner labels with live reference
        card.removeAll();
        JPanel inner = new JPanel(new GridLayout(2, 1, 0, 6));
        inner.setOpaque(false);
        inner.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        valueLbl.setFont(Theme.F_STAT);
        valueLbl.setForeground(Theme.TEXT_MAIN);
        JLabel titleLbl = Theme.label(title, Theme.F_SMALL, Theme.TEXT_SUB);
        inner.add(valueLbl); inner.add(titleLbl);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // ── Detail table card ─────────────────────────────────────────
    private JPanel buildDetailCard() {
        JPanel card = Theme.card(14);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        card.add(Theme.label("Impact Breakdown", Theme.F_HEADING, Theme.TEXT_MAIN), BorderLayout.NORTH);

        String[] cols = {"Metric", "Without POOLWISE", "With POOLWISE", "Savings"};
        detailModel   = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(detailModel);
        t.setFont(Theme.F_TABLE);
        t.setRowHeight(28);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.getTableHeader().setFont(Theme.F_TABLE_H);
        t.getTableHeader().setBackground(new Color(239, 246, 255));
        t.getTableHeader().setForeground(Theme.PRIMARY);

        // Colour the "Savings" column green
        DefaultTableCellRenderer savingsRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, v, sel, foc, row, col);
                setBackground(row % 2 == 0 ? Color.WHITE : Theme.ROW_ALT);
                setForeground(col == 3 ? Theme.SUCCESS : Theme.TEXT_MAIN);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
        for (int i = 0; i < 4; i++) t.getColumnModel().getColumn(i).setCellRenderer(savingsRenderer);
        t.getColumnModel().getColumn(0).setPreferredWidth(160);

        JScrollPane sp = new JScrollPane(t); sp.setBorder(null);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    // ── Assumptions card ──────────────────────────────────────────
    private JPanel buildAssumptionsCard() {
        JPanel card = Theme.card(14);
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(Theme.label("Calculation Assumptions", Theme.F_SUB, Theme.TEXT_MAIN));
        card.add(Box.createVerticalStrut(6));
        String[] lines = {
            "Fuel efficiency: " + ImpactCalculator.FUEL_EFF_KM_PER_L + " km/L  (avg Indian petrol car)",
            "CO₂ factor: " + ImpactCalculator.CO2_KG_PER_LITRE + " kg/L of petrol burned",
            "Fuel price: ₹" + (int)ImpactCalculator.FUEL_COST_PER_LITRE + "/L  (approximate 2024 Delhi rate)",
            "Savings are estimates — actual values depend on vehicle type, load and driving conditions."
        };
        for (String l : lines) {
            JLabel lbl = Theme.label("• " + l, Theme.F_SMALL, Theme.TEXT_SUB);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(lbl);
        }
        return card;
    }

    // ── Refresh ───────────────────────────────────────────────────
    public void refresh() {
        List<Carpool>   carpools   = main.matchingService.getLastCarpools();
        List<Driver>    drivers    = main.userService.getAllDrivers();
        List<Passenger> passengers = main.userService.getAllPassengers();

        ImpactReport r = ImpactCalculator.calculate(carpools, drivers, passengers);

        carsAvoidedLbl.setText(String.valueOf(r.carsAvoided()));
        co2Lbl .setText(String.format("%.2f", r.co2SavedKg()));
        fuelLbl.setText(String.format("%.2f", r.fuelSavedLitres()));
        costLbl.setText(String.format("₹%.0f", r.costSavedRupees()));

        detailModel.setRowCount(0);
        detailModel.addRow(new Object[]{
            "Total vehicles used",
            r.carsWithoutCarpool(),
            r.carsWithCarpool(),
            "−" + r.carsAvoided() + " vehicles"
        });
        detailModel.addRow(new Object[]{
            "Passengers matched",
            "0 / " + r.totalPassengers(),
            r.passengersMatched() + " / " + r.totalPassengers(),
            r.passengersMatched() + " matched"
        });
        detailModel.addRow(new Object[]{
            "Net distance saved (km)",
            "—",
            "—",
            String.format("%.2f km", r.totalDistanceSavedKm())
        });
        detailModel.addRow(new Object[]{
            "Extra distance from detours",
            "0 km",
            String.format("%.2f km", r.totalExtraDistanceKm()),
            "overhead"
        });
        detailModel.addRow(new Object[]{
            "Fuel saved (L)",
            "—", "—",
            String.format("%.2f L", r.fuelSavedLitres())
        });
        detailModel.addRow(new Object[]{
            "CO₂ emissions avoided",
            "—", "—",
            String.format("%.2f kg", r.co2SavedKg())
        });
        detailModel.addRow(new Object[]{
            "Estimated cost saving",
            "—", "—",
            String.format("₹%.2f", r.costSavedRupees())
        });
        repaint();
    }
}
