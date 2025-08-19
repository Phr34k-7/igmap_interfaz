package com.nmapscanner.ui;

import com.nmapscanner.model.Port;
import com.nmapscanner.model.ScanResult;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;


public class PortsAndServersPanel extends JPanel { 

    private JTree serversTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;

    public PortsAndServersPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Historial de Escaneos / Servicios")); 
        rootNode = new DefaultMutableTreeNode("Historial de Escaneos"); 
        treeModel = new DefaultTreeModel(rootNode);
        serversTree = new JTree(treeModel);
        serversTree.setCellRenderer(new CustomTreeCellRenderer());
        serversTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(serversTree);
        add(scrollPane, BorderLayout.CENTER);
    }

    public JTree getServersTree() { 
        return serversTree;
    }

    public void setTreeSelectionListener(TreeSelectionListener listener) {
        if (serversTree != null) { 
            serversTree.addTreeSelectionListener(listener);
        }
    }

    // Este método se llamará desde MainWindow cuando un nuevo escaneo sea completado o cargado
    public void addScanResult(ScanResult result) {
        if (result == null || result.getTarget() == null || result.getTarget().isEmpty()) {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm:ss");
        // El ScanResult es el UserObject del nodo principal del escaneo
        DefaultMutableTreeNode scanNode = new DefaultMutableTreeNode(result); 
        
        // Agrega el nodo de escaneo al nodo raíz
        rootNode.insert(scanNode, 0); // Agrega al principio para que los más nuevos estén arriba
        
        // Poblar el nodo del escaneo con servicios y puertos
        populateScanNode(scanNode, result);

        treeModel.reload(rootNode); 
        
        // Expandir el nuevo nodo de escaneo automáticamente
        serversTree.expandPath(serversTree.getPathForRow(0));
        
        // Seleccionar el nuevo nodo automáticamente (opcional, MainWindow puede manejar esto)
        serversTree.setSelectionRow(0); 
    }

    // Método para poblar un nodo de escaneo con sus servicios y puertos
    private void populateScanNode(DefaultMutableTreeNode scanNode, ScanResult result) {
        // Agrupar puertos por servicio
        Map<String, List<Port>> servicesMap = new HashMap<>();
        if (result.getOpenPorts() != null) {
            for (Port port : result.getOpenPorts()) {
                // Usar toLowerCase() para agrupar servicios con mayúsculas/minúsculas inconsistentes
                servicesMap.computeIfAbsent(port.getService().toLowerCase(), k -> new ArrayList<>()).add(port);
            }
        }

        // Añadir los servicios como nodos hijos del scanNode
        List<String> sortedServices = new ArrayList<>(servicesMap.keySet());
        sortedServices.sort(String.CASE_INSENSITIVE_ORDER); 

        for (String serviceName : sortedServices) {
            DefaultMutableTreeNode serviceNode = new DefaultMutableTreeNode("Servicio: " + serviceName);
            scanNode.add(serviceNode);

            List<Port> portsForService = servicesMap.get(serviceName);
            portsForService.sort(Comparator.comparingInt(Port::getPortNumber)); 

            for (Port port : portsForService) {
                String portInfo = "Puerto: " + port.getPortNumber() + "/" + port.getProtocol() + " (" + port.getState() + ")";
                if (port.getVersion() != null && !port.getVersion().isEmpty()) {
                    portInfo += " (" + port.getVersion() + ")";
                }
                DefaultMutableTreeNode portNode = new DefaultMutableTreeNode(portInfo);
                serviceNode.add(portNode);
            }
        }

        // Caso especial: no se encontraron puertos, o host inactivo
        if (result.getOpenPorts() == null || result.getOpenPorts().isEmpty()) {
            if (result.getHostStatus() != null && result.getHostStatus().toLowerCase().contains("down")) {
                scanNode.add(new DefaultMutableTreeNode("Host inactivo."));
            } else if (result.getRawOutput() != null && result.getRawOutput().contains("Host seems down")) {
                scanNode.add(new DefaultMutableTreeNode("Host inactivo."));
            } else {
                scanNode.add(new DefaultMutableTreeNode("No se encontraron puertos abiertos."));
            }
        }

        treeModel.reload(scanNode); 
    }

    // Renderizador personalizado para el árbol
    private class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            
            // Si el Look and Feel es Nimbus, aplicamos nuestros colores custom
            if (UIManager.getLookAndFeel().getName().equals("Nimbus")) {
                if (sel) {
                    c.setBackground(UIManager.getColor("Tree.selectionBackground")); 
                    c.setForeground(UIManager.getColor("Tree.selectionForeground")); 
                } else {
                    c.setBackground(UIManager.getColor("Tree.background")); 
                    c.setForeground(UIManager.getColor("Tree.foreground")); 
                }
            }
            ((JComponent) c).setOpaque(true); 

            // Mejorar la visualización de los nodos
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObj = node.getUserObject();

                if (userObj.equals("Historial de Escaneos")) { 
                    setIcon(UIManager.getIcon("Tree.closedIcon")); 
                    setText("Historial de Escaneos"); 
                } else if (userObj instanceof ScanResult) {
                    ScanResult result = (ScanResult) userObj;
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm:ss");
                    setText(result.getTarget() + " [" + sdf.format(result.getTimestamp() != null ? result.getTimestamp() : new Date()) + "]");
                    setIcon(UIManager.getIcon("FileView.computerIcon")); 
                } else if (userObj.toString().startsWith("Servicio:")) {
                    setIcon(UIManager.getIcon("FileView.directoryIcon")); 
                } else if (userObj.toString().startsWith("Puerto:")) {
                    setIcon(UIManager.getIcon("FileView.fileIcon")); 
                } else if (userObj.toString().equals("Host inactivo.")) {
                    setIcon(UIManager.getIcon("OptionPane.warningIcon")); // Icono de advertencia
                } else if (userObj.toString().equals("No se encontraron puertos abiertos.")) {
                    setIcon(UIManager.getIcon("OptionPane.informationIcon")); // Icono de información
                }
            }
            return c;
        }
    }
}
