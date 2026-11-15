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
 * Driver Management Panel.
 * Shows all drivers in a table; supports Add, Edit, Delete, Toggle Availability.
 * Demonstrates: JTable, DefaultTableModel, form dialogs, exception handling.
 */
public class DriverPanel extends JPanel {

    private final MainWindow main;

    // Table
    private DefaultTableModel tableModel;
    private JTable            table;

    // Columns shown in the table
    private static final String[] COLS = {
        "ID", "Name", "Phone", "From", "Destination",
        "Departs", "Seats", "Max Detour", "Reliability", "Available"
    };

    public DriverPanel(MainWindow main) {
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
        JLabel title = Theme.label("Driver Management", Theme.F_TITLE, Theme.TEXT_MAIN);
        JLabel sub   = Theme.label("Add, edit and manage registered drivers", Theme.F_BODY, Theme.TEXT_SUB);
        JPanel left  = new JPanel(); left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title); left.add(Box.createVerticalStrut(2)); left.add(sub);
        p.add(left, BorderLayout.WEST);
        JButton addBtn = Theme.primaryButton("+ Add Driver");
        addBtn.addActionListener(e -> showDriverDialog(null));
        p.add(addBtn, BorderLayout.EAST);
        return p;
    }

    // ── Table ─────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == 9) return Boolean.class; return String.class;
            }
        };
        table = new JTable(tableModel);
        table.setFont(Theme.F_TABLE);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(Theme.PRIMARY_LIGHT);
        table.setSelectionForeground(Theme.TEXT_MAIN);

        JTableHeader hdr = table.getTableHeader();
        hdr.setFont(Theme.F_TABLE_H);
        hdr.setBackground(new Color(239, 246, 255));
        hdr.setForeground(Theme.PRIMARY);
        hdr.setReorderingAllowed(false);

        // Alternating row colours
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(sel ? Theme.PRIMARY_LIGHT : (row % 2 == 0 ? Color.WHITE : Theme.ROW_ALT));
                setForeground(Theme.TEXT_MAIN);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        // Column widths
        int[] widths = {60, 120, 100, 120, 120, 70, 50, 85, 85, 70};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    // ── Toolbar ───────────────────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        p.setOpaque(false);

        JButton editBtn   = Theme.primaryButton("✏ Edit");
        JButton deleteBtn = Theme.dangerButton("🗑 Delete");
        JButton toggleBtn = Theme.primaryButton("⟳ Toggle Availability");
        toggleBtn.setPreferredSize(new Dimension(190, 38));

        editBtn.addActionListener(e -> {
            Driver d = selectedDriver();
            if (d == null) { noSelection(); return; }
            showDriverDialog(d);
        });
        deleteBtn.addActionListener(e -> {
            Driver d = selectedDriver();
            if (d == null) { noSelection(); return; }
            int ok = JOptionPane.showConfirmDialog(this,
                "Delete driver " + d.getName() + "?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                main.userService.removeDriver(d.getUserId());
                refresh();
            }
        });
        toggleBtn.addActionListener(e -> {
            Driver d = selectedDriver();
            if (d == null) { noSelection(); return; }
            d.setAvailable(!d.isAvailable());
            refresh();
        });

        p.add(editBtn); p.add(deleteBtn); p.add(toggleBtn);
        return p;
    }

    // ── Refresh data ──────────────────────────────────────────────
    public void refresh() {
        tableModel.setRowCount(0);
        List<Driver> drivers = main.userService.getAllDrivers();
        for (Driver d : drivers) {
            tableModel.addRow(new Object[]{
                d.getUserId(), d.getName(), d.getPhone(),
                d.getStartLocation(), d.getDestination(),
                d.getDepartureTime().toString(),
                d.getAvailableSeats() + "/" + d.getTotalSeats(),
                String.format("%.1f km", d.getMaxDetourKm()),
                String.format("%.2f", d.getReliabilityScore()),
                d.isAvailable()
            });
        }
    }

    // ── Add / Edit dialog ─────────────────────────────────────────
    private void showDriverDialog(Driver existing) {
        boolean isEdit = (existing != null);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Driver" : "Add Driver", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(520, 640);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_PAGE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 4, 5, 4);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        // Fields
        JTextField idF          = field(isEdit ? existing.getUserId() : main.userService.nextDriverId());
        JTextField nameF        = field(isEdit ? existing.getName()         : "");
        JTextField phoneF       = field(isEdit ? existing.getPhone()        : "");
        JTextField startLocF    = field(isEdit ? existing.getStartLocation(): "");
        JTextField startLatF    = field(isEdit ? String.valueOf(existing.getStartLat())  : "28.6448");
        JTextField startLonF    = field(isEdit ? String.valueOf(existing.getStartLon())  : "77.2167");
        JTextField destF        = field(isEdit ? existing.getDestination()  : "IIT Delhi");
        JTextField destLatF     = field(isEdit ? String.valueOf(existing.getDestLat())   : "28.5459");
        JTextField destLonF     = field(isEdit ? String.valueOf(existing.getDestLon())   : "77.1926");
        JTextField deptF        = field(isEdit ? existing.getDepartureTime().toString()  : "08:00");
        JTextField seatsF       = field(isEdit ? String.valueOf(existing.getTotalSeats()) : "3");
        JTextField detourF      = field(isEdit ? String.valueOf(existing.getMaxDetourKm()): "5.0");
        JTextField relF         = field(isEdit ? String.valueOf(existing.getReliabilityScore()) : "0.85");
        // Vehicle fields
        JTextField regF   = field(isEdit && existing.getVehicle() != null ? existing.getVehicle().getRegistrationNumber() : "");
        JTextField makeF  = field(isEdit && existing.getVehicle() != null ? existing.getVehicle().getMake()  : "");
        JTextField modelF = field(isEdit && existing.getVehicle() != null ? existing.getVehicle().getModel() : "");
        JTextField colorF = field(isEdit && existing.getVehicle() != null ? existing.getVehicle().getColor() : "");

        if (isEdit) idF.setEditable(false);

        String[][] rows = {
            {"User ID", null}, {"Name", null}, {"Phone (10 digits)", null},
            {"Start Location", null}, {"Start Lat", "Start Lon"},
            {"Destination", null},    {"Dest Lat",  "Dest Lon"},
            {"Departure (HH:MM)", null}, {"Total Seats (1-8)", null},
            {"Max Detour (km)", null},   {"Reliability (0-1)", null},
            {"── Vehicle ──────────", null},
            {"Reg Number", null}, {"Make", null}, {"Model", null}, {"Color", null}
        };
        JTextField[][] flds = {
            {idF}, {nameF}, {phoneF},
            {startLocF}, {startLatF, startLonF},
            {destF},     {destLatF, destLonF},
            {deptF}, {seatsF}, {detourF}, {relF},
            {null},
            {regF}, {makeF}, {modelF}, {colorF}
        };

        int row = 0;
        for (int i = 0; i < rows.length; i++) {
            String lbl = rows[i][0];
            if (flds[i][0] == null) {
                // Section header
                gc.gridx=0; gc.gridy=row++; gc.gridwidth=4;
                form.add(Theme.label(lbl, Theme.F_SMALL, Theme.TEXT_SUB), gc);
                gc.gridwidth=1; continue;
            }
            gc.gridx=0; gc.gridy=row; gc.gridwidth=1; gc.weightx=0;
            form.add(Theme.label(lbl, Theme.F_SMALL, Theme.TEXT_MAIN), gc);
            if (rows[i][1] == null) {
                gc.gridx=1; gc.gridwidth=3; gc.weightx=1;
                form.add(flds[i][0], gc);
            } else {
                gc.gridx=1; gc.gridwidth=1; gc.weightx=0.5;
                form.add(flds[i][0], gc);
                gc.gridx=2; gc.weightx=0;
                form.add(Theme.label(rows[i][1], Theme.F_SMALL, Theme.TEXT_MAIN), gc);
                gc.gridx=3; gc.weightx=0.5;
                form.add(flds[i][1], gc);
            }
            row++;
        }

        JScrollPane sp = new JScrollPane(form);
        sp.setBorder(null);
        dlg.add(sp, BorderLayout.CENTER);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btns.setBackground(Theme.BG_PAGE);
        JButton cancel = new JButton("Cancel");
        cancel.setFont(Theme.F_BODY);
        cancel.addActionListener(e -> dlg.dispose());
        JButton save = Theme.primaryButton(isEdit ? "Update" : "Add Driver");
        save.addActionListener(e -> {
            try {
                String id      = Validator.requireNonBlank(idF.getText(), "User ID");
                String name    = Validator.requireNonBlank(nameF.getText(), "Name");
                String phone   = Validator.requirePhone(phoneF.getText(), "Phone");
                String sLoc    = Validator.requireNonBlank(startLocF.getText(), "Start Location");
                double sLat    = Validator.requireInRange(startLatF.getText(), "Start Lat", -90, 90);
                double sLon    = Validator.requireInRange(startLonF.getText(), "Start Lon", -180, 180);
                String dLoc    = Validator.requireNonBlank(destF.getText(), "Destination");
                double dLat    = Validator.requireInRange(destLatF.getText(), "Dest Lat", -90, 90);
                double dLon    = Validator.requireInRange(destLonF.getText(), "Dest Lon", -180, 180);
                String dept    = Validator.requireTime(deptF.getText(), "Departure Time");
                int seats      = Validator.requirePositiveInt(seatsF.getText(), "Seats");
                if (seats < 1 || seats > 8) throw new InvalidUserException("seats", "Seats must be 1-8");
                double detour  = Validator.requireNonNegDouble(detourF.getText(), "Max Detour");
                double rel     = Validator.requireInRange(relF.getText(), "Reliability", 0, 1);

                Vehicle veh = null;
                if (!regF.getText().trim().isEmpty()) {
                    veh = new Vehicle(
                        Validator.sanitize(regF.getText()),
                        Validator.sanitize(makeF.getText()),
                        Validator.sanitize(modelF.getText()),
                        Validator.sanitize(colorF.getText()),
                        seats
                    );
                }

                Driver d = new Driver(id, name, phone, rel, sLoc, sLat, sLon,
                    dLoc, dLat, dLon, LocalTime.parse(dept), seats, detour, veh);

                if (isEdit) main.userService.updateDriver(d);
                else        main.userService.addDriver(d);

                main.fileManager.saveDrivers(main.userService.getAllDrivers());
                refresh();
                dlg.dispose();
                JOptionPane.showMessageDialog(this,
                    (isEdit ? "Driver updated." : "Driver added successfully!"),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (InvalidUserException ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btns.add(cancel); btns.add(save);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── Helpers ───────────────────────────────────────────────────
    private JTextField field(String val) {
        JTextField tf = Theme.field(14);
        tf.setText(val);
        tf.setPreferredSize(new Dimension(140, 32));
        return tf;
    }

    private Driver selectedDriver() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        String id = (String) tableModel.getValueAt(row, 0);
        return main.userService.findDriver(id).orElse(null);
    }

    private void noSelection() {
        JOptionPane.showMessageDialog(this, "Please select a driver from the table.",
            "No Selection", JOptionPane.WARNING_MESSAGE);
    }
}
