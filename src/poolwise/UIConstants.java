package poolwise;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class UIConstants {
    // ── Colour Palette ──────────────────────────────────────────
    public static final Color BG_DARK       = new Color(15,  23,  42);   // sidebar
    public static final Color BG_MEDIUM     = new Color(30,  41,  59);   // cards
    public static final Color BG_LIGHT      = new Color(248, 250, 252);  // main bg
    public static final Color ACCENT        = new Color(20,  184, 166);  // teal
    public static final Color ACCENT_DARK   = new Color(13,  148, 136);
    public static final Color ACCENT_LIGHT  = new Color(153, 246, 228);
    public static final Color TEXT_WHITE    = new Color(248, 250, 252);
    public static final Color TEXT_GRAY     = new Color(148, 163, 184);
    public static final Color TEXT_DARK     = new Color(15,  23,  42);
    public static final Color SUCCESS       = new Color(34,  197, 94);
    public static final Color WARNING       = new Color(251, 191, 36);
    public static final Color DANGER        = new Color(239, 68,  68);
    public static final Color CARD_WHITE    = Color.WHITE;
    public static final Color BORDER_COLOR  = new Color(226, 232, 240);

    // ── Fonts ────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  24);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font FONT_SUB     = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_LOGO    = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_STAT    = new Font("Segoe UI", Font.BOLD,  32);

    // ── Reusable component builders ──────────────────────────────

    public static JLabel makeLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    public static JButton makePrimaryButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(ACCENT_DARK);
                else if (getModel().isRollover()) g2.setColor(ACCENT_DARK);
                else g2.setColor(ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        b.setFont(FONT_SUB);
        b.setForeground(Color.WHITE);
        b.setPreferredSize(new Dimension(200, 42));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton makeOutlineButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(240,255,253) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(ACCENT);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                g2.setColor(ACCENT);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        b.setFont(FONT_SUB);
        b.setPreferredSize(new Dimension(200, 42));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JTextField makeTextField(String placeholder) {
        JTextField tf = new JTextField(placeholder);
        tf.setFont(FONT_BODY);
        tf.setForeground(Color.GRAY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        tf.setPreferredSize(new Dimension(280, 42));
        // placeholder behavior
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (tf.getText().equals(placeholder)) { tf.setText(""); tf.setForeground(TEXT_DARK); }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (tf.getText().isEmpty()) { tf.setText(placeholder); tf.setForeground(Color.GRAY); }
            }
        });
        return tf;
    }

    public static JPasswordField makePasswordField(String placeholder) {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        pf.setPreferredSize(new Dimension(280, 42));
        return pf;
    }

    /** Rounded white card panel */
    public static JPanel makeCard(int arcW, int arcH) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arcW, arcH);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arcW, arcH);
                g2.dispose();
                super.paintComponent(g);
            }
            { setOpaque(false); }
        };
    }

    /** Stat card with coloured top strip */
    public static JPanel makeStatCard(String value, String label, Color stripColor) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(stripColor);
                g2.fillRoundRect(0, 0, getWidth(), 6, 6, 6);
                g2.fillRect(0, 3, getWidth(), 6);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
            { setOpaque(false); }
        };
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel valLabel = makeLabel(value, FONT_STAT, TEXT_DARK);
        JLabel txtLabel = makeLabel(label, FONT_SMALL, TEXT_GRAY);

        JPanel inner = new JPanel(new GridLayout(2, 1, 0, 4));
        inner.setOpaque(false);
        inner.add(valLabel);
        inner.add(txtLabel);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    public static void setLook() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        UIManager.put("Button.arc", 10);
    }
}
