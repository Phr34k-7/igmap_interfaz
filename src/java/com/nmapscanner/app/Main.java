package com.nmapscanner.app;

import com.nmapscanner.ui.MainWindow;
import com.nmapscanner.auth.Login; // <-- Agrega esta línea

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // CÓDIGO ORIGINAL: MainWindow frame = new MainWindow();
                // CÓDIGO ORIGINAL: frame.setVisible(true);

                // CAMBIO: Ahora abre la ventana de Login primero
                Login loginFrame = new Login(); // Crea una instancia de tu ventana de Login
                loginFrame.setVisible(true);    // La hace visible
            }
        });
    }
}
