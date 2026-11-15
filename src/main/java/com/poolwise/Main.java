package com.poolwise;

import com.poolwise.ui.LoginScreen;
import javax.swing.SwingUtilities;

/**
 * POOLWISE — Entry point.
 * SwingUtilities.invokeLater ensures the GUI is created on the Event Dispatch Thread (EDT).
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginScreen::launch);
    }
}
