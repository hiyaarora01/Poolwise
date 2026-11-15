package com.poolwise.ui;

import com.poolwise.data.FileManager;
import com.poolwise.service.*;
import com.poolwise.util.SampleDataLoader;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;

/** Main application window with sidebar navigation and content panels. */
public class MainWindow extends JFrame {

    // ── Services ──────────────────────────────────────────────────
    final UserService     userService;
    final RouteService    routeService;
    final MatchingService matchingService;
    final FileManager     fileManager;

    // ── Panels ────────────────────────────────────────────────────
    private DashboardPanel   dashPanel;
    private DriverPanel      driverPanel;
    private PassengerPanel   passengerPanel;
    private MatchingPanel    matchingPanel;
    private ImpactPanel      impactPanel;
    private SimulationPanel  simPanel;
    private MapPanel         mapPanel;

    private CardLayout  contentLayout;
    private JPanel      contentArea;
    private JButton     activeNav;

    static final String P_DASH    = "DASHBOARD";
    static final String P_DRIVERS = "DRIVERS";
    static final String P_PASS    = "PASSENGERS";
    static final String P_MATCH   = "MATCHING";
    static final String P_IMPACT  = "IMPACT";
    static final String P_SIM     = "SIMULATION";
    static final String P_MAP     = "MAP";

    public MainWindow() {
        Theme.applyLook();
        // Initialise services
        userService     = new UserService();
        routeService    = new RouteService();
        matchingService = new MatchingService(routeService);
        fileManager     = new FileManager();

        // Try loading saved data; fall back to sample
        if (fileManager.dataExists()) {
            userService.loadAll(fileManager.loadDrivers(), fileManager.loadPassengers());
        } else {
            SampleDataLoader.load(userService);
        }

        setTitle("POOLWISE — Smart Carpool Optimization");
        setSize(1280, 760);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildSidebar(),     BorderLayout.WEST);
        root.add(buildContentArea(), BorderLayout.CENTER);
        setContentPane(root);

        // Auto-save on close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                fileManager.saveDrivers(userService.getAllDrivers());
                fileManager.savePassengers(userService.getAllPassengers());
            }
        });
    }

    // ── Sidebar ───────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Theme.BG_SIDEBAR); g.fillRect(0,0,getWidth(),getHeight());
                super.paintComponent(g);
            }
            { setOpaque(false); }
        };
        sidebar.setPreferredSize(new Dimension(220,760));
        sidebar.setLayout(new BorderLayout());

        JPanel top = new JPanel(); top.setOpaque(false);
        top.setLayout(new BoxLayout(top,BoxLayout.Y_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(24,16,10,16));

        JLabel logo = Theme.label("🚗 POOLWISE",Theme.F_LOGO,new Color(147,197,253));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub  = Theme.label("Carpool Optimizer",Theme.F_SMALL,Theme.TEXT_SUB);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(logo); top.add(Box.createVerticalStrut(2)); top.add(sub);
        top.add(Box.createVerticalStrut(28));

        Object[][] navItems = {
            {"📊","Dashboard",   P_DASH},
            {"🚗","Drivers",     P_DRIVERS},
            {"👤","Passengers",  P_PASS},
            {"⚡","Find Carpool",P_MATCH},
            {"🗺️","Map & Routes", P_MAP},
            {"🌱","Eco Impact",  P_IMPACT},
            {"🔄","Simulation",  P_SIM},
        };
        for (Object[] ni : navItems) {
            JButton btn = makeNavBtn(ni[0]+" "+ni[1]);
            final String key = (String)ni[2];
            btn.addActionListener(e -> switchTo(key, btn));
            top.add(btn); top.add(Box.createVerticalStrut(2));
            if (key.equals(P_DASH)) { activeNav = btn; setNavActive(btn,true); }
        }

        sidebar.add(top,BorderLayout.NORTH);

        JPanel bot = new JPanel(); bot.setOpaque(false);
        bot.setLayout(new BoxLayout(bot,BoxLayout.Y_AXIS));
        bot.setBorder(BorderFactory.createEmptyBorder(10,16,20,16));
        JSeparator sep = new JSeparator(); sep.setForeground(new Color(255,255,255,30));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        bot.add(sep); bot.add(Box.createVerticalStrut(10));
        JButton logout = makeNavBtn("🚪 Logout");
        logout.addActionListener(e -> { fileManager.saveDrivers(userService.getAllDrivers());
            fileManager.savePassengers(userService.getAllPassengers());
            dispose(); LoginScreen.launch(); });
        bot.add(logout);
        sidebar.add(bot,BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton makeNavBtn(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = getBackground().equals(Theme.PRIMARY);
                if (active) { g2.setColor(Theme.PRIMARY); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8)); }
                else if (getModel().isRollover()) { g2.setColor(new Color(255,255,255,18)); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8)); }
                g2.setColor(active ? Color.WHITE : Theme.TEXT_SUB); g2.setFont(Theme.F_BODY);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),14,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setBackground(Theme.BG_SIDEBAR); b.setFont(Theme.F_BODY);
        b.setMaximumSize(new Dimension(195,38)); b.setPreferredSize(new Dimension(190,38));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void setNavActive(JButton b, boolean active) {
        b.setBackground(active ? Theme.PRIMARY : Theme.BG_SIDEBAR);
    }

    private void switchTo(String key, JButton btn) {
        if (activeNav != null) setNavActive(activeNav, false);
        setNavActive(btn, true); activeNav = btn;
        // Refresh target panel before showing
        switch(key) {
            case P_DASH    -> dashPanel.refresh();
            case P_DRIVERS -> driverPanel.refresh();
            case P_PASS    -> passengerPanel.refresh();
            case P_MATCH   -> matchingPanel.refresh();
            case P_IMPACT  -> impactPanel.refresh();
            case P_SIM     -> simPanel.refresh();
            case P_MAP     -> mapPanel.refresh();
        }
        contentLayout.show(contentArea, key);
    }

    // ── Content area ──────────────────────────────────────────────
    private JPanel buildContentArea() {
        contentLayout = new CardLayout();
        contentArea   = new JPanel(contentLayout);
        contentArea.setBackground(Theme.BG_PAGE);

        dashPanel      = new DashboardPanel(this);
        driverPanel    = new DriverPanel(this);
        passengerPanel = new PassengerPanel(this);
        matchingPanel  = new MatchingPanel(this);
        impactPanel    = new ImpactPanel(this);
        simPanel       = new SimulationPanel(this);
        mapPanel       = new MapPanel(this);

        contentArea.add(dashPanel,      P_DASH);
        contentArea.add(driverPanel,    P_DRIVERS);
        contentArea.add(passengerPanel, P_PASS);
        contentArea.add(matchingPanel,  P_MATCH);
        contentArea.add(impactPanel,    P_IMPACT);
        contentArea.add(simPanel,       P_SIM);
        contentArea.add(mapPanel,       P_MAP);
        return contentArea;
    }

    public void refreshAll() {
        dashPanel.refresh();
        driverPanel.refresh();
        passengerPanel.refresh();
    }
}
