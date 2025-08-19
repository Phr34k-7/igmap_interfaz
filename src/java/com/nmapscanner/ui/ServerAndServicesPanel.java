package com.nmapscanner.ui;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import com.nmapscanner.model.ScanResult;
import com.nmapscanner.model.Port;

import java.awt.*;

public class ServerAndServicesPanel extends JPanel {

    private JTree serversTree;
    private DefaultTreeModel treeModel;
    private JEditorPane detailsPane;

    public ServerAndServicesPanel() {
        setLayout(new BorderLayout());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Servidores y Servicios");
        treeModel = new DefaultTreeModel(root);
        serversTree = new JTree(treeModel);
        serversTree.getSelectionModel().setSelectionMode(javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);
        JScrollPane treeScrollPane = new JScrollPane(serversTree);
        treeScrollPane.setPreferredSize(new Dimension(250, 0));

        detailsPane = new JEditorPane();
        detailsPane.setContentType("text/html");
        detailsPane.setEditable(false);
        JScrollPane detailsScrollPane = new JScrollPane(detailsPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, detailsScrollPane);
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);

        serversTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) serversTree.getLastSelectedPathComponent();
                if (node == null) return;
                Object userObject = node.getUserObject();
                if (userObject instanceof Port) {
                    Port selectedPort = (Port) userObject;
                    displayPortDetails(selectedPort);
                } else {
                    detailsPane.setText("<html><body><b>Detalles del Servidor</b><p>Selecciona un puerto para ver sus detalles.</p></body></html>");
                }
            }
        });
    }

    public void updatePanelWithScanResult(ScanResult scanResult) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        root.removeAllChildren();
        
        DefaultMutableTreeNode targetNode = new DefaultMutableTreeNode(scanResult.getTarget());
        root.add(targetNode);
        
        for (Port port : scanResult.getOpenPorts()) {
            DefaultMutableTreeNode portNode = new DefaultMutableTreeNode(port);
            targetNode.add(portNode);
        }
        treeModel.reload();
        serversTree.expandPath(new javax.swing.tree.TreePath(targetNode.getPath()));
    }
    
    private void displayPortDetails(Port port) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h2>Detalles del Puerto: ").append(port.getPortNumber()).append("</h2>");
        sb.append("<table border='1' cellspacing='0' cellpadding='5'>");
        sb.append("<tr><td><b>Puerto</b></td><td>").append(port.getPortNumber()).append("</td></tr>");
        sb.append("<tr><td><b>Protocolo</b></td><td>").append(port.getProtocol()).append("</td></tr>");
        sb.append("<tr><td><b>Estado</b></td><td>").append(port.getState()).append("</td></tr>");
        sb.append("<tr><td><b>Servicio</b></td><td>").append(port.getService()).append("</td></tr>");
        sb.append("<tr><td><b>Versión</b></td><td>").append(port.getVersion()).append("</td></tr>");
        sb.append("</table>");
        sb.append("</body></html>");
        detailsPane.setText(sb.toString());
    }
}
