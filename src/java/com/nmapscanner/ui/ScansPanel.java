package com.nmapscanner.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ScansPanel extends JPanel {
    private JTable scansTable;
    private DefaultTableModel tableModel;

    public ScansPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Historial de Escaneos"));

        String[] columnNames = {"Objetivo", "Comando"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Las celdas no son editables
            }
        };
        scansTable = new JTable(tableModel);
        scansTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scrollPane = new JScrollPane(scansTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addScan(String target, String command) {
        Object[] rowData = {target, command};
        tableModel.addRow(rowData);
    }

    public void clearScans() {
        tableModel.setRowCount(0);
    }
}
