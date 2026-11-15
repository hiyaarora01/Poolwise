package com.poolwise.ui;

import com.poolwise.model.*;
import com.poolwise.service.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/** Dashboard — statistics cards + active carpools + driver table. */
public class DashboardPanel extends JPanel {

    private final MainWindow main;
    private JPanel statsRow;

    public DashboardPanel(MainWindow main) {
        this.main = main;
        setBackground(Theme.BG_PAGE);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(28,28,28,28));

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(20,0,0,0));

        statsRow = new JPanel(new GridLayout(1,4,16,0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,110));
        body.add(statsRow);
        body.add(Box.createVerticalStrut(20));
        body.add(buildBottomRow());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null); scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        statsRow.removeAll();
        UserService us = main.userService;
        List<Carpool> carpools = main.matchingService.getLastCarpools();

        statsRow.add(Theme.statCard(String.valueOf(us.driverCount()),    "Total Drivers",        Theme.PRIMARY));
        statsRow.add(Theme.statCard(String.valueOf(us.passengerCount()), "Total Passengers",     Theme.SUCCESS));
        statsRow.add(Theme.statCard(String.valueOf(us.countAvailableVehicles()), "Available Vehicles", Theme.ACCENT));
        statsRow.add(Theme.statCard(String.valueOf(us.countMatched()),   "Passengers Matched",   Theme.WARNING));
        statsRow.revalidate(); statsRow.repaint();
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout()); h.setOpaque(false);
        h.setBorder(BorderFactory.createEmptyBorder(0,0,4,0));
        JPanel left = new JPanel(); left.setOpaque(false);
        left.setLayout(new BoxLayout(left,BoxLayout.Y_AXIS));
        left.add(Theme.label("Dashboard",Theme.F_TITLE,Theme.TEXT_MAIN));
        left.add(Box.createVerticalStrut(2));
        left.add(Theme.label("Overview of your POOLWISE network",Theme.F_BODY,Theme.TEXT_SUB));
        h.add(left,BorderLayout.WEST);

        JButton refreshBtn = Theme.primaryButton("⟳ Refresh");
        refreshBtn.addActionListener(e -> refresh());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT)); right.setOpaque(false);
        right.add(refreshBtn); h.add(right,BorderLayout.EAST);
        return h;
    }

    private JPanel buildBottomRow() {
        JPanel row = new JPanel(new GridLayout(1,2,16,0));
        row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,340));

        // Driver table card
        JPanel dCard = Theme.card(14);
        dCard.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        dCard.setLayout(new BorderLayout(0,10));
        dCard.add(Theme.label("Registered Drivers",Theme.F_HEADING,Theme.TEXT_MAIN),BorderLayout.NORTH);

        String[] dCols = {"ID","Name","From","To","Departs","Seats","Avail."};
        List<Driver> drivers = main.userService.getAllDrivers();
        Object[][] dData = new Object[drivers.size()][7];
        for (int i=0;i<drivers.size();i++) {
            Driver d = drivers.get(i);
            dData[i] = new Object[]{d.getUserId(),d.getName(),d.getStartLocation(),
                d.getDestination(),d.getDepartureTime(),d.getAvailableSeats(),d.isAvailable()?"✓":"✗"};
        }
        JTable dTable = makeTable(dData, dCols);
        dCard.add(new JScrollPane(dTable),BorderLayout.CENTER);
        row.add(dCard);

        // Carpool groups card
        JPanel cCard = Theme.card(14);
        cCard.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        cCard.setLayout(new BorderLayout(0,10));
        cCard.add(Theme.label("Active Carpool Groups",Theme.F_HEADING,Theme.TEXT_MAIN),BorderLayout.NORTH);

        List<Carpool> carpools = main.matchingService.getLastCarpools();
        if (carpools.isEmpty()) {
            JLabel none = Theme.label("No carpools yet — go to 'Find Carpool' to run matching.",Theme.F_BODY,Theme.TEXT_SUB);
            none.setHorizontalAlignment(SwingConstants.CENTER);
            cCard.add(none, BorderLayout.CENTER);
        } else {
            String[] cCols = {"Group","Driver","Passengers","Extra km","CO₂ Saved"};
            Object[][] cData = new Object[carpools.size()][5];
            for (int i=0;i<carpools.size();i++) {
                Carpool cp = carpools.get(i);
                String pNames = cp.getPassengers().stream().map(Passenger::getName)
                    .reduce((a,b)->a+", "+b).orElse("—");
                cData[i] = new Object[]{cp.getCarpoolId(),cp.getDriver().getName(),pNames,
                    String.format("%.2f",cp.getExtraDistanceKm()),
                    String.format("%.2f kg",cp.getEstimatedCO2SavedKg())};
            }
            cCard.add(new JScrollPane(makeTable(cData,cCols)),BorderLayout.CENTER);
        }
        row.add(cCard);
        return row;
    }

    private JTable makeTable(Object[][] data, String[] cols) {
        JTable t = new JTable(new DefaultTableModel(data,cols){public boolean isCellEditable(int r,int c){return false;}}) {
            @Override public Component prepareRenderer(TableCellRenderer r,int row,int col) {
                Component c = super.prepareRenderer(r,row,col);
                c.setBackground(row%2==0 ? Color.WHITE : Theme.ROW_ALT);
                c.setForeground(Theme.TEXT_MAIN); return c;
            }
        };
        t.setFont(Theme.F_TABLE); t.setRowHeight(28);
        t.getTableHeader().setFont(Theme.F_TABLE_H);
        t.getTableHeader().setBackground(new Color(239,246,255));
        t.getTableHeader().setForeground(Theme.PRIMARY);
        t.setShowGrid(false); t.setIntercellSpacing(new Dimension(0,1));
        t.setSelectionBackground(Theme.PRIMARY_LIGHT); return t;
    }
}
