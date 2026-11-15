package com.poolwise.ui;

import com.poolwise.exception.InvalidUserException;
import com.poolwise.model.*;
import com.poolwise.util.Validator;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalTime;
import java.util.List;

/**
 * Passenger Management Panel — fully fixed version.
 *
 * FIX 1: Table column 9 ("Matched") now uses String.class (was Boolean.class
 *        which caused a ClassCastException → frozen/blank table).
 * FIX 2: Custom renderer properly checks the string value, not Boolean.
 * FIX 3: All column widths and toolbar correctly scoped to Passenger fields.
 */
public class PassengerPanel extends JPanel {

    private final MainWindow main;

    private DefaultTableModel tableModel;
    private JTable            table;

    private static final String[] COLS = {
        "ID", "Name", "Phone", "Pickup Location", "Destination",
        "Arrive By", "Max Wait (min)", "Max Detour (km)", "Reliability", "Matched / Driver"
    };

    public PassengerPanel(MainWindow main) {
        this.main = main;
        setBackground(Theme.BG_PAGE);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        add(buildToolbar(), BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JLabel title = Theme.label("Passenger Management", Theme.F_TITLE, Theme.TEXT_MAIN);
        JLabel sub   = Theme.label("Add, edit and manage registered passengers",
                                    Theme.F_BODY, Theme.TEXT_SUB);
        JPanel left = new JPanel(); left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title); left.add(Box.createVerticalStrut(2)); left.add(sub);
        p.add(left, BorderLayout.WEST);

        JButton addBtn = Theme.primaryButton("+ Add Passenger");
        addBtn.addActionListener(e -> showPassengerDialog(null));
        p.add(addBtn, BorderLayout.EAST);
        return p;
    }

    // ── Table ─────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        // FIX: All columns are String — no Boolean column that breaks rendering
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return String.class; }
        };

        table = new JTable(tableModel);
        table.setFont(Theme.F_TABLE);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(Theme.PRIMARY_LIGHT);
        table.setSelectionForeground(Theme.TEXT_MAIN);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        JTableHeader hdr = table.getTableHeader();
        hdr.setFont(Theme.F_TABLE_H);
        hdr.setBackground(new Color(239, 246, 255));
        hdr.setForeground(Theme.PRIMARY);
        hdr.setReorderingAllowed(false);

        // FIX: renderer checks String "No" vs driver-id — no Boolean cast
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);

                // Safe: col 9 is always a String now
                Object matchedVal = tableModel.getValueAt(row, 9);
                boolean isMatched = matchedVal != null
                    && !matchedVal.toString().equalsIgnoreCase("No")
                    && !matchedVal.toString().isEmpty();

                if (sel) {
                    setBackground(Theme.PRIMARY_LIGHT);
                    setForeground(Theme.TEXT_MAIN);
                } else if (isMatched) {
                    setBackground(new Color(240, 255, 240)); // light green for matched
                    setForeground(col == 9 ? Theme.SUCCESS : Theme.TEXT_MAIN);
                } else {
                    setBackground(row % 2 == 0 ? Color.WHITE : Theme.ROW_ALT);
                    setForeground(Theme.TEXT_MAIN);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        };

        // Apply renderer to all columns
        for (int i = 0; i < COLS.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Column widths
        int[] widths = {55, 110, 100, 130, 110, 65, 80, 90, 80, 100};
        for (int i = 0; i < widths.length && i < COLS.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    // ── Toolbar ───────────────────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        p.setOpaque(false);

        JButton editBtn    = Theme.primaryButton("✏  Edit");
        JButton deleteBtn  = Theme.dangerButton("🗑  Delete");
        JButton clearBtn   = Theme.primaryButton("↺  Clear Match");
        JButton refreshBtn = Theme.primaryButton("⟳  Refresh");

        editBtn.addActionListener(e -> {
            Passenger ps = selectedPassenger();
            if (ps == null) { noSelection(); return; }
            showPassengerDialog(ps);
        });

        deleteBtn.addActionListener(e -> {
            Passenger ps = selectedPassenger();
            if (ps == null) { noSelection(); return; }
            int ok = JOptionPane.showConfirmDialog(this,
                "Delete passenger " + ps.getName() + "?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                main.userService.removePassenger(ps.getUserId());
                main.fileManager.savePassengers(main.userService.getAllPassengers());
                refresh();
                JOptionPane.showMessageDialog(this, "Passenger deleted.", "Deleted",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });

        clearBtn.addActionListener(e -> {
            Passenger ps = selectedPassenger();
            if (ps == null) { noSelection(); return; }
            ps.clearAssignment();
            refresh();
        });

        refreshBtn.addActionListener(e -> refresh());

        p.add(editBtn); p.add(deleteBtn); p.add(clearBtn); p.add(refreshBtn);

        // Summary label
        JLabel summary = Theme.label("", Theme.F_SMALL, Theme.TEXT_SUB);
        summary.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));

        // Update summary on table data change
        tableModel.addTableModelListener(e2 -> {
            int total   = tableModel.getRowCount();
            long matched = 0;
            for (int r = 0; r < total; r++) {
                Object v = tableModel.getValueAt(r, 9);
                if (v != null && !v.toString().equalsIgnoreCase("No") && !v.toString().isEmpty())
                    matched++;
            }
            summary.setText("Total: " + total + "  |  Matched: " + matched
                + "  |  Unmatched: " + (total - matched));
        });
        p.add(summary);
        return p;
    }

    // ── Refresh ───────────────────────────────────────────────────
    public void refresh() {
        // Always run on EDT
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refresh);
            return;
        }
        tableModel.setRowCount(0);
        List<Passenger> passengers = main.userService.getAllPassengers();
        for (Passenger ps : passengers) {
            String matchInfo;
            if (ps.isMatched() && ps.getAssignedDriverId() != null
                    && !ps.getAssignedDriverId().isBlank()) {
                // Show the driver's name if we can find them
                String driverName = main.userService.findDriver(ps.getAssignedDriverId())
                    .map(d -> d.getName() + " (" + ps.getAssignedDriverId() + ")")
                    .orElse(ps.getAssignedDriverId());
                matchInfo = driverName;
            } else {
                matchInfo = "No";
            }
            tableModel.addRow(new Object[]{
                ps.getUserId(),
                ps.getName(),
                ps.getPhone(),
                ps.getPickupLocation(),
                ps.getDestination(),
                ps.getRequiredArrivalTime().toString(),
                String.format("%.0f", ps.getMaxWaitMinutes()),
                String.format("%.1f", ps.getMaxDetourKm()),
                String.format("%.2f", ps.getReliabilityScore()),
                matchInfo   // String, never Boolean
            });
        }
        table.repaint();
    }

    // ── Add / Edit dialog ─────────────────────────────────────────
    private void showPassengerDialog(Passenger existing) {
        boolean isEdit = (existing != null);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Passenger" : "Add Passenger",
            java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(520, 600);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_PAGE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 4, 5, 4);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        // ── Form fields ──────────────────────────────────────────
        JTextField idF      = field(isEdit ? existing.getUserId()                      : main.userService.nextPassengerId());
        JTextField nameF    = field(isEdit ? existing.getName()                        : "");
        JTextField phoneF   = field(isEdit ? existing.getPhone()                       : "");
        JTextField pickupF  = field(isEdit ? existing.getPickupLocation()              : "");
        JTextField pLatF    = field(isEdit ? fmt(existing.getPickupLat())              : "28.6448");
        JTextField pLonF    = field(isEdit ? fmt(existing.getPickupLon())              : "77.2167");
        JTextField destF    = field(isEdit ? existing.getDestination()                 : "IIT Delhi");
        JTextField dLatF    = field(isEdit ? fmt(existing.getDestLat())                : "28.5459");
        JTextField dLonF    = field(isEdit ? fmt(existing.getDestLon())                : "77.1926");
        JTextField arrivalF = field(isEdit ? existing.getRequiredArrivalTime().toString() : "09:00");
        JTextField waitF    = field(isEdit ? fmt(existing.getMaxWaitMinutes())         : "15");
        JTextField detourF  = field(isEdit ? fmt(existing.getMaxDetourKm())            : "5.0");
        JTextField relF     = field(isEdit ? fmt(existing.getReliabilityScore())       : "0.80");

        if (isEdit) idF.setEditable(false);

        // Row definitions: {label, secondLabel or null}
        Object[][] rows = {
            {"User ID",              null},
            {"Full Name",            null},
            {"Phone (10 digits)",    null},
            {"Pickup Location",      null},
            {"Pickup Latitude",      "Pickup Longitude"},
            {"Destination",          null},
            {"Dest Latitude",        "Dest Longitude"},
            {"Required Arrival (HH:MM)", null},
            {"Max Wait (minutes)",   null},
            {"Max Detour (km)",      null},
            {"Reliability (0.0–1.0)",null}
        };
        JTextField[][] flds = {
            {idF}, {nameF}, {phoneF},
            {pickupF}, {pLatF, pLonF},
            {destF},   {dLatF, dLonF},
            {arrivalF}, {waitF}, {detourF}, {relF}
        };

        int row = 0;
        for (int i = 0; i < rows.length; i++) {
            String lbl1 = (String) rows[i][0];
            String lbl2 = (String) rows[i][1];

            gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0;
            form.add(Theme.label(lbl1, Theme.F_SMALL, Theme.TEXT_MAIN), gc);

            if (lbl2 == null) {
                gc.gridx = 1; gc.gridwidth = 3; gc.weightx = 1;
                form.add(flds[i][0], gc);
            } else {
                gc.gridx = 1; gc.gridwidth = 1; gc.weightx = 0.45;
                form.add(flds[i][0], gc);
                gc.gridx = 2; gc.weightx = 0;
                form.add(Theme.label(lbl2, Theme.F_SMALL, Theme.TEXT_MAIN), gc);
                gc.gridx = 3; gc.weightx = 0.45;
                form.add(flds[i][1], gc);
            }
            row++;
        }

        // Hint row
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4; gc.weightx = 1;
        JLabel hint = Theme.label(
            "Tip: Lat/Lon for Delhi area is approx 28.4–28.9 N, 76.8–77.4 E",
            Theme.F_SMALL, Theme.TEXT_SUB);
        hint.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        form.add(hint, gc);

        JScrollPane sp = new JScrollPane(form); sp.setBorder(null);
        dlg.add(sp, BorderLayout.CENTER);

        // ── Buttons ──────────────────────────────────────────────
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btns.setBackground(Theme.BG_PAGE);
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        JButton cancel = new JButton("Cancel");
        cancel.setFont(Theme.F_BODY);
        cancel.addActionListener(e -> dlg.dispose());

        JButton save = Theme.primaryButton(isEdit ? "Update Passenger" : "Add Passenger");
        save.addActionListener(e -> {
            try {
                String id      = Validator.requireNonBlank(idF.getText(),     "User ID");
                String name    = Validator.requireNonBlank(nameF.getText(),   "Name");
                String phone   = Validator.requirePhone(phoneF.getText(),     "Phone");
                String pickup  = Validator.requireNonBlank(pickupF.getText(), "Pickup Location");
                double pLat    = Validator.requireInRange(pLatF.getText(),    "Pickup Lat", -90,   90);
                double pLon    = Validator.requireInRange(pLonF.getText(),    "Pickup Lon", -180, 180);
                String dest    = Validator.requireNonBlank(destF.getText(),   "Destination");
                double dLat    = Validator.requireInRange(dLatF.getText(),    "Dest Lat",   -90,   90);
                double dLon    = Validator.requireInRange(dLonF.getText(),    "Dest Lon",   -180, 180);
                String arrival = Validator.requireTime(arrivalF.getText(),    "Arrive By");
                double maxWait = Validator.requireNonNegDouble(waitF.getText(),"Max Wait");
                double detour  = Validator.requireNonNegDouble(detourF.getText(),"Max Detour");
                double rel     = Validator.requireInRange(relF.getText(),     "Reliability", 0, 1);

                Passenger p = new Passenger(
                    id, name, phone, rel,
                    pickup, pLat, pLon,
                    dest,   dLat, dLon,
                    LocalTime.parse(arrival), maxWait, detour);

                if (isEdit) main.userService.updatePassenger(p);
                else        main.userService.addPassenger(p);

                main.fileManager.savePassengers(main.userService.getAllPassengers());
                refresh();
                dlg.dispose();
                JOptionPane.showMessageDialog(this,
                    isEdit ? "Passenger updated successfully." : "Passenger added successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (InvalidUserException ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(),
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg,
                    "Unexpected error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btns.add(cancel);
        btns.add(save);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── Helpers ───────────────────────────────────────────────────
    private JTextField field(String val) {
        JTextField tf = Theme.field(14);
        tf.setText(val);
        tf.setPreferredSize(new Dimension(145, 32));
        return tf;
    }

    private static String fmt(double v) {
        return String.valueOf(v);
    }

    private Passenger selectedPassenger() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        // Convert view row to model row (handles sorting)
        int modelRow = table.convertRowIndexToModel(row);
        Object idObj = tableModel.getValueAt(modelRow, 0);
        if (idObj == null) return null;
        return main.userService.findPassenger(idObj.toString()).orElse(null);
    }

    private void noSelection() {
        JOptionPane.showMessageDialog(this,
            "Please select a passenger from the table first.",
            "No Selection", JOptionPane.WARNING_MESSAGE);
    }
}
