package poolwise;

import javax.swing.SwingUtilities;

/**
 * POOLWISE — Entry point.
 * Launches the GUI dashboard.
 */
public class PoolwiseApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
