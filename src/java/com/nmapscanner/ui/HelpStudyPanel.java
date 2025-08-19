package com.nmapscanner.ui;

import javax.swing.*;
import java.awt.*;

public class HelpStudyPanel extends JPanel {

    public HelpStudyPanel() {
        setLayout(new BorderLayout()); // Usamos BorderLayout para un diseño simple
        setBackground(Color.WHITE); // Fondo blanco para el panel de ayuda

        // Título principal del panel de ayuda
        JLabel mainTitle = new JLabel("Guía de Estudio de Nmap", SwingConstants.CENTER);
        mainTitle.setFont(new Font("Arial", Font.BOLD, 28));
        mainTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(mainTitle, BorderLayout.NORTH);

        // Panel scrollable para el contenido (necesario si el texto es largo)
        JTextPane contentTextPane = new JTextPane();
        contentTextPane.setContentType("text/html"); // Para poder usar HTML para formato
        contentTextPane.setEditable(false); // No editable
        contentTextPane.setBackground(Color.WHITE);
        contentTextPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE); // Para que use la fuente default
        contentTextPane.setFont(new Font("SansSerif", Font.PLAIN, 14)); // Ajusta el tamaño de fuente si es necesario


        // Contenido HTML detallado
        String htmlContent = "<html><body style='font-family: SansSerif; font-size: 14px; margin: 20px;'>" +
                              "<h2>1. Introducción a Nmap</h2>" +
                              "<p><b>Nmap (Network Mapper)</b> es una herramienta de código abierto para la exploración de redes y auditorías de seguridad. Fue creado por Gordon Lyon (conocido como Fyodor) y se lanzó por primera vez en 1997. Se ha convertido en una herramienta indispensable para administradores de sistemas, ingenieros de redes y profesionales de la seguridad.</p>" +
                              "<p><b>¿Para qué sirve Nmap?</b></p>" +
                              "<ul>" +
                              "<li><b>Descubrimiento de hosts:</b> Identifica qué dispositivos están activos en una red.</li>" +
                              "<li><b>Descubrimiento de puertos:</b> Determina qué puertos están abiertos en los hosts, indicando posibles servicios en ejecución.</li>" +
                              "<li><b>Detección de servicios:</b> Identifica la aplicación y la versión del servicio que está escuchando en un puerto abierto (ej. Apache 2.4, OpenSSH 8.2).</li>" +
                              "<li><b>Detección de sistemas operativos:</b> Intenta determinar el sistema operativo y la versión del host remoto.</li>" +
                              "<li><b>Detección de vulnerabilidades:</b> A través de su motor de scripting (NSE), puede automatizar la detección de vulnerabilidades y la explotación básica.</li>" +
                              "</ul>" +
                              "<p>Nmap es extremadamente versátil y se utiliza para una amplia gama de tareas, desde el inventario de redes hasta la identificación de configuraciones erróneas de seguridad.</p>" +

                              "<h2>2. Tipos de Escaneo en IGMAP y sus Comandos Nmap</h2>" +
                              "<p>Aquí te explicamos los tipos de escaneo disponibles en IGMAP y sus equivalentes en la línea de comandos de Nmap, con una descripción de cada parámetro.</p>" +

                              "<h3>2.1. Regular Scan (Escaneo Regular)</h3>" +
                              "<p>Este escaneo es un buen punto de partida para una revisión general de un host. Combina escaneo de puertos, detección de servicios y detección de sistema operativo.</p>" +
                              "<p><b>Comando Nmap:</b> <code>-sS -sV -O</code></p>" +
                              "<ul>" +
                              "<li><b><code>-sS</code> (SYN Scan / Half-open Scan):</b><br>Es el escaneo de puertos por defecto y el más popular. Es rápido y discreto porque no completa la conexión TCP. Envía un paquete SYN y espera la respuesta. Si recibe SYN/ACK, el puerto está abierto; si recibe RST, está cerrado.</li>" +
                              "<li><b><code>-sV</code> (Service/Version Detection):</b><br>Intenta determinar qué servicio y qué versión de ese servicio están escuchando en los puertos abiertos. Esto es crucial para identificar posibles vulnerabilidades específicas de una versión de software.</li>" +
                              "<li><b><code>-O</code> (OS Detection):</b><br>Realiza una detección del sistema operativo del host remoto. Envía una serie de paquetes TCP e IP a los puertos abiertos y cerrados para analizar las respuestas y compararlas con una base de datos de huellas dactilares de SO.</li>" +
                              "</ul>" +

                              "<h3>2.2. Quick Scan (Escaneo Rápido)</h3>" +
                              "<p>Un escaneo más veloz que el Regular Scan, ideal para un chequeo rápido de los puertos más comunes.</p>" +
                              "<p><b>Comando Nmap:</b> <code>-sS -F --version-light</code></p>" +
                              "<ul>" +
                              "<li><b><code>-sS</code> (SYN Scan):</b> Igual que en el Regular Scan, para un escaneo eficiente de puertos.</li>" +
                              "<li><b><code>-F</code> (Fast Scan):</b> Escanea solo los 100 puertos más comunes según la base de datos de Nmap. Esto lo hace significativamente más rápido que un escaneo completo de los 65535 puertos.</li>" +
                              "<li><b><code>--version-light</code> (Lightweight Service/Version Detection):</b> Una versión más ligera de <code>-sV</code> que intenta detectar servicios, pero con menos intensidad y rapidez, enfocándose solo en los puertos más comunes.</li>" +
                              "</ul>" +

                              "<h3>2.3. Full Port Scan (Escaneo de Puertos Completos)</h3>" +
                              "<p>Este escaneo revisa los 65535 puertos TCP de un host, garantizando que no se omita ningún servicio que escuche en un puerto no estándar.</p>" +
                              "<p><b>Comando Nmap:</b> <code>-sS -p 1-65535</code></p>" +
                              "<ul>" +
                              "<li><b><code>-sS</code> (SYN Scan):</b> Método de escaneo de puertos.</li>" +
                              "<li><b><code>-p 1-65535</code> (Port Specification):</b> Especifica el rango de puertos a escanear. En este caso, de todos los puertos TCP (1 al 65535).</li>" +
                              "</ul>" +

                              "<h3>2.4. Stealth Scan (Escaneo Sigiloso)</h3>" +
                              "<p>Un escaneo que intenta ser lo más discreto posible, a menudo utilizado para evitar la detección de firewalls y sistemas de detección de intrusiones (IDS).</p>" +
                              "<p><b>Comando Nmap:</b> <code>-sS -Pn -f</code></p>" +
                              "<ul>" +
                              // ¡Esta es la línea corregida! Se escapa la comilla doble antes de "medio-abierto"
                              "<li><b><code>-sS</code> (SYN Scan):</b> Se utiliza por su naturaleza de \"medio-abierto\" que a veces puede pasar desapercibida.</li>" +
                              "<li><b><code>-Pn</code> (No Ping):</b> Salta la fase de descubrimiento de host (ping). Nmap asume que el host está activo, lo que es útil si el host bloquea solicitudes de ping. Esto evita que el firewall detecte la sonda inicial de ping.</li>" +
                              "<li><b><code>-f</code> (Fragment Packets):</b> Fragmenta los paquetes IP en partes más pequeñas. Algunos firewalls o IDS son menos eficientes en reensamblar y analizar paquetes fragmentados, lo que puede permitir que el escaneo pase por alto sus reglas.</li>" +
                              "</ul>" +

                              "<h3>2.5. Aggressive Scan (Escaneo Agresivo)</h3>" +
                              "<p>Un escaneo muy completo y ruidoso. Incluye detección de SO, detección de versiones, escaneo de scripts predeterminados (NSE) y traceroute.</p>" +
                              "<p><b>Comando Nmap:</b> <code>-A</code></p>" +
                              "<ul>" +
                              "<li><b><code>-A</code> (Aggressive Scan):</b><br>Es una opción combinada que activa varias funcionalidades avanzadas de Nmap: <code>-O</code> (detección de SO), <code>-sV</code> (detección de servicio/versión), <code>-sC</code> (ejecuta scripts NSE predeterminados) y <code>--traceroute</code> (realiza un traceroute para mapear la ruta de red). Es rápido, completo, pero también más fácil de detectar.</li>" +
                              "</ul>" +

                              "<h3>2.6. Custom Scan (Escaneo Personalizado)</h3>" +
                              "<p>Permite al usuario introducir comandos Nmap personalizados, ofreciendo la máxima flexibilidad para necesidades específicas.</p>" +
                              "<p><b>Comando Nmap:</b> El comando es determinado por el usuario (ej. <code>-sU -p 53,161</code> para escanear puertos UDP específicos, o <code>-sN</code> para un escaneo Null).</p>" +
                              "<ul>" +
                              "<li>No hay parámetros fijos, ya que dependen de la entrada del usuario. Aquí el usuario tiene control total sobre el comando Nmap a ejecutar.</li>" +
                              "</ul>" +

                              "</body></html>";
        contentTextPane.setText(htmlContent);

        // Añadir el JTextPane a un JScrollPane para que sea scrollable
        JScrollPane scrollPane = new JScrollPane(contentTextPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }
}
