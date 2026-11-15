package poolwise;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Login / Registration window for POOLWISE.
 */
public class LoginFrame extends JFrame {

    private JPanel cardPanel;
    private CardLayout cardLayout;
    private static final String CARD_LOGIN    = "LOGIN";
    private static final String CARD_REGISTER = "REGISTER";

    public LoginFrame() {
        UIConstants.setLook();
        setTitle("POOLWISE — Login");
        setSize(900, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildLeftPanel(),  BorderLayout.WEST);
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.add(buildLoginPanel(),    CARD_LOGIN);
        cardPanel.add(buildRegisterPanel(), CARD_REGISTER);
        root.add(cardPanel, BorderLayout.CENTER);

        setContentPane(root);
    }

    // ── Left hero panel ──────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UIConstants.BG_DARK, 0, getHeight(), new Color(20,100,90));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // decorative circles
                g2.setColor(new Color(20, 184, 166, 40));
                g2.fillOval(-40, -40, 200, 200);
                g2.setColor(new Color(20, 184, 166, 25));
                g2.fillOval(80, 300, 250, 250);
                g2.dispose();
                super.paintComponent(g);
            }
            { setOpaque(false); }
        };
        p.setPreferredSize(new Dimension(340, 580));
        p.setLayout(new GridBagLayout());

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        // Logo
        JLabel logo = new JLabel("🚗 POOLWISE");
        logo.setFont(UIConstants.FONT_LOGO);
        logo.setForeground(UIConstants.ACCENT);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = UIConstants.makeLabel("Smart Carpool Optimization", UIConstants.FONT_BODY, UIConstants.TEXT_GRAY);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(Box.createVerticalStrut(10));
        inner.add(logo);
        inner.add(Box.createVerticalStrut(6));
        inner.add(tagline);
        inner.add(Box.createVerticalStrut(40));

        // Feature bullets
        String[][] features = {
            {"🎯", "Smart Route Matching"},
            {"⛽", "Fuel & CO₂ Savings"},
            {"⏱️", "5 Optimization Modes"},
            {"📊", "Real-time Analytics"},
            {"🔄", "Dynamic Re-routing"}
        };
        for (String[] f : features) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(300, 30));
            JLabel icon = UIConstants.makeLabel(f[0], UIConstants.FONT_BODY, UIConstants.TEXT_WHITE);
            JLabel text = UIConstants.makeLabel(f[1], UIConstants.FONT_BODY, UIConstants.TEXT_GRAY);
            row.add(icon); row.add(text);
            inner.add(row);
        }

        p.add(inner);
        return p;
    }

    // ── Login panel ──────────────────────────────────────────────
    private JPanel buildLoginPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UIConstants.BG_LIGHT);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setMaximumSize(new Dimension(320, 500));

        JLabel title = UIConstants.makeLabel("Welcome back!", UIConstants.FONT_TITLE, UIConstants.TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub   = UIConstants.makeLabel("Login to your POOLWISE account", UIConstants.FONT_BODY, UIConstants.TEXT_GRAY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField  emailField = UIConstants.makeTextField("Email address");
        emailField.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailField.setMaximumSize(new Dimension(300, 42));

        JPasswordField passField = UIConstants.makePasswordField("Password");
        passField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passField.setMaximumSize(new Dimension(300, 42));

        JButton loginBtn = UIConstants.makePrimaryButton("Login");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(300, 42));
        loginBtn.addActionListener(e -> openDashboard());

        JButton guestBtn = UIConstants.makeOutlineButton("Continue as Guest");
        guestBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        guestBtn.setMaximumSize(new Dimension(300, 42));
        guestBtn.addActionListener(e -> openDashboard());

        JPanel switchRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        switchRow.setOpaque(false);
        switchRow.add(UIConstants.makeLabel("Don't have an account?", UIConstants.FONT_SMALL, UIConstants.TEXT_GRAY));
        JButton toReg = new JButton("Register");
        toReg.setFont(UIConstants.FONT_SMALL);
        toReg.setForeground(UIConstants.ACCENT);
        toReg.setContentAreaFilled(false); toReg.setBorderPainted(false); toReg.setFocusPainted(false);
        toReg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toReg.addActionListener(e -> cardLayout.show(cardPanel, CARD_REGISTER));
        switchRow.add(toReg);

        form.add(title);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(28));
        form.add(UIConstants.makeLabel("Email", UIConstants.FONT_SMALL, UIConstants.TEXT_DARK));
        form.add(Box.createVerticalStrut(4));
        form.add(emailField);
        form.add(Box.createVerticalStrut(14));
        form.add(UIConstants.makeLabel("Password", UIConstants.FONT_SMALL, UIConstants.TEXT_DARK));
        form.add(Box.createVerticalStrut(4));
        form.add(passField);
        form.add(Box.createVerticalStrut(20));
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(10));
        form.add(guestBtn);
        form.add(Box.createVerticalStrut(16));
        form.add(switchRow);

        p.add(form);
        return p;
    }

    // ── Register panel ───────────────────────────────────────────
    private JPanel buildRegisterPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UIConstants.BG_LIGHT);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = UIConstants.makeLabel("Create Account", UIConstants.FONT_TITLE, UIConstants.TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub   = UIConstants.makeLabel("Join POOLWISE today", UIConstants.FONT_BODY, UIConstants.TEXT_GRAY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField nameField  = UIConstants.makeTextField("Full name");
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameField.setMaximumSize(new Dimension(300, 42));

        JTextField emailField = UIConstants.makeTextField("Email address");
        emailField.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailField.setMaximumSize(new Dimension(300, 42));

        JPasswordField passField = UIConstants.makePasswordField("Password");
        passField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passField.setMaximumSize(new Dimension(300, 42));

        // Role selection
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rolePanel.setOpaque(false);
        rolePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JRadioButton driverRb  = new JRadioButton("Driver");
        JRadioButton passnRb   = new JRadioButton("Passenger", true);
        driverRb.setFont(UIConstants.FONT_BODY); driverRb.setOpaque(false);
        passnRb.setFont(UIConstants.FONT_BODY);  passnRb.setOpaque(false);
        ButtonGroup bg = new ButtonGroup(); bg.add(driverRb); bg.add(passnRb);
        rolePanel.add(UIConstants.makeLabel("Register as:", UIConstants.FONT_SMALL, UIConstants.TEXT_DARK));
        rolePanel.add(driverRb); rolePanel.add(passnRb);

        JButton regBtn = UIConstants.makePrimaryButton("Create Account");
        regBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        regBtn.setMaximumSize(new Dimension(300, 42));
        regBtn.addActionListener(e -> openDashboard());

        JPanel switchRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        switchRow.setOpaque(false);
        switchRow.add(UIConstants.makeLabel("Already have an account?", UIConstants.FONT_SMALL, UIConstants.TEXT_GRAY));
        JButton toLogin = new JButton("Login");
        toLogin.setFont(UIConstants.FONT_SMALL); toLogin.setForeground(UIConstants.ACCENT);
        toLogin.setContentAreaFilled(false); toLogin.setBorderPainted(false); toLogin.setFocusPainted(false);
        toLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toLogin.addActionListener(e -> cardLayout.show(cardPanel, CARD_LOGIN));
        switchRow.add(toLogin);

        form.add(title);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(20));
        form.add(UIConstants.makeLabel("Full Name", UIConstants.FONT_SMALL, UIConstants.TEXT_DARK));
        form.add(Box.createVerticalStrut(4));
        form.add(nameField);
        form.add(Box.createVerticalStrut(10));
        form.add(UIConstants.makeLabel("Email", UIConstants.FONT_SMALL, UIConstants.TEXT_DARK));
        form.add(Box.createVerticalStrut(4));
        form.add(emailField);
        form.add(Box.createVerticalStrut(10));
        form.add(UIConstants.makeLabel("Password", UIConstants.FONT_SMALL, UIConstants.TEXT_DARK));
        form.add(Box.createVerticalStrut(4));
        form.add(passField);
        form.add(Box.createVerticalStrut(10));
        form.add(rolePanel);
        form.add(Box.createVerticalStrut(20));
        form.add(regBtn);
        form.add(Box.createVerticalStrut(10));
        form.add(switchRow);

        p.add(form);
        return p;
    }

    private void openDashboard() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            MainFrame mf = new MainFrame();
            mf.setVisible(true);
        });
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
