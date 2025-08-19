package com.nmapscanner.ui;

import javax.swing.*;
import java.awt.*;

public class NmapOutputPanel extends JPanel {

    private JTextArea resultsDisplayArea;
    private JComboBox<String> historyComboBox;

    public NmapOutputPanel() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel historyLabel = new JLabel("Historial de comandos: ");
        historyComboBox = new JComboBox<>();
        historyComboBox.addItem("Selecciona un comando del historial...");
        historyComboBox.setEditable(false);

        topPanel.add(historyLabel, BorderLayout.WEST);
        topPanel.add(historyComboBox, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        resultsDisplayArea = new JTextArea();
        resultsDisplayArea.setEditable(false);
        resultsDisplayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultsDisplayArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    public JTextArea getResultsDisplayArea() {
        return resultsDisplayArea;
    }

    public JComboBox<String> getHistoryComboBox() {
        return historyComboBox;
    }
}
