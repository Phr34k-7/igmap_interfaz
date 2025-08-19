package com.nmapscanner.ui;

import com.nmapscanner.core.NmapExecutor;
import com.nmapscanner.core.NmapOutputParser;
import com.nmapscanner.model.ScanResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public class ScanInputPanel extends JPanel {
    private JTextField targetField;
    private JTextField commandField; // Ahora solo "Comando"
    private JButton startScanButton;
    private JButton stopScanButton;
    // private JCheckBox sudoCheckBox; // ¡QUITADO!

    private JTextArea resultsDisplay; // Referencia al JTextArea en NmapOutputPanel
    private JLabel statusBar; // Referencia a la barra de estado en MainWindow
    private Consumer<ScanResult> scanResultConsumer; // Para enviar el ScanResult a MainWindow

    // ComboBox para perfiles de escaneo
    private JComboBox<String> profileComboBox;

    // Referencia al proceso de Nmap para poder detenerlo
    private Process currentNmapProcess;


    public ScanInputPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 1: Objetivo
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Objetivo (IP/Dominio):"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        targetField = new JTextField("scanme.nmap.org", 25);
        add(targetField, gbc);

        // ComboBox de Perfil de escaneo
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        add(new JLabel("Perfil:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0.5; // Darle algo de peso para que se expanda
        profileComboBox = new JComboBox<>(new String[] {
            "Regular scan",
            "Intense scan",
            "Intense scan plus UDP",
            "Intense scan, all TCP ports",
            "Intense scan, no ping",
            "Ping scan",
            "Quick scan",
            "Quick scan plus"
        });
        profileComboBox.setSelectedIndex(0); // Seleccionar el primero por defecto
        add(profileComboBox, gbc);


        // Fila 2: Comando (¡TEXTO CAMBIADO!)
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Comando:"), gbc); // ¡Solo "Comando"!

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3; // Ocupa el resto de las columnas para el campo de comando
        commandField = new JTextField("-sV -O", 30);
        add(commandField, gbc);

        // ¡EL CHECKBOX SUDO HA SIDO ELIMINADO DE AQUÍ!
        // Si necesitas que se use sudo, debe ir en el comando directamente.

        // Fila 3: Botones
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1; // Restablecer a 1 columna
        gbc.anchor = GridBagConstraints.EAST;
        startScanButton = new JButton("Iniciar Escaneo");
        add(startScanButton, gbc);

        gbc.gridx = 2; // Mueve el botón Detener al lado de Iniciar
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        stopScanButton = new JButton("Detener Escaneo");
        stopScanButton.setEnabled(false); // Deshabilitado hasta que un escaneo esté en curso
        add(stopScanButton, gbc);

        // Listeners
        startScanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startScan();
            }
        });

        stopScanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopScan();
            }
        });

        profileComboBox.addActionListener(e -> {
            updateCommandFieldFromProfile();
        });

        // Inicializar el campo de comando al cargar el panel
        updateCommandFieldFromProfile();
    }

    private void updateCommandFieldFromProfile() {
        String selectedProfile = (String) profileComboBox.getSelectedItem();
        String command = "";
        switch (selectedProfile) {
            case "Regular scan":
                command = "-sS -sV -O";
                break;
            case "Intense scan":
                command = "-T4 -A -v";
                break;
            case "Intense scan plus UDP":
                command = "-sS -sU -T4 -A -v"; // UDP requiere -sU
                break;
            case "Intense scan, all TCP ports":
                command = "-p 1-65535 -T4 -A -v";
                break;
            case "Intense scan, no ping":
                command = "-T4 -A -v -Pn";
                break;
            case "Ping scan":
                command = "-sn";
                break;
            case "Quick scan":
                command = "-F"; // Fast scan
                break;
            case "Quick scan plus":
                command = "-sV -O -F"; // Fast scan with version and OS detection
                break;
            default:
                command = "";
                break;
        }
        commandField.setText(command);
    }

    private void startScan() {
        String target = targetField.getText().trim();
        String command = commandField.getText().trim();
        boolean useSudo = command.startsWith("sudo "); // Determina si usar sudo basado en el comando

        if (target.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, introduce un objetivo (IP o Dominio).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        resultsDisplay.setText("Iniciando escaneo para " + target + " con Nmap " + command + "...\n");
        if (statusBar != null) {
            statusBar.setText("Estado: Ejecutando Nmap en " + target + "...");
        }

        startScanButton.setEnabled(false);
        stopScanButton.setEnabled(true);

        // Ejecutar Nmap en un hilo separado para no bloquear la GUI
        new SwingWorker<ScanResult, String>() {
            private NmapExecutor executor; // Instancia de NmapExecutor para este Worker
            private String fullCommandUsed;

            @Override
            protected ScanResult doInBackground() throws Exception {
                // Instancia de NmapExecutor con los consumidores para la salida
                executor = new NmapExecutor(
                    line -> publish(line), // Consumer para líneas de salida
                    errorLine -> { // Consumer para mensajes de error
                        publish("ERROR: " + errorLine);
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ScanInputPanel.this, 
                            "Error en NmapExecutor: " + errorLine + 
                            "\nAsegúrate de que Nmap está instalado y en tu PATH.", 
                            "Error de Nmap", JOptionPane.ERROR_MESSAGE));
                    },
                    null // ScanInputPanel no necesita un ScanResultConsumer para NmapExecutor
                );
                
                fullCommandUsed = (useSudo ? "sudo " : "") + "nmap " + command + " " + target; 
                
                executor.executeNmapCommand(target, command, useSudo);

                String fullOutput = executor.getFullOutput();
                NmapOutputParser parser = new NmapOutputParser();
                ScanResult result = parser.parseOutput(fullOutput);
                result.setRawOutput(fullOutput); // Almacenar la salida bruta
                result.setCommandUsed(fullCommandUsed); // Guardar el comando usado
                return result;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String line : chunks) {
                    resultsDisplay.append(line + "\n");
                    resultsDisplay.setCaretPosition(resultsDisplay.getDocument().getLength()); // Auto-scroll
                }
            }

            @Override
            protected void done() {
                startScanButton.setEnabled(true);
                stopScanButton.setEnabled(false);
                try {
                    ScanResult result = get(); // Obtiene el resultado del escaneo
                    if (scanResultConsumer != null) {
                        scanResultConsumer.accept(result); // Envía el resultado a MainWindow
                    }
                    if (statusBar != null) {
                        statusBar.setText("Estado: Escaneo de " + result.getTarget() + " completado.");
                    }
                } catch (InterruptedException ex) {
                    resultsDisplay.append("\nEscaneo interrumpido.\n");
                    if (statusBar != null) {
                        statusBar.setText("Estado: Escaneo interrumpido.");
                    }
                } catch (java.util.concurrent.ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    resultsDisplay.append("\nError durante el escaneo: " + cause.getMessage() + "\n");
                    if (statusBar != null) {
                        statusBar.setText("Estado: Error durante el escaneo.");
                    }
                    cause.printStackTrace();
                    JOptionPane.showMessageDialog(ScanInputPanel.this, 
                        "Error al ejecutar Nmap: " + cause.getMessage() + 
                        "\nAsegúrate de que Nmap está instalado y en tu PATH.", 
                        "Error de Nmap", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void stopScan() {
        // La detención del proceso debe ser manejada por el SwingWorker
        // ya que la instancia de NmapExecutor está dentro de él.
        // Si necesitamos una detención externa, el SwingWorker necesitaría
        // un método público para cancelar su ejecución o un campo para 'currentNmapProcess'.
        // Por ahora, esta es una limitación. Para detener un proceso externo:
        if (currentNmapProcess != null && currentNmapProcess.isAlive()) {
             currentNmapProcess.destroyForcibly(); // Intenta detener el proceso de Nmap
             resultsDisplay.append("\nIntentando detener el escaneo...\n");
        } else {
            resultsDisplay.append("\nNo hay escaneo activo para detener.\n");
        }
        startScanButton.setEnabled(true);
        stopScanButton.setEnabled(false);
        if (statusBar != null) {
            statusBar.setText("Estado: Deteniendo escaneo...");
        }
    }

    // Métodos para que MainWindow pueda inyectar referencias
    public void setResultsDisplay(JTextArea resultsDisplay) {
        this.resultsDisplay = resultsDisplay;
    }

    public void setStatusBar(JLabel statusBar) {
        this.statusBar = statusBar;
    }

    public void setScanResultConsumer(Consumer<ScanResult> consumer) {
        this.scanResultConsumer = consumer;
    }
    
    // Método para obtener el campo de comando (usado por ScansPanel si lo necesita)
    public JTextField getCommandField() {
        return commandField;
    }
}
