package com.nmapscanner.auth;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.nmapscanner.ui.MainWindow; 

public class Login extends JFrame {

    private JPanel mainPanel;
    private JPanel loginPanel;
    private JLabel igmapTitleLabel;
    private JLabel descriptionLabel1;
    private JLabel descriptionLabel2;
    private JLabel projectDescriptionLabel;
    private JLabel loginTitleLabel;
    private JLabel emailLabel;
    private JTextField emailField;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JButton loginButton;

    public Login() {
        setTitle("IGMAP - Iniciar Sesión");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // --- CÓDIGO PARA AGREGAR EL ÍCONO DE LA VENTANA ---
        try {
            // Usa una ruta absoluta a tu archivo.
            // Asegúrate de que esta ruta sea la correcta en tu sistema.
            // Ejemplo: /home/angel/Descargas/BSC/Igmap/src/java/com/nmapscanner/auth/Img/IGMAP.png
            Image icon = new ImageIcon("/home/angel/Descargas/BSC/Igmap/src/java/com/nmapscanner/auth/Img/IGMAP.png").getImage();
            setIconImage(icon);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el ícono de la ventana.");
            e.printStackTrace();
        }
        // --- FIN DEL CÓDIGO PARA AGREGAR EL ÍCONO ---

        JPanel contentPane = new JPanel();
        contentPane.setLayout(null);
        setContentPane(contentPane);

        // --- Panel Izquierdo (gris oscuro con texto IGMAP) ---
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(50, 50, 50));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(null);
        mainPanel.setBounds(0, 0, getWidth() / 2, getHeight());
        contentPane.add(mainPanel);

        // --- Componentes del lado izquierdo (Texto IGMAP) ---
        igmapTitleLabel = new JLabel("IGMAP");
        igmapTitleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        igmapTitleLabel.setForeground(Color.WHITE);
        igmapTitleLabel.setBounds(130, 50, 140, 50);
        mainPanel.add(igmapTitleLabel);

        descriptionLabel1 = new JLabel("<html><p style=\"text-align: center;\">Interfaz Grafica Para El Escaner De Puertos</p></html>");
        descriptionLabel1.setFont(new Font("SansSerif", Font.PLAIN, 16));
        descriptionLabel1.setForeground(Color.LIGHT_GRAY);
        descriptionLabel1.setBounds(40, 120, 320, 40);
        mainPanel.add(descriptionLabel1);

        descriptionLabel2 = new JLabel("<html><p style=\"text-align: center;\">Análisis De Seguridad Y Descubrimiento De Redes</p></html>");
        descriptionLabel2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        descriptionLabel2.setForeground(Color.LIGHT_GRAY);
        descriptionLabel2.setBounds(30, 160, 340, 40);
        mainPanel.add(descriptionLabel2);

        projectDescriptionLabel = new JLabel("<html><p style=\"text-align: center;\">IGMAP es una herramienta de escaneo de puertos<br>basada en Nmap, diseñada para análisis de seguridad y descubrimiento de redes.</p></html>");
        projectDescriptionLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        projectDescriptionLabel.setForeground(new Color(170, 170, 170));
        projectDescriptionLabel.setBounds(25, 350, 350, 60);
        mainPanel.add(projectDescriptionLabel);

        // --- Panel de Login (lado derecho, blanco) ---
        loginPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        loginPanel.setLayout(null);
        loginPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        loginPanel.setBounds(getWidth() / 2, 0, getWidth() / 2, getHeight());
        contentPane.add(loginPanel);

        // **** Componentes del panel de login (dentro de loginPanel) ****
        // *** Título "Iniciar Sesión" ***
        loginTitleLabel = new JLabel("Iniciar Sesión", SwingConstants.CENTER);
        loginTitleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        loginTitleLabel.setForeground(Color.DARK_GRAY);
        loginTitleLabel.setBounds(0, 60, loginPanel.getWidth(), 30);
        loginPanel.add(loginTitleLabel);
        // *** Fin Título "Iniciar Sesión" ***

        int fieldWidth = 250;
        int fieldX = (loginPanel.getWidth() - fieldWidth) / 2;

        // Ajustamos la Y de los campos para dar espacio al nuevo título
        emailLabel = new JLabel("Correo Electrónico");
        emailLabel.setBounds(fieldX, 130, fieldWidth, 20);
        loginPanel.add(emailLabel);

        emailField = new JTextField(20);
        emailField.setBounds(fieldX, 155, fieldWidth, 30);
        loginPanel.add(emailField);

        passwordLabel = new JLabel("Contraseña");
        passwordLabel.setBounds(fieldX, 200, fieldWidth, 20);
        loginPanel.add(passwordLabel);

        passwordField = new JPasswordField(20);
        passwordField.setBounds(fieldX, 225, fieldWidth, 30);
        loginPanel.add(passwordField);

        int buttonWidth = 150;
        int buttonX = (loginPanel.getWidth() - buttonWidth) / 2;
        loginButton = new JButton("Iniciar Sesión");
        loginButton.setBackground(new Color(59, 89, 182));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBounds(buttonX, 280, buttonWidth, 40);
        loginPanel.add(loginButton);
        // **** FIN Componentes del panel de login ****

        // Listener para el botón de Iniciar Sesión
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());

                if (email.equals("gabrielsancz@gmail.com") && password.equals("contraseña123")) {
                    JOptionPane.showMessageDialog(Login.this, "¡Inicio de sesión exitoso!");

                    MainWindow nmapGUI = new MainWindow();
                    nmapGUI.setVisible(true);

                    dispose(); // Cierra la ventana de login actual
                } else {
                    JOptionPane.showMessageDialog(Login.this, "Correo o contraseña incorrectos", "Error de Inicio de Sesión", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Login().setVisible(true);
            }
        });
    }
}
