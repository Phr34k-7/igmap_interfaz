package com.nmapscanner.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AllRecordsPanel extends JPanel {

    private JTable recordsTable;
    private DefaultTableModel tableModel;

    public AllRecordsPanel() {
        setLayout(new BorderLayout());
        
        String[] columnNames = {"id", "ip", "puertos", "fecha_registro"};
        tableModel = new DefaultTableModel(columnNames, 0);
        recordsTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(recordsTable);
        add(scrollPane, BorderLayout.CENTER);

        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        tableModel.setRowCount(0);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            String url = "jdbc:mysql://localhost:3306/proyecto";
            String user = "angel"; 
            String password = "angel";

            Connection connection = DriverManager.getConnection(url, user, password);

            String sql = "SELECT id, ip, puertos, fecha_registro FROM direccionesIP";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String ip = resultSet.getString("ip");
                String puertos = resultSet.getString("puertos");
                Timestamp fecha = resultSet.getTimestamp("fecha_registro");
                
                tableModel.addRow(new Object[]{id, ip, puertos, fecha});
            }

            resultSet.close();
            statement.close();
            connection.close();

        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error: Conector de MariaDB no encontrado.", "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al conectar o consultar la base de datos: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
