package com.nmapscanner.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TopologyPanel extends JPanel {
    private JTextArea topologyArea;

    public TopologyPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Topología"));

        topologyArea = new JTextArea();
        topologyArea.setEditable(false);
        topologyArea.setLineWrap(true);
        topologyArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(topologyArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void updateTopology(List<String> traceRouteHops) {
        if (traceRouteHops != null && !traceRouteHops.isEmpty()) {
            topologyArea.setText("Saltos de Ruta:\n" + String.join("\n", traceRouteHops));
        } else {
            topologyArea.setText("No se encontraron saltos de ruta. Asegúrate de usar la opción --traceroute en Nmap.");
        }
    }
}
