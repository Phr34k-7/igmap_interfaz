package com.nmapscanner.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter; // Importación necesaria para WindowAdapter
import java.awt.event.WindowEvent; // Importación necesaria para WindowEvent
import java.sql.Connection; // Importación para base de datos
import java.sql.DriverManager; // Importación para base de datos
import java.sql.PreparedStatement; // Importación para base de datos
import java.sql.ResultSet; // Importación para base de datos
import java.sql.SQLException; // Importación para base de datos
import java.util.prefs.Preferences; // Importación para preferencias

import com.nmapscanner.model.ScanResult;
import com.nmapscanner.model.Port;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.nmapscanner.core.NmapOutputParser;
import com.nmapscanner.core.PdfReportGenerator;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

// NUEVAS IMPORTACIONES
import com.nmapscanner.ui.HelpStudyPanel;
import com.nmapscanner.ui.AllRecordsPanel; // <-- ¡NUEVA IMPORTACIÓN!
import com.nmapscanner.core.NmapExecutor; // Asegúrate de que esta importación exista si NmapExecutor se usa aquí

public class MainWindow extends JFrame {

    private ScanInputPanel scanInputPanel;
    private JTabbedPane contentTabbedPane;
    private JLabel statusBar;

    private NmapOutputPanel nmapOutputPanel;
    private ServerAndServicesPanel serverAndServicesPanel;
    private TopologyPanel topologyPanel;
    private ServerDetailsPanel serverDetailsPanel;
    private ScansPanel scansPanel;

    private JPanel leftButtonsPanel;
    private JButton viewHistoryButton;
    private JButton viewLastScanButton;
    private JButton viewAllRecordsButton;
    private JButton filterServersButton;

    private JPanel mainContentCardPanel;
    private CardLayout mainCardLayout;

    private List<ScanResult> allScanResults = new ArrayList<>();
    private ScanResult currentScanResult;

    private boolean isDarkModeActive = false;

    public MainWindow() {
        setTitle("IGMAP - Nmap GUI Scanner");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- CÓDIGO PARA AGREGAR EL ÍCONO DE LA VENTANA ---
        try {
            // Asegúrate de que esta ruta sea la correcta en tu sistema.
            // Ejemplo: "/home/angel/Descargas/BSC/Igmap/src/java/com/nmapscanner/auth/Img/IGMAP.png"
            Image icon = new ImageIcon("/home/angel/Descargas/BSC/Igmap/src/java/com/nmapscanner/auth/Img/IGMAP.png").getImage();
            setIconImage(icon);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el ícono de la ventana principal.");
            e.printStackTrace();
        }
        // --- FIN DEL CÓDIGO PARA AGREGAR EL ÍCONO ---

        JMenuBar menuBar = new JMenuBar();
        JMenu scanMenu = new JMenu("Escaneo");
        JMenuItem newWindowMenuItem = new JMenuItem("Nueva ventana");
        newWindowMenuItem.addActionListener(e -> new MainWindow().setVisible(true));
        
        JMenuItem openScanMenuItem = new JMenuItem("Abrir escaneo (Archivo de texto)");
        openScanMenuItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Abrir Escaneo Nmap (Texto)");
            int userSelection = fileChooser.showOpenDialog(MainWindow.this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToOpen = fileChooser.getSelectedFile();
                try {
                    String fileContent = new String(Files.readAllBytes(fileToOpen.toPath()));
                    NmapOutputParser parser = new NmapOutputParser();
                    ScanResult loadedResult = parser.parseOutput(fileContent);
                    if (loadedResult.getTarget() == null || loadedResult.getTarget().isEmpty() || loadedResult.getTarget().equals("N/A")) {
                        loadedResult.setTarget(fileToOpen.getName());
                        if (fileToOpen.getName().matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*")) {
                            loadedResult.setIpAddress(fileToOpen.getName().split("\\.")[0] + "." +
                                    loadedResult.getTarget().split("\\.")[1] + "." +
                                    loadedResult.getTarget().split("\\.")[2] + "." +
                                    loadedResult.getTarget().split("\\.")[3].split("_")[0]);
                        }
                    }
                    loadedResult.setRawOutput(fileContent);
                    handleNewScanResult(loadedResult);
                    statusBar.setText("Estado: Escaneo '" + fileToOpen.getName() + "' cargado exitosamente.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "Error al leer el archivo: " + ex.getMessage(),
                            "Error de Archivo", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "Error al parsear el contenido del archivo.\n" +
                                    "Asegúrate de que es una salida de Nmap válida.\n" + ex.getMessage(),
                            "Error de Parseo", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
        
        JMenuItem saveScanMenuItem = new JMenuItem("Guardar escaneo actual (PDF)");
        saveScanMenuItem.addActionListener(e -> {
            if (currentScanResult == null || currentScanResult.getTarget() == null || currentScanResult.getTarget().isEmpty()) {
                JOptionPane.showMessageDialog(MainWindow.this,
                        "No hay un escaneo actual para guardar o el objetivo está vacío.",
                        "Guardar Escaneo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar Reporte Nmap (PDF)");
            String defaultFileName = "nmap_report.pdf";
            if (currentScanResult.getTarget() != null && !currentScanResult.getTarget().isEmpty() && !currentScanResult.getTarget().equals("N/A")) {
                String cleanTarget = currentScanResult.getTarget().replaceAll("[^a-zA-Z0-9.-]", "_");
                defaultFileName = cleanTarget + "_nmap_report.pdf";
            }
            fileChooser.setSelectedFile(new File(defaultFileName));
            int userSelection = fileChooser.showSaveDialog(MainWindow.this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String filePath = fileToSave.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".pdf")) {
                    filePath += ".pdf";
                    fileToSave = new File(filePath);
                }
                try {
                    PdfReportGenerator generator = new PdfReportGenerator();
                    generator.generateScanReport(currentScanResult, filePath);
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "Reporte PDF guardado exitosamente en:\n" + fileToSave.getAbsolutePath(),
                            "Guardar Reporte", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "Error al guardar el reporte PDF: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "Error inesperado al generar el PDF: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
        
        JMenuItem saveAllScansMenuItem = new JMenuItem("Guardar todos los escaneos activos (texto y PDF)");
        saveAllScansMenuItem.addActionListener(e -> {
            if (allScanResults.isEmpty()) {
                JOptionPane.showMessageDialog(MainWindow.this,
                        "No hay escaneos para guardar.",
                        "Guardar Todos los Escaneos", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Seleccionar Directorio para Guardar Todos los Escaneos");
            int userSelection = fileChooser.showSaveDialog(MainWindow.this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File directoryToSave = fileChooser.getSelectedFile();
                int savedCount = 0;
                int pdfCount = 0;
                for (ScanResult sr : allScanResults) {
                    String cleanTarget = (sr.getTarget() != null && !sr.getTarget().isEmpty() && !sr.getTarget().equals("N/A")) ?
                            sr.getTarget().replaceAll("[^a-zA-Z0-9.-]", "_") : "unknown_target";
                    String baseFileName = cleanTarget + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(sr.getTimestamp() != null ? sr.getTimestamp() : new Date());
                    File txtFile = new File(directoryToSave, baseFileName + ".txt");
                    try (FileWriter writer = new FileWriter(txtFile)) {
                        writer.write(sr.getRawOutput());
                        savedCount++;
                    } catch (IOException ex) {
                        System.err.println("Error saving TXT for " + sr.getTarget() + ": " + ex.getMessage());
                    }
                    File pdfFile = new File(directoryToSave, baseFileName + ".pdf");
                    try {
                        PdfReportGenerator generator = new PdfReportGenerator();
                        generator.generateScanReport(sr, pdfFile.getAbsolutePath());
                        pdfCount++;
                    } catch (IOException ex) {
                        System.err.println("Error saving PDF for " + sr.getTarget() + ": " + ex.getMessage());
                    }
                }
                JOptionPane.showMessageDialog(MainWindow.this,
                        "Se guardaron " + savedCount + " archivos de texto y " + pdfCount + " reportes PDF en:\n" +
                                directoryToSave.getAbsolutePath(),
                        "Guardar Todos los Escaneos", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        JMenuItem printMenuItem = new JMenuItem("Imprimir...");
        printMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(MainWindow.this,
                "La funcionalidad de impresión es compleja y NO está implementada aquí.",
                "Imprimir", JOptionPane.INFORMATION_MESSAGE));
        
        JMenuItem closeWindowMenuItem = new JMenuItem("Cerrar ventana");
        closeWindowMenuItem.addActionListener(e -> MainWindow.this.dispose());
        
        JMenuItem exitMenuItem = new JMenuItem("Salir");
        exitMenuItem.addActionListener(e -> System.exit(0));
        
        scanMenu.add(newWindowMenuItem);
        scanMenu.add(openScanMenuItem);
        scanMenu.add(saveScanMenuItem);
        scanMenu.add(saveAllScansMenuItem);
        scanMenu.addSeparator();
        scanMenu.add(printMenuItem);
        scanMenu.add(closeWindowMenuItem);
        scanMenu.add(exitMenuItem);
        
        JMenu toolsMenu = new JMenu("Herramientas");
        JMenuItem toggleDarkModeMenuItem = new JMenuItem("Alternar Modo Oscuro (Nimbus)");
        toolsMenu.add(toggleDarkModeMenuItem);
        toggleDarkModeMenuItem.addActionListener(e -> {
            try {
                if (!(UIManager.getLookAndFeel() instanceof javax.swing.plaf.nimbus.NimbusLookAndFeel)) {
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                }
                if (isDarkModeActive) {
                    UIManager.put("control", new Color(214, 217, 223));
                    UIManager.put("info", new Color(242, 242, 189));
                    UIManager.put("nimbusBase", new Color(51, 98, 140));
                    UIManager.put("nimbusBlueGrey", new Color(169, 182, 192));
                    UIManager.put("text", Color.BLACK);
                    UIManager.put("Table.background", Color.WHITE);
                    UIManager.put("Table.foreground", Color.BLACK);
                    UIManager.put("Table.selectionBackground", new Color(51, 153, 255));
                    UIManager.put("Table.selectionForeground", Color.WHITE);
                    UIManager.put("TabbedPane.background", new Color(214, 217, 223));
                    UIManager.put("TabbedPane.foreground", Color.BLACK);
                    UIManager.put("Button.background", new Color(230, 230, 230));
                    UIManager.put("Button.foreground", Color.BLACK);
                    UIManager.put("ComboBox.background", Color.WHITE);
                    UIManager.put("ComboBox.foreground", Color.BLACK);
                    UIManager.put("TextField.background", Color.WHITE);
                    UIManager.put("TextField.foreground", Color.BLACK);
                    UIManager.put("TextArea.background", Color.WHITE);
                    UIManager.put("TextArea.foreground", Color.BLACK);
                    UIManager.put("Panel.background", new Color(214, 217, 223));
                    UIManager.put("Menu.background", new Color(214, 217, 223));
                    UIManager.put("Menu.foreground", Color.BLACK);
                    UIManager.put("MenuItem.background", new Color(214, 217, 223));
                    UIManager.put("MenuItem.foreground", Color.BLACK);
                    UIManager.put("Label.foreground", Color.BLACK);
                    UIManager.put("Tree.background", Color.WHITE);
                    UIManager.put("Tree.foreground", Color.BLACK);
                    UIManager.put("Tree.selectionBackground", new Color(51, 153, 255));
                    UIManager.put("Tree.selectionForeground", Color.WHITE);
                    UIManager.put("Tree.selectionBorderColor", new Color(51, 153, 255));
                    isDarkModeActive = false;
                } else {
                    UIManager.put("control", new Color(60, 63, 65));
                    UIManager.put("info", new Color(49, 51, 53));
                    UIManager.put("nimbusBase", new Color(18, 30, 49));
                    UIManager.put("nimbusBlueGrey", new Color(48, 60, 80));
                    UIManager.put("text", Color.WHITE);
                    UIManager.put("Table.background", new Color(50, 50, 50));
                    UIManager.put("Table.foreground", Color.WHITE);
                    UIManager.put("Table.selectionBackground", new Color(70, 70, 70));
                    UIManager.put("Table.selectionForeground", Color.WHITE);
                    UIManager.put("TabbedPane.background", new Color(60, 63, 65));
                    UIManager.put("TabbedPane.foreground", Color.WHITE);
                    UIManager.put("Button.background", new Color(70, 70, 70));
                    UIManager.put("Button.foreground", Color.WHITE);
                    UIManager.put("ComboBox.background", new Color(70, 70, 70));
                    UIManager.put("ComboBox.foreground", Color.WHITE);
                    UIManager.put("TextField.background", new Color(70, 70, 70));
                    UIManager.put("TextField.foreground", Color.WHITE);
                    UIManager.put("TextArea.background", new Color(70, 70, 70));
                    UIManager.put("TextArea.foreground", Color.WHITE);
                    UIManager.put("Panel.background", new Color(60, 63, 65));
                    UIManager.put("Menu.background", new Color(50, 50, 50));
                    UIManager.put("Menu.foreground", Color.WHITE);
                    UIManager.put("MenuItem.background", new Color(50, 50, 50));
                    UIManager.put("MenuItem.foreground", Color.WHITE);
                    UIManager.put("Label.foreground", Color.WHITE);
                    UIManager.put("Tree.background", new Color(60, 63, 65));
                    UIManager.put("Tree.foreground", Color.WHITE);
                    UIManager.put("Tree.selectionBackground", new Color(48, 60, 80));
                    UIManager.put("Tree.selectionForeground", Color.WHITE);
                    UIManager.put("Tree.selectionBorderColor", new Color(48, 60, 80));
                    isDarkModeActive = true;
                }
                SwingUtilities.updateComponentTreeUI(MainWindow.this);
                MainWindow.this.revalidate();
                MainWindow.this.repaint();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(MainWindow.this, "Error al aplicar Nimbus: " + ex.getMessage(), "Error de Tema", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JMenu profileMenu = new JMenu("Perfil");
        JMenu helpMenu = new JMenu("Ayuda");
        JMenuItem studyMenuItem = new JMenuItem("Estudio de Nmap");
        studyMenuItem.setAccelerator(KeyStroke.getKeyStroke("control H"));
        helpMenu.add(studyMenuItem);
        
        menuBar.add(scanMenu);
        menuBar.add(toolsMenu);
        menuBar.add(profileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        scanInputPanel = new ScanInputPanel();
        add(scanInputPanel, BorderLayout.NORTH);

        leftButtonsPanel = new JPanel();
        leftButtonsPanel.setLayout(new GridBagLayout());
        leftButtonsPanel.setBorder(BorderFactory.createTitledBorder("Navegación de Escaneos"));
        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.insets = new Insets(5, 5, 5, 5);
        gbcLeft.fill = GridBagConstraints.HORIZONTAL;
        gbcLeft.gridx = 0;
        gbcLeft.weightx = 1.0;

        // Botón "Ver Historial de Escaneos"
        gbcLeft.gridy = 0;
        viewHistoryButton = new JButton("Ver Historial de Escaneos");
        leftButtonsPanel.add(viewHistoryButton, gbcLeft);

        // Botón "Ver Último Escaneo"
        gbcLeft.gridy = 1;
        viewLastScanButton = new JButton("Ver Último Escaneo");
        leftButtonsPanel.add(viewLastScanButton, gbcLeft);

        // --- INICIO: CAMBIO DE ORDEN Y NUEVO BOTÓN ---
        // Nuevo botón "Ver Todos Los Registros"
        gbcLeft.gridy = 2; // Ahora en la posición 2
        viewAllRecordsButton = new JButton("Ver Todos Los Registros");
        leftButtonsPanel.add(viewAllRecordsButton, gbcLeft);

        // Separador (para el "Filtrar Servidor")
        gbcLeft.gridy = 3;
        leftButtonsPanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbcLeft);

        // Botón "Filtrar Servidor" (ahora en la posición 4)
        gbcLeft.gridy = 4;
        filterServersButton = new JButton("Filtrar Servidor");
        leftButtonsPanel.add(filterServersButton, gbcLeft);

        // Añadir espacio extra al final
        gbcLeft.gridy = 5;
        gbcLeft.weighty = 1.0;
        leftButtonsPanel.add(Box.createVerticalGlue(), gbcLeft);
        // --- FIN: CAMBIO DE ORDEN Y NUEVO BOTÓN ---

        // Listeners para los botones
        viewHistoryButton.addActionListener(e -> {
            contentTabbedPane.setSelectedComponent(scansPanel);
            statusBar.setText("Estado: Mostrando historial de comandos.");
            mainCardLayout.show(mainContentCardPanel, "SCAN_CONTENT");
            setTitle("IGMAP - Nmap GUI Scanner");
        });

        viewLastScanButton.addActionListener(e -> {
            if (!allScanResults.isEmpty()) {
                ScanResult lastResult = allScanResults.get(allScanResults.size() - 1);
                displayScanResult(lastResult);
                contentTabbedPane.setSelectedComponent(serverAndServicesPanel);
                statusBar.setText("Estado: Mostrando el último escaneo: " + lastResult.getTarget());
                mainCardLayout.show(mainContentCardPanel, "SCAN_CONTENT");
                setTitle("IGMAP - Nmap GUI Scanner");
            } else {
                JOptionPane.showMessageDialog(MainWindow.this, "No hay escaneos en el historial.", "Historial Vacío", JOptionPane.INFORMATION_MESSAGE);
                statusBar.setText("Estado: Historial de escaneos vacío.");
            }
        });

        // --- NUEVO LISTENER PARA EL BOTÓN "VER TODOS LOS REGISTROS" ---
        viewAllRecordsButton.addActionListener(e -> {
            // Creamos y mostramos la nueva ventana con la tabla
            JFrame recordsFrame = new JFrame("Todos los Registros");
            recordsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            recordsFrame.add(new AllRecordsPanel()); // Usa la clase que creamos antes
            recordsFrame.setSize(800, 600);
            recordsFrame.setLocationRelativeTo(null);
            
            // --- CÓDIGO PARA AGREGAR EL ÍCONO A LA VENTANA DE REGISTROS (si no lo tiene ya AllRecordsPanel) ---
            try {
                // Asegúrate de que esta ruta sea la correcta en tu sistema.
                Image icon = new ImageIcon("/home/angel/Descargas/BSC/Igmap/src/java/com/nmapscanner/auth/Img/IGMAP.png").getImage();
                recordsFrame.setIconImage(icon);
            } catch (Exception ex) {
                System.err.println("No se pudo cargar el ícono para la ventana de registros.");
                ex.printStackTrace();
            }
            // --- FIN DEL CÓDIGO DE ÍCONO PARA VENTANA DE REGISTROS ---

            recordsFrame.setVisible(true);
        });

        filterServersButton.addActionListener(e -> {
            contentTabbedPane.setSelectedComponent(serverAndServicesPanel);
            statusBar.setText("Estado: Listo para filtrar servidores/servicios.");
            mainCardLayout.show(mainContentCardPanel, "SCAN_CONTENT");
            setTitle("IGMAP - Nmap GUI Scanner");
        });

        contentTabbedPane = new JTabbedPane();
        nmapOutputPanel = new NmapOutputPanel();
        contentTabbedPane.addTab("Salida Nmap", nmapOutputPanel);
        serverAndServicesPanel = new ServerAndServicesPanel();
        contentTabbedPane.addTab("Servidores / Servicios", serverAndServicesPanel);
        topologyPanel = new TopologyPanel();
        contentTabbedPane.addTab("Topología", topologyPanel);
        serverDetailsPanel = new ServerDetailsPanel();
        contentTabbedPane.addTab("Detalles del servidor", serverDetailsPanel);
        scansPanel = new ScansPanel();
        contentTabbedPane.addTab("Historial de Comandos", scansPanel);

        mainCardLayout = new CardLayout();
        mainContentCardPanel = new JPanel(mainCardLayout);
        mainContentCardPanel.add(contentTabbedPane, "SCAN_CONTENT");
        HelpStudyPanel helpStudyPanel = new HelpStudyPanel();
        mainContentCardPanel.add(helpStudyPanel, "HELP_CONTENT");
        mainCardLayout.show(mainContentCardPanel, "SCAN_CONTENT");

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftButtonsPanel, mainContentCardPanel);
        mainSplitPane.setResizeWeight(0.0);
        mainSplitPane.setDividerLocation(200);
        mainSplitPane.setOneTouchExpandable(true);
        add(mainSplitPane, BorderLayout.CENTER);

        statusBar = new JLabel("Listo.");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        add(statusBar, BorderLayout.SOUTH);

        scanInputPanel.setResultsDisplay(nmapOutputPanel.getResultsDisplayArea());
        scanInputPanel.setStatusBar(statusBar);
        scanInputPanel.setScanResultConsumer(this::handleNewScanResult);

        studyMenuItem.addActionListener(e -> {
            mainCardLayout.show(mainContentCardPanel, "HELP_CONTENT");
            setTitle("IGMAP - Guía de Estudio de Nmap");
        });
        JMenuItem backToScanItem = new JMenuItem("Volver al Escaneo");
        helpMenu.add(backToScanItem);
        backToScanItem.addActionListener(e -> {
            mainCardLayout.show(mainContentCardPanel, "SCAN_CONTENT");
            setTitle("IGMAP - Nmap GUI Scanner");
        });
        
        // Preferencias para guardar la ruta de Nmap
        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Puedes guardar la ruta de nmap aquí si es necesario
                // prefs.put("nmapPath", "nmap"); // o la ruta actual si se configura en algún lugar
            }
        });
    }

    private void handleNewScanResult(ScanResult result) {
        this.currentScanResult = result;
        allScanResults.add(result);
        displayScanResult(result);
        if (scansPanel != null) {
            String commandUsed = result.getCommandUsed() != null && !result.getCommandUsed().isEmpty() ? result.getCommandUsed() : "N/A";
            scansPanel.addScan(result.getTarget(), commandUsed);
        }
        contentTabbedPane.setSelectedComponent(serverAndServicesPanel);
        mainCardLayout.show(mainContentCardPanel, "SCAN_CONTENT");
        setTitle("IGMAP - Nmap GUI Scanner");
        
        // Guardar en la base de datos
        saveScanResultToDatabase(result);
    }

    private void displayScanResult(ScanResult result) {
        this.currentScanResult = result;
        if (nmapOutputPanel != null && nmapOutputPanel.getResultsDisplayArea() != null) {
            nmapOutputPanel.getResultsDisplayArea().setText(result.getRawOutput());
            nmapOutputPanel.getResultsDisplayArea().setCaretPosition(0);
        }
        if (statusBar != null) {
            statusBar.setText("Estado: Mostrando escaneo de " + result.getTarget() + ".");
        }
        if (serverAndServicesPanel != null) {
            serverAndServicesPanel.updatePanelWithScanResult(result);
        }
        if (serverDetailsPanel != null) {
            serverDetailsPanel.updateDetails(result);
        }
        if (topologyPanel != null) {
            topologyPanel.updateTopology(result.getTraceRouteHops());
        }
    }
    
    // Método para guardar resultados en la base de datos
    private void saveScanResultToDatabase(ScanResult scanResult) {
        // Asegúrate de que las credenciales y la URL de la base de datos son correctas
        String url = "jdbc:mysql://localhost:3306/proyecto";
        String user = "angel";
        String password = "angel";
        
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "INSERT INTO direccionesIP (IP, puertos, fecha_registro) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            
            // Construir la cadena de puertos
            String puertosStr = "";
            for (int i = 0; i < scanResult.getOpenPorts().size(); i++) {
                puertosStr += scanResult.getOpenPorts().get(i).getPortNumber() + "/" + scanResult.getOpenPorts().get(i).getState();
                if (i < scanResult.getOpenPorts().size() - 1) {
                    puertosStr += ", ";
                }
            }

            statement.setString(1, scanResult.getTarget());
            statement.setString(2, puertosStr);
            statement.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));

            statement.executeUpdate();
            System.out.println("Resultado de escaneo guardado en la base de datos."); // Para depuración
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar el resultado en la base de datos: " + e.getMessage(), "Error de DB", JOptionPane.ERROR_MESSAGE);
        }
    }

    // El método main para iniciar la aplicación
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            // Tus configuraciones de Nimbus...
            UIManager.put("control", new Color(214, 217, 223));
            UIManager.put("info", new Color(242, 242, 189));
            UIManager.put("nimbusBase", new Color(51, 98, 140));
            UIManager.put("nimbusBlueGrey", new Color(169, 182, 192));
            UIManager.put("text", Color.BLACK);
            UIManager.put("Table.background", Color.WHITE);
            UIManager.put("Table.foreground", Color.BLACK);
            UIManager.put("Table.selectionBackground", new Color(51, 153, 255));
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("TabbedPane.background", new Color(214, 217, 223));
            UIManager.put("TabbedPane.foreground", Color.BLACK);
            UIManager.put("Button.background", new Color(230, 230, 230));
            UIManager.put("Button.foreground", Color.BLACK);
            UIManager.put("ComboBox.background", Color.WHITE);
            UIManager.put("ComboBox.foreground", Color.BLACK);
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.foreground", Color.BLACK);
            UIManager.put("TextArea.background", Color.WHITE);
            UIManager.put("TextArea.foreground", Color.BLACK);
            UIManager.put("Panel.background", new Color(214, 217, 223));
            UIManager.put("Menu.background", new Color(214, 217, 223));
            UIManager.put("Menu.foreground", Color.BLACK);
            UIManager.put("MenuItem.background", new Color(214, 217, 223));
            UIManager.put("MenuItem.foreground", Color.BLACK);
            UIManager.put("Label.foreground", Color.BLACK);
            UIManager.put("Tree.background", Color.WHITE);
            UIManager.put("Tree.foreground", Color.BLACK);
            UIManager.put("Tree.selectionBackground", new Color(51, 153, 255));
            UIManager.put("Tree.selectionForeground", Color.WHITE);
            UIManager.put("Tree.selectionBorderColor", new Color(51, 153, 255));
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            System.err.println("Error al inicializar Look and Feel: " + ex.getMessage());
            ex.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
