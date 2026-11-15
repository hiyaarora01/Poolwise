package com.poolwise.ui;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;

/** Central design token store for POOLWISE UI. Blue & white professional theme. */
public class Theme {
    // Colours
    public static final Color PRIMARY       = new Color(37, 99, 235);   // blue-600
    public static final Color PRIMARY_DARK  = new Color(29, 78, 216);   // blue-700
    public static final Color PRIMARY_LIGHT = new Color(219,234,254);   // blue-100
    public static final Color BG_PAGE      = new Color(241,245,249);    // slate-100
    public static final Color BG_SIDEBAR   = new Color(15, 23, 42);     // slate-900
    public static final Color BG_CARD      = Color.WHITE;
    public static final Color TEXT_MAIN    = new Color(15, 23, 42);
    public static final Color TEXT_SUB     = new Color(100,116,139);    // slate-500
    public static final Color TEXT_WHITE   = new Color(248,250,252);
    public static final Color SUCCESS      = new Color(22,163,74);
    public static final Color WARNING      = new Color(234,179,8);
    public static final Color DANGER       = new Color(220,38,38);
    public static final Color BORDER       = new Color(226,232,240);
    public static final Color ACCENT       = new Color(14,165,233);     // sky-500
    public static final Color ROW_ALT     = new Color(248,250,252);

    // Fonts
    public static final Font F_TITLE   = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font F_HEADING = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font F_SUB     = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font F_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font F_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font F_STAT    = new Font("Segoe UI", Font.BOLD,  28);
    public static final Font F_LOGO    = new Font("Segoe UI", Font.BOLD,  20);
    public static final Font F_TABLE   = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font F_TABLE_H = new Font("Segoe UI", Font.BOLD,  12);

    // ── Component builders ────────────────────────────────────────

    public static JLabel label(String text, Font f, Color c) {
        JLabel l = new JLabel(text); l.setFont(f); l.setForeground(c); return l;
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? PRIMARY_DARK : getModel().isRollover() ? PRIMARY_DARK : PRIMARY);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                g2.setColor(Color.WHITE); g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setFont(F_SUB); b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(160,38)); return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = primaryButton(text);
        b.setForeground(Color.WHITE);
        // override paint for red
        return new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(185,28,28) : DANGER);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                g2.setColor(Color.WHITE); g2.setFont(F_SUB);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
            { setFont(F_SUB); setContentAreaFilled(false); setBorderPainted(false);
              setFocusPainted(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
              setPreferredSize(new Dimension(140,36)); }
        };
    }

    public static JTextField field(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(F_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER,1,true),
            BorderFactory.createEmptyBorder(6,10,6,10)));
        return tf;
    }

    public static JComboBox<String> combo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(F_BODY); return cb;
    }

    /** White rounded card panel. */
    public static JPanel card(int arc) {
        return new JPanel() {
            { setOpaque(false); setLayout(new BorderLayout()); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),arc,arc));
                g2.setColor(BORDER); g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,arc,arc));
                g2.dispose(); super.paintComponent(g);
            }
        };
    }

    /** Stat card: coloured top bar + big number + label. */
    public static JPanel statCard(String value, String lbl, Color barColor) {
        JPanel card = new JPanel() {
            { setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),12,12));
                g2.setColor(barColor); g2.fillRect(0,0,getWidth(),5);
                g2.fillRoundRect(0,0,getWidth(),10,12,12);
                g2.setColor(BORDER); g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,12,12));
                g2.dispose(); super.paintComponent(g);
            }
        };
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        JLabel vLbl = label(value, F_STAT, TEXT_MAIN);
        JLabel tLbl = label(lbl,   F_SMALL, TEXT_SUB);
        JPanel inner = new JPanel(new GridLayout(2,1,0,4)); inner.setOpaque(false);
        inner.add(vLbl); inner.add(tLbl);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    public static void applyLook() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
    }
}
