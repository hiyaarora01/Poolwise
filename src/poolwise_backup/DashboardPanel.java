package poolwise;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * Dashboard panel — statistics overview, quick summary cards, recent carpool table.
 */
public class DashboardPanel extends JPanel {

    private MainFrame main;
    private JPanel statsRow;
    private JPanel groupsPanel;

    public DashboardPanel(List<Driver> drivers, List<Passenger> passengers, MainFrame main) {
        this.main = main;
        setBackground(UIConstants.BG_LIGHT);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = UIConstants.makeLabel("Dashboard", UIConstants.FONT_TITLE, UIConstants.TEXT_DARK);
        JLabel sub   = UIConstants.makeLabel("Overview of your carpool network", UIConstants.FONT_BODY, UIConstants.TEXT_GRAY);
        JPanel ht = new JPanel(); ht.setOpaque(false); ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        ht.add(title); ht.add(Box.createVerticalStrut(2)); ht.add(sub);
        header.add(ht, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Stat cards row
        statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        body.add(statsRow);
        body.add(Box.createVerticalStrut(24));

        // Middle row: optimization modes + recent groups
        JPanel mid = new JPanel(new GridLayout(1, 2, 16, 0));
        mid.setOpaque(false);
        mid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        mid.add(buildModesCard());
        groupsPanel = buildGroupsCard(drivers, passengers);
        mid.add(groupsPanel);
        body.add(mid);
        body.add(Box.createVerticalStrut(20));

        // Compatibility table
        body.add(buildCompatTable(drivers, passengers));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        add(scroll, BorderLayout.CENTER);

        refresh(drivers, passengers);
    }

    public void refresh(List<Driver> drivers, List<Passenger> passengers) {
        // Run a quick balanced optimization for stats
        CarpoolPlan plan = CarpoolOptimizer.optimize(
            new java.util.ArrayList<>(drivers),
            new java.util.ArrayList<>(passengers),
            OptimizationMode.BALANCED);

        statsRow.removeAll();
        int totalSeats = drivers.stream().mapToInt(Driver::getAvailableSeats).sum();
        statsRow.add(UIConstants.makeStatCard(String.valueOf(drivers.size()),    "Total Drivers",        UIConstants.ACCENT));
        statsRow.add(UIConstants.makeStatCard(String.valueOf(passengers.size()), "Total Passengers",     UIConstants.SUCCESS));
        statsRow.add(UIConstants.makeStatCard(String.valueOf(plan.getCarsAvoided()), "Cars Avoided",     UIConstants.WARNING));
        statsRow.add(UIConstants.makeStatCard(
            String.format("%.1f kg", plan.getTotalCO2Saved()), "CO₂ Saved",         UIConstants.DANGER));
        statsRow.revalidate();
        statsRow.repaint();
    }

    private JPanel buildModesCard() {
        JPanel card = UIConstants.makeCard(14, 14);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        card.add(UIConstants.makeLabel("Optimization Modes", UIConstants.FONT_HEADING, UIConstants.TEXT_DARK));
        card.add(Box.createVerticalStrut(14));

        String[][] modes = {
            {"🚗", "Min Cars",     "Fewest vehicles"},
            {"⛽", "Min Distance", "Least extra km"},
            {"⏱️", "Min Time",    "Fastest arrival"},
            {"🌱", "Min CO₂",     "Eco-friendly"},
            {"⚖️", "Balanced",    "Best overall"},
        };
        for (String[] m : modes) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            JLabel left  = UIConstants.makeLabel(m[0] + "  " + m[1], UIConstants.FONT_BODY, UIConstants.TEXT_DARK);
            JLabel right = UIConstants.makeLabel(m[2], UIConstants.FONT_SMALL, UIConstants.TEXT_GRAY);
            row.add(left, BorderLayout.WEST);
            row.add(right, BorderLayout.EAST);
            card.add(row);
            card.add(Box.createVerticalStrut(6));
        }
        return card;
    }

    private JPanel buildGroupsCard(List<Driver> drivers, List<Passenger> passengers) {
        JPanel card = UIConstants.makeCard(14, 14);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        card.add(UIConstants.makeLabel("Active Carpool Groups", UIConstants.FONT_HEADING, UIConstants.TEXT_DARK));
        card.add(Box.createVerticalStrut(14));

        CarpoolPlan plan = CarpoolOptimizer.optimize(
            new java.util.ArrayList<>(drivers),
            new java.util.ArrayList<>(passengers),
            OptimizationMode.BALANCED);

        for (CarpoolGroup g : plan.getGroups()) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

            String names = g.getPassengers().stream()
                .map(Passenger::getName)
                .reduce((a,b) -> a + ", " + b).orElse("—");
            JLabel left  = UIConstants.makeLabel("🚗 " + g.getDriver().getName() + "  →  " + names, UIConstants.FONT_BODY, UIConstants.TEXT_DARK);
            JLabel right = UIConstants.makeLabel(String.format("+%.1f km", g.getTotalExtraDistanceKm()), UIConstants.FONT_SMALL, UIConstants.ACCENT);
            row.add(left, BorderLayout.WEST);
            row.add(right, BorderLayout.EAST);
            card.add(row);
        }
        if (plan.getGroups().isEmpty()) {
            card.add(UIConstants.makeLabel("No groups formed yet.", UIConstants.FONT_BODY, UIConstants.TEXT_GRAY));
        }
        return card;
    }

    private JPanel buildCompatTable(List<Driver> drivers, List<Passenger> passengers) {
        JPanel card = UIConstants.makeCard(14, 14);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        card.add(UIConstants.makeLabel("Compatibility Matrix", UIConstants.FONT_HEADING, UIConstants.TEXT_DARK), BorderLayout.NORTH);

        if (drivers.isEmpty() || passengers.isEmpty()) {
            card.add(UIConstants.makeLabel("Add drivers and passengers to see compatibility.", UIConstants.FONT_BODY, UIConstants.TEXT_GRAY), BorderLayout.CENTER);
            return card;
        }

        CompatibilityMatrix matrix = new CompatibilityMatrix(drivers, passengers);
        String[] cols = {"Driver", "Passenger", "Overall", "Route", "Pickup", "Detour", "Time"};
        Object[][] data = new Object[Math.min(matrix.getAllScores().size(), 12)][7];
        java.util.List<CompatibilityScore> scores = matrix.getAllScores();
        for (int i = 0; i < data.length; i++) {
            CompatibilityScore cs = scores.get(i);
            data[i][0] = cs.getDriver().getName();
            data[i][1] = cs.getPassenger().getName();
            data[i][2] = String.format("%.3f", cs.getScore());
            data[i][3] = String.format("%.2f", cs.getRouteOverlapScore());
            data[i][4] = String.format("%.2f", cs.getPickupDistanceScore());
            data[i][5] = String.format("%.2f", cs.getDetourScore());
            data[i][6] = String.format("%.2f", cs.getTimeScore());
        }

        JTable table = new JTable(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248,250,252));
                if (column == 2) {
                    double val = Double.parseDouble((String) getValueAt(row, column));
                    c.setForeground(val >= 0.7 ? UIConstants.SUCCESS : val >= 0.4 ? UIConstants.WARNING : UIConstants.DANGER);
                } else {
                    c.setForeground(UIConstants.TEXT_DARK);
                }
                return c;
            }
        };
        table.setFont(UIConstants.FONT_BODY);
        table.setRowHeight(30);
        table.getTableHeader().setFont(UIConstants.FONT_SUB);
        table.getTableHeader().setBackground(UIConstants.BG_MEDIUM);
        table.getTableHeader().setForeground(UIConstants.TEXT_WHITE);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setDefaultRenderer(Object.class, table.getDefaultRenderer(Object.class));

        // Center-align all columns
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 2; i < cols.length; i++) table.getColumnModel().getColumn(i).setCellRenderer(center);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        sp.setPreferredSize(new Dimension(0, 220));
        card.add(sp, BorderLayout.CENTER);
        return card;
    }
}
