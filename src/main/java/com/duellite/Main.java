package com.duellite;

import com.duellite.ui.DuelFrame;

import javax.swing.SwingUtilities;


public class Main {
    public static void main(String[] args) {
        // Toda la interfaz Swing se crea en el Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> new DuelFrame().setVisible(true));
    }
}
