package poolwise;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * Main application window with sidebar navigation and content panels.
 */
public class MainFrame extends JFrame {

    private CardLayout contentLayout;
    private JPanel     contentPanel;
    private JButton    activeNavBtn;

    // Shared data (sample loaded on start)
    private java.util.List<Driver>    drivers    = new java.util.ArrayList<>();
    private java.util.List<Passenger> passengers = new java.util.ArrayList<>();
    private OptimizationMode          currentMode = OptimizationMode.BALANCED;

    // Panel references (so we can refresh them)
    private DashboardPanel  dashPanel;
    private ManagePanel     managePanel;
    private OptimizePanel   optimizePanel;
    private ScenariosPanel  scenariosPanel;

    private static final String P_DASH     = "DASHBOARD";
    private static final String P_MANAGE   = "MANAGE";
    private static final String P_OPTIMIZE = "OPTIMIZE";
    private static final String P_SCENARIO = "SCENARIOS";

    public MainFrame() {
        UIConstants.setLook();
        setTitle("POOLWISE — Smart Carpool Optimization");
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));

        loadSampleData();

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildSidebar(),       BorderLayout.WEST);
        root.add(buildContentArea(),   BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Sidebar ──────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UIConstants.BG_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
            { setOpaque(false); }
        };
        sidebar.setPreferredSize(new Dimension(220, 720));
        sidebar.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(24, 20, 10, 20));

        // Logo
        JLabel logo = new JLabel("🚗  POOLWISE");
        logo.setFont(UIConstants.FONT_LOGO);
        logo.setForeground(UIConstants.ACCENT);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = UIConstants.makeLabel("Carpool Optimizer", UIConstants.FONT_SMALL, UIConstants.TEXT_GRAY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(logo);
        top.add(Box.createVerticalStrut(2));
        top.add(sub);
        top.add(Box.createVerticalStrut(28));

        // Nav items: label, panel key, emoji
        String[][] navItems = {
            {"📊", "Dashboard",   P_DASH},
            {"👥", "Manage",      P_MANAGE},
            {"⚡", "Optimize",    P_OPTIMIZE},
            {"🔄", "Scenarios",   P_SCENARIO},
        };

        for (String[] item : navItems) {
            JButton btn = makeNavButton(item[0] + "  " + item[1]);
            btn.addActionListener(e -> switchPanel(item[2], btn));
            top.add(btn);
            top.add(Box.createVerticalStrut(4));
            if (item[2].equals(P_DASH)) {
                activeNavBtn = btn;
                btn.setBackground(UIConstants.ACCENT);
            }
        }

        sidebar.add(top, BorderLayout.NORTH);

        // Bottom: user info
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255,255,255,30));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userIcon  = UIConstants.makeLabel("👤  User", UIConstants.FONT_BODY, UIConstants.TEXT_GRAY);
        userIcon.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton logoutBtn = makeNavButton("🚪  Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            LoginFrame.launch();
        });

        bottom.add(sep);
        bottom.add(Box.createVerticalStrut(12));
        bottom.add(userIcon);
        bottom.add(Box.createVerticalStrut(8));
        bottom.add(logoutBtn);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton makeNavButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getBackground().equals(UIConstants.ACCENT)) {
                    g2.setColor(UIConstants.ACCENT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(Color.WHITE);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255,255,255,15));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(UIConstants.TEXT_GRAY);
                } else {
                    g2.setColor(UIConstants.TEXT_GRAY);
                }
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 14, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(UIConstants.FONT_BODY);
        btn.setBackground(UIConstants.BG_DARK);
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setPreferredSize(new Dimension(180, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void switchPanel(String key, JButton btn) {
        if (activeNavBtn != null) activeNavBtn.setBackground(UIConstants.BG_DARK);
        btn.setBackground(UIConstants.ACCENT);
        activeNavBtn = btn;
        if (key.equals(P_OPTIMIZE)) optimizePanel.refresh(drivers, passengers);
        if (key.equals(P_DASH))     dashPanel.refresh(drivers, passengers);
        contentLayout.show(contentPanel, key);
    }

    // ── Content area ─────────────────────────────────────────────
    private JPanel buildContentArea() {
        contentLayout = new CardLayout();
        contentPanel  = new JPanel(contentLayout);
        contentPanel.setBackground(UIConstants.BG_LIGHT);

        dashPanel      = new DashboardPanel(drivers, passengers, this);
        managePanel    = new ManagePanel(drivers, passengers, this);
        optimizePanel  = new OptimizePanel(drivers, passengers, this);
        scenariosPanel = new ScenariosPanel(drivers, passengers, this);

        contentPanel.add(dashPanel,      P_DASH);
        contentPanel.add(managePanel,    P_MANAGE);
        contentPanel.add(optimizePanel,  P_OPTIMIZE);
        contentPanel.add(scenariosPanel, P_SCENARIO);

        return contentPanel;
    }

    // ── Sample data ──────────────────────────────────────────────
    public void loadSampleData() {
        drivers.clear();
        passengers.clear();

        Location college = new Location("College", 28.5459, 77.1926);

        drivers.add(new Driver("D1", "Rahul",  0.92, new Location("Rahul Home - Connaught Pl", 28.6139, 77.2090), college, java.time.LocalTime.of(8,  0), 3, 5.0));
        drivers.add(new Driver("D2", "Arjun",  0.88, new Location("Arjun Home - Kashmiri Gate", 28.6353, 77.2250), college, java.time.LocalTime.of(8, 15), 2, 4.0));
        drivers.add(new Driver("D3", "Mehak",  0.95, new Location("Mehak Home - AIIMS",         28.5921, 77.2219), college, java.time.LocalTime.of(8, 10), 3, 6.0));
        drivers.add(new Driver("D4", "Priya",  0.78, new Location("Priya Home - Ghaziabad",     28.6692, 77.4538), college, java.time.LocalTime.of(7, 30), 2, 3.0));

        passengers.add(new Passenger("P1", "Amit",   0.90, new Location("Karol Bagh",    28.6519, 77.1902), college, java.time.LocalTime.of(9,30), 20));
        passengers.add(new Passenger("P2", "Sneha",  0.85, new Location("Rajouri Garden",28.6485, 77.1225), college, java.time.LocalTime.of(9,30), 25));
        passengers.add(new Passenger("P3", "Vikram", 0.88, new Location("Lajpat Nagar",  28.5706, 77.2434), college, java.time.LocalTime.of(9,30), 15));
        passengers.add(new Passenger("P4", "Priti",  0.82, new Location("Saket",         28.5244, 77.2167), college, java.time.LocalTime.of(9,30), 20));
        passengers.add(new Passenger("P5", "Rohan",  0.79, new Location("Dwarka",        28.5823, 77.0500), college, java.time.LocalTime.of(9,30), 30));
        passengers.add(new Passenger("P6", "Nisha",  0.91, new Location("Vasant Kunj",   28.5244, 77.1500), college, java.time.LocalTime.of(9,30), 20));
    }

    public java.util.List<Driver>    getDrivers()    { return drivers; }
    public java.util.List<Passenger> getPassengers() { return passengers; }
    public OptimizationMode          getMode()       { return currentMode; }
    public void setMode(OptimizationMode m)          { this.currentMode = m; }

    public void refreshAll() {
        dashPanel.refresh(drivers, passengers);
        optimizePanel.refresh(drivers, passengers);
    }
}
