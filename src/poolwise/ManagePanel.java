package poolwise;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * Manage panel — add drivers and passengers, view existing ones.
 */
public class ManagePanel extends JPanel {

    private MainFrame main;
    private List<Driver>    drivers;
    private List<Passenger> passengers;

    private DefaultTableModel driverTableModel;
    private DefaultTableModel passnTableModel;

    public ManagePanel(List<Driver> drivers, List<Passenger> passengers, MainFrame main) {
        this.drivers    = drivers;
        this.passengers = passengers;
        this.main       = main;

        setBackground(UIConstants.BG_LIGHT);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        // Header
        JLabel title = UIConstants.makeLabel("Manage Users", UIConstants.FONT_TITLE, UIConstants.TEXT_DARK);
        JLabel sub   = UIConstants.makeLabel("Add drivers and passengers to the system", UIConstants.FONT_BODY, UIConstants.TEXT_GRAY);
        JPanel header = new JPanel(); header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title); header.add(Box.createVerticalStrut(2)); header.add(sub);
        add(header, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIConstants.FONT_SUB);
        tabs.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        tabs.setBackground(UIConstants.BG_LIGHT);
        tabs.addTab("🚗  Drivers",    buildDriversTab());
        tabs.addTab("👤  Passengers", buildPassengersTab());
        add(tabs, BorderLayout.CENTER);
    }

    // ── Drivers tab ──────────────────────────────────────────────
    private JPanel buildDriversTab() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setBackground(UIConstants.BG_LIGHT);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // Left: add form
        JPanel formCard = UIConstants.makeCard(14, 14);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formCard.setPreferredSize(new Dimension(280, 0));

        formCard.add(UIConstants.makeLabel("Add New Driver", UIConstants.FONT_HEADING, UIConstants.TEXT_DARK));
        formCard.add(Box.createVerticalStrut(14));

        JTextField nameF   = addField(formCard, "Driver Name");
        JTextField startF  = addField(formCard, "Start Location");
        JTextField sLatF   = addField(formCard, "Start Latitude  e.g. 28.6139");
        JTextField sLonF   = addField(formCard, "Start Longitude e.g. 77.2090");
        JTextField destF   = addField(formCard, "Destination");
        JTextField dLatF   = addField(formCard, "Dest. Latitude  e.g. 28.5459");
        JTextField dLonF   = addField(formCard, "Dest. Longitude e.g. 77.1926");
        JTextField timeF   = addField(formCard, "Departure (HH:mm) e.g. 08:00");
        JTextField seatsF  = addField(formCard, "Available Seats");
        JTextField detourF = addField(formCard, "Max Detour (km)");
        JTextField relF    = addField(formCard, "Reliability (0.0–1.0)");

        JButton addBtn = UIConstants.makePrimaryButton("Add Driver");
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(addBtn);

        addBtn.addActionListener(e -> {
            try {
                String id = "D" + (drivers.size() + 1);
                Location start = new Location(getText(startF), Double.parseDouble(getText(sLatF)), Double.parseDouble(getText(sLonF)));
                Location dest  = new Location(getText(destF),  Double.parseDouble(getText(dLatF)), Double.parseDouble(getText(dLonF)));
                Driver d = new Driver(id, getText(nameF), Double.parseDouble(getText(relF)),
                    start, dest, LocalTime.parse(getText(timeF)),
                    Integer.parseInt(getText(seatsF)), Double.parseDouble(getText(detourF)));
                drivers.add(d);
                refreshDriverTable();
                main.refreshAll();
                clearFields(nameF, startF, sLatF, sLonF, destF, dLatF, dLonF, timeF, seatsF, detourF, relF);
                JOptionPane.showMessageDialog(this, "✓ Driver " + d.getName() + " added!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "⚠ Error: " + ex.getMessage() + "\nCheck all fields are filled correctly.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        p.add(formCard, BorderLayout.WEST);

        // Right: table
        String[] cols = {"ID", "Name", "From", "To", "Departs", "Seats", "Max Detour", "Reliability"};
        driverTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = makeStyledTable(driverTableModel);
        refreshDriverTable();

        // Delete button
        JButton delBtn = UIConstants.makeOutlineButton("Remove Selected");
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String id = (String) driverTableModel.getValueAt(row, 0);
                drivers.removeIf(d -> d.getId().equals(id));
                refreshDriverTable();
                main.refreshAll();
            }
        });

        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setOpaque(false);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        btnRow.add(delBtn);
        right.add(UIConstants.makeLabel("All Drivers", UIConstants.FONT_HEADING, UIConstants.TEXT_DARK), BorderLayout.NORTH);
        right.add(new JScrollPane(table), BorderLayout.CENTER);
        right.add(btnRow, BorderLayout.SOUTH);
        p.add(right, BorderLayout.CENTER);

        return p;
    }

    private void refreshDriverTable() {
        driverTableModel.setRowCount(0);
        for (Driver d : drivers) {
            driverTableModel.addRow(new Object[]{
                d.getId(), d.getName(),
                d.getStartLocation().getName(), d.getDestination().getName(),
                d.getDepartureTime().toString(), d.getAvailableSeats(),
                d.getMaxDetourKm() + " km",
                String.format("%.2f", d.getReliabilityScore())
            });
        }
    }

    // ── Passengers tab ───────────────────────────────────────────
    private JPanel buildPassengersTab() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setBackground(UIConstants.BG_LIGHT);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JPanel formCard = UIConstants.makeCard(14, 14);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formCard.setPreferredSize(new Dimension(280, 0));

        formCard.add(UIConstants.makeLabel("Add New Passenger", UIConstants.FONT_HEADING, UIConstants.TEXT_DARK));
        formCard.add(Box.createVerticalStrut(14));

        JTextField nameF    = addField(formCard, "Passenger Name");
        JTextField startF   = addField(formCard, "Pickup Location");
        JTextField sLatF    = addField(formCard, "Pickup Latitude  e.g. 28.6519");
        JTextField sLonF    = addField(formCard, "Pickup Longitude e.g. 77.1902");
        JTextField destF    = addField(formCard, "Destination");
        JTextField dLatF    = addField(formCard, "Dest. Latitude  e.g. 28.5459");
        JTextField dLonF    = addField(formCard, "Dest. Longitude e.g. 77.1926");
        JTextField arrTimeF = addField(formCard, "Arrive By (HH:mm) e.g. 09:30");
        JTextField waitF    = addField(formCard, "Max Wait (minutes)");
        JTextField relF     = addField(formCard, "Reliability (0.0–1.0)");

        JButton addBtn = UIConstants.makePrimaryButton("Add Passenger");
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(addBtn);

        addBtn.addActionListener(e -> {
            try {
                String id = "P" + (passengers.size() + 1);
                Location start = new Location(getText(startF), Double.parseDouble(getText(sLatF)), Double.parseDouble(getText(sLonF)));
                Location dest  = new Location(getText(destF),  Double.parseDouble(getText(dLatF)), Double.parseDouble(getText(dLonF)));
                Passenger ps = new Passenger(id, getText(nameF), Double.parseDouble(getText(relF)),
                    start, dest, LocalTime.parse(getText(arrTimeF)), Double.parseDouble(getText(waitF)));
                passengers.add(ps);
                refreshPassengerTable();
                main.refreshAll();
                clearFields(nameF, startF, sLatF, sLonF, destF, dLatF, dLonF, arrTimeF, waitF, relF);
                JOptionPane.showMessageDialog(this, "✓ Passenger " + ps.getName() + " added!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "⚠ Error: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        p.add(formCard, BorderLayout.WEST);

        String[] cols = {"ID", "Name", "Pickup", "Destination", "Arrive By", "Max Wait", "Reliability"};
        passnTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = makeStyledTable(passnTableModel);
        refreshPassengerTable();

        JButton delBtn = UIConstants.makeOutlineButton("Remove Selected");
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String id = (String) passnTableModel.getValueAt(row, 0);
                passengers.removeIf(ps -> ps.getId().equals(id));
                refreshPassengerTable();
                main.refreshAll();
            }
        });

        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setOpaque(false);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        btnRow.add(delBtn);
        right.add(UIConstants.makeLabel("All Passengers", UIConstants.FONT_HEADING, UIConstants.TEXT_DARK), BorderLayout.NORTH);
        right.add(new JScrollPane(table), BorderLayout.CENTER);
        right.add(btnRow, BorderLayout.SOUTH);
        p.add(right, BorderLayout.CENTER);

        return p;
    }

    private void refreshPassengerTable() {
        passnTableModel.setRowCount(0);
        for (Passenger ps : passengers) {
            passnTableModel.addRow(new Object[]{
                ps.getId(), ps.getName(),
                ps.getStartLocation().getName(), ps.getDestination().getName(),
                ps.getRequiredArrivalTime().toString(),
                ps.getMaxWaitMinutes() + " min",
                String.format("%.2f", ps.getReliabilityScore())
            });
        }
    }

    // ── Helpers ──────────────────────────────────────────────────
    private JTextField addField(JPanel parent, String placeholder) {
        JTextField tf = UIConstants.makeTextField(placeholder);
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        parent.add(tf);
        parent.add(Box.createVerticalStrut(6));
        return tf;
    }

    private String getText(JTextField tf) {
        String t = tf.getText().trim();
        // return empty if still showing placeholder (grey)
        if (tf.getForeground().equals(Color.GRAY)) return "";
        return t;
    }

    private void clearFields(JTextField... fields) {
        for (JTextField f : fields) {
            f.setText("");
            f.setForeground(UIConstants.TEXT_DARK);
        }
    }

    private JTable makeStyledTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                c.setForeground(UIConstants.TEXT_DARK);
                return c;
            }
        };
        t.setFont(UIConstants.FONT_BODY);
        t.setRowHeight(32);
        t.getTableHeader().setFont(UIConstants.FONT_SUB);
        t.getTableHeader().setBackground(UIConstants.BG_MEDIUM);
        t.getTableHeader().setForeground(UIConstants.TEXT_WHITE);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(UIConstants.ACCENT_LIGHT);
        t.setSelectionForeground(UIConstants.TEXT_DARK);
        return t;
    }
}
