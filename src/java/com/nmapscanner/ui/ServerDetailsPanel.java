package com.nmapscanner.ui;

import com.nmapscanner.model.ScanResult;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ServerDetailsPanel extends JPanel {
    private JLabel targetLabel;
    private JLabel statusLabel;
    private JLabel ipLabel;
    private JTextArea hostnamesArea;
    private JLabel osLabel;
    private JLabel latencyLabel;
    private JLabel uptimeLabel; 
    private JLabel scanTimeLabel; // Ahora para la fecha/hora de inicio
    private JLabel scanDurationLabel; // Nuevo para la duración del escaneo
    private JLabel openPortsCountLabel;
    private JLabel filteredPortsCountLabel;
    private JLabel closedPortsCountLabel;

    public ServerDetailsPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Detalles del Servidor"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Target ---
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Objetivo:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        targetLabel = new JLabel("N/A");
        add(targetLabel, gbc);

        // --- Status ---
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        statusLabel = new JLabel("N/A");
        add(statusLabel, gbc);

        // --- IP Address ---
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("IP:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        ipLabel = new JLabel("N/A");
        add(ipLabel, gbc);

        // --- Hostnames ---
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Hostnames:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        hostnamesArea = new JTextArea(3, 20);
        hostnamesArea.setEditable(false);
        hostnamesArea.setLineWrap(true);
        hostnamesArea.setWrapStyleWord(true);
        JScrollPane hostnamesScrollPane = new JScrollPane(hostnamesArea);
        add(hostnamesScrollPane, gbc);
        gbc.weightx = 0; 

        // --- OS Guess ---
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("SO (Guess):"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        osLabel = new JLabel("N/A");
        add(osLabel, gbc);
        
        // --- Uptime ---
        gbc.gridx = 0; gbc.gridy = 5;
        add(new JLabel("Uptime:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        uptimeLabel = new JLabel("N/A");
        add(uptimeLabel, gbc);

        // --- Latency ---
        gbc.gridx = 0; gbc.gridy = 6;
        add(new JLabel("Latencia:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6;
        latencyLabel = new JLabel("N/A");
        add(latencyLabel, gbc);

        // --- Scan Time (Fecha/Hora de Inicio) ---
        gbc.gridx = 0; gbc.gridy = 7;
        add(new JLabel("Fecha/Hora Escaneo:"), gbc);
        gbc.gridx = 1; gbc.gridy = 7;
        scanTimeLabel = new JLabel("N/A");
        add(scanTimeLabel, gbc);

        // --- Scan Duration (Nueva) ---
        gbc.gridx = 0; gbc.gridy = 8;
        add(new JLabel("Duración Escaneo:"), gbc);
        gbc.gridx = 1; gbc.gridy = 8;
        scanDurationLabel = new JLabel("N/A");
        add(scanDurationLabel, gbc);

        // --- Port Counts ---
        gbc.gridx = 0; gbc.gridy = 9;
        add(new JLabel("Puertos Abiertos:"), gbc);
        gbc.gridx = 1; gbc.gridy = 9;
        openPortsCountLabel = new JLabel("N/A");
        add(openPortsCountLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 10;
        add(new JLabel("Puertos Filtrados:"), gbc);
        gbc.gridx = 1; gbc.gridy = 10;
        filteredPortsCountLabel = new JLabel("N/A");
        add(filteredPortsCountLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 11;
        add(new JLabel("Puertos Cerrados:"), gbc);
        gbc.gridx = 1; gbc.gridy = 11;
        closedPortsCountLabel = new JLabel("N/A");
        add(closedPortsCountLabel, gbc);
    }

    public void updateDetails(ScanResult result) {
        if (result == null) {
            clearDetails();
            return;
        }

        targetLabel.setText(result.getTarget() != null && !result.getTarget().isEmpty() ? result.getTarget() : "N/A");
        statusLabel.setText(result.getHostStatus() != null && !result.getHostStatus().isEmpty() ? result.getHostStatus() : "N/A");
        ipLabel.setText(result.getIpAddress() != null && !result.getIpAddress().isEmpty() ? result.getIpAddress() : "N/A");
        
        List<String> hostnames = result.getHostnames(); 
        if (hostnames != null && !hostnames.isEmpty()) {
            hostnamesArea.setText(String.join("\n", hostnames));
        } else {
            hostnamesArea.setText("N/A");
        }

        osLabel.setText(result.getOsGuess() != null && !result.getOsGuess().isEmpty() ? result.getOsGuess() : "N/A");
        uptimeLabel.setText(result.getUptime() != null && !result.getUptime().isEmpty() ? result.getUptime() : "N/A");
        latencyLabel.setText(result.getLatency() != null && !result.getLatency().isEmpty() ? result.getLatency() : "N/A");
        
        // Usar el getScanTime() que ya formatea el timestamp
        scanTimeLabel.setText(result.getScanTime());
        scanDurationLabel.setText(result.getScanDuration() != null && !result.getScanDuration().isEmpty() ? result.getScanDuration() : "N/A");

        Map<String, Integer> counts = result.getPortStateCounts(); 
        openPortsCountLabel.setText(counts.getOrDefault("open", 0).toString());
        filteredPortsCountLabel.setText(counts.getOrDefault("filtered", 0).toString());
        closedPortsCountLabel.setText(counts.getOrDefault("closed", 0) + counts.getOrDefault("unfiltered", 0) + ""); 
    }

    private void clearDetails() {
        targetLabel.setText("N/A");
        statusLabel.setText("N/A");
        ipLabel.setText("N/A");
        hostnamesArea.setText("N/A");
        osLabel.setText("N/A");
        uptimeLabel.setText("N/A");
        latencyLabel.setText("N/A");
        scanTimeLabel.setText("N/A");
        scanDurationLabel.setText("N/A");
        openPortsCountLabel.setText("N/A");
        filteredPortsCountLabel.setText("N/A");
        closedPortsCountLabel.setText("N/A");
    }
}
