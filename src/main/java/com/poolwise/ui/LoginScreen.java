package com.poolwise.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;

/** Login / Registration welcome screen. */
public class LoginScreen extends JFrame {

    private CardLayout cl = new CardLayout();
    private JPanel     cp = new JPanel(cl);

    public LoginScreen() {
        Theme.applyLook();
        setTitle("POOLWISE — Login");
        setSize(880, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildHero(), BorderLayout.WEST);
        cp.add(buildLoginPanel(),    "LOGIN");
        cp.add(buildRegisterPanel(), "REGISTER");
        root.add(cp, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Hero (left panel) ─────────────────────────────────────────
    private JPanel buildHero() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                GradientPaint gp = new GradientPaint(0,0,Theme.BG_SIDEBAR,0,getHeight(),new Color(30,64,175));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(37,99,235,40)); g2.fillOval(-30,-30,180,180);
                g2.setColor(new Color(37,99,235,25)); g2.fillOval(100,320,220,220);
                g2.dispose(); super.paintComponent(g);
            }
            { setOpaque(false); }
        };
        p.setPreferredSize(new Dimension(320,560));
        p.setLayout(new GridBagLayout());
        JPanel inner = new JPanel(); inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner,BoxLayout.Y_AXIS));
        inner.setBorder(BorderFactory.createEmptyBorder(0,28,0,28));

        JLabel logo = Theme.label("🚗 POOLWISE", Theme.F_LOGO, Theme.PRIMARY_LIGHT);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel tag  = Theme.label("Smart Carpool Optimization", Theme.F_BODY, Theme.TEXT_SUB);
        tag.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(logo); inner.add(Box.createVerticalStrut(6)); inner.add(tag);
        inner.add(Box.createVerticalStrut(36));

        String[][] feats = {{"🎯","Intelligent Route Matching"},{"⛽","Fuel & CO₂ Savings"},
            {"⚡","5 Optimization Modes"},{"📊","Real-time Analytics"},{"🔄","Dynamic Re-routing"}};
        for (String[] f : feats) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT,8,2));
            row.setOpaque(false); row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(280,28));
            row.add(Theme.label(f[0],Theme.F_BODY,Theme.TEXT_WHITE));
            row.add(Theme.label(f[1],Theme.F_BODY,Theme.TEXT_SUB));
            inner.add(row);
        }
        p.add(inner); return p;
    }

    // ── Login panel ───────────────────────────────────────────────
    private JPanel buildLoginPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.BG_PAGE);
        JPanel form = new JPanel(); form.setOpaque(false);
        form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));

        addTitle(form,"Welcome back!","Sign in to your account");
        form.add(Box.createVerticalStrut(24));

        JTextField  emailF = addFormField(form,"Email / User ID","");
        JPasswordField passF = new JPasswordField(16);
        passF.setFont(Theme.F_BODY);
        passF.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Theme.BORDER,1,true),
            BorderFactory.createEmptyBorder(6,10,6,10)));
        addLabel(form,"Password");
        passF.setMaximumSize(new Dimension(300,36)); passF.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(passF); form.add(Box.createVerticalStrut(20));

        JButton loginBtn = Theme.primaryButton("Sign In");
        loginBtn.setMaximumSize(new Dimension(300,40)); loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> openMain());
        form.add(loginBtn); form.add(Box.createVerticalStrut(10));

        JButton guestBtn = new JButton("Continue as Guest");
        guestBtn.setFont(Theme.F_BODY); guestBtn.setForeground(Theme.PRIMARY);
        guestBtn.setContentAreaFilled(false); guestBtn.setBorderPainted(false);
        guestBtn.setFocusPainted(false); guestBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        guestBtn.setMaximumSize(new Dimension(300,36)); guestBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        guestBtn.addActionListener(e -> openMain());
        form.add(guestBtn); form.add(Box.createVerticalStrut(14));

        addSwitch(form,"Don't have an account? ","Register →","REGISTER");
        p.add(form); return p;
    }

    // ── Register panel ────────────────────────────────────────────
    private JPanel buildRegisterPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.BG_PAGE);
        JPanel form = new JPanel(); form.setOpaque(false);
        form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));

        addTitle(form,"Create Account","Join POOLWISE today");
        form.add(Box.createVerticalStrut(20));

        addFormField(form,"Full Name","");
        addFormField(form,"Phone Number (10 digits)","");
        addFormField(form,"Email Address","");

        addLabel(form,"Register as:");
        JPanel roleRow = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        roleRow.setOpaque(false); roleRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        JRadioButton dRb = new JRadioButton("Driver");
        JRadioButton pRb = new JRadioButton("Passenger",true);
        dRb.setFont(Theme.F_BODY); dRb.setOpaque(false);
        pRb.setFont(Theme.F_BODY); pRb.setOpaque(false);
        ButtonGroup bg = new ButtonGroup(); bg.add(dRb); bg.add(pRb);
        roleRow.add(dRb); roleRow.add(pRb);
        form.add(roleRow); form.add(Box.createVerticalStrut(16));

        JButton regBtn = Theme.primaryButton("Create Account");
        regBtn.setMaximumSize(new Dimension(300,40)); regBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        regBtn.addActionListener(e -> openMain());
        form.add(regBtn); form.add(Box.createVerticalStrut(14));
        addSwitch(form,"Already have an account? ","Sign in →","LOGIN");

        p.add(form); return p;
    }

    // ── Helpers ───────────────────────────────────────────────────
    private void addTitle(JPanel p, String title, String sub) {
        JLabel t = Theme.label(title,Theme.F_TITLE,Theme.TEXT_MAIN); t.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel s = Theme.label(sub,  Theme.F_BODY, Theme.TEXT_SUB);  s.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(t); p.add(Box.createVerticalStrut(4)); p.add(s);
    }
    private void addLabel(JPanel p, String text) {
        JLabel l = Theme.label(text,Theme.F_SMALL,Theme.TEXT_MAIN);
        l.setAlignmentX(Component.CENTER_ALIGNMENT); p.add(l); p.add(Box.createVerticalStrut(3));
    }
    private JTextField addFormField(JPanel p, String label, String val) {
        addLabel(p, label);
        JTextField tf = Theme.field(16); if(!val.isBlank()) tf.setText(val);
        tf.setMaximumSize(new Dimension(300,36)); tf.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(tf); p.add(Box.createVerticalStrut(10)); return tf;
    }
    private void addSwitch(JPanel form, String msg, String linkText, String target) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER,2,0)); row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.add(Theme.label(msg,Theme.F_SMALL,Theme.TEXT_SUB));
        JButton btn = new JButton(linkText); btn.setFont(Theme.F_SMALL); btn.setForeground(Theme.PRIMARY);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> cl.show(cp,target)); row.add(btn); form.add(row);
    }
    private void openMain() {
        dispose();
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}
