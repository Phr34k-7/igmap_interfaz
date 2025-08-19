package com.nmapscanner.core;

import com.nmapscanner.model.ScanResult;
import com.nmapscanner.model.Port;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

public class PdfReportGenerator {

    private static final PDType1Font FONT_BOLD = new PDType1Font(FontName.HELVETICA_BOLD);
    private static final PDType1Font FONT_PLAIN = new PDType1Font(FontName.HELVETICA);
    private static final PDType1Font FONT_MONOSPACE = new PDType1Font(FontName.COURIER);

    public void generateScanReport(ScanResult scanResult, String outputPath) throws IOException {
        if (scanResult == null) {
            throw new IllegalArgumentException("ScanResult no puede ser nulo.");
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float yPosition = yStart;
            float leading = 1.5f * 12; 

            // --- Título del Reporte ---
            contentStream.beginText();
            contentStream.setFont(FONT_BOLD, 18);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Reporte de Escaneo Nmap");
            contentStream.endText();
            yPosition -= leading * 2;

            // --- Información General ---
            contentStream.beginText();
            contentStream.setFont(FONT_PLAIN, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Objetivo: " + scanResult.getTarget());
            yPosition -= leading;
            contentStream.newLineAtOffset(0, -leading); 
            String command = scanResult.getCommandUsed() != null && !scanResult.getCommandUsed().isEmpty() ? scanResult.getCommandUsed() : "N/A";
            contentStream.showText("Comando: " + command);
            yPosition -= leading;
            contentStream.newLineAtOffset(0, -leading);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            contentStream.showText("Fecha del Escaneo: " + (scanResult.getTimestamp() != null ? sdf.format(scanResult.getTimestamp()) : "N/A"));
            yPosition -= leading;
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText("Duración del Escaneo: " + (scanResult.getScanDuration() != null ? scanResult.getScanDuration() : "N/A"));
            contentStream.endText();
            yPosition -= leading * 2;

            // --- Salida Nmap Cruda ---
            contentStream.beginText();
            contentStream.setFont(FONT_BOLD, 14);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Salida Nmap Cruda:");
            contentStream.endText();
            yPosition -= leading;

            contentStream.beginText();
            contentStream.setFont(FONT_MONOSPACE, 10);
            contentStream.setLeading(1.2f * 10); 
            contentStream.newLineAtOffset(margin, yPosition);

            String rawOutput = scanResult.getRawOutput();
            if (rawOutput != null && !rawOutput.isEmpty()) {
                String[] lines = rawOutput.split("\\r?\\n");
                for (String line : lines) {
                    if (yPosition < margin + 50) { 
                        contentStream.endText();
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = page.getMediaBox().getHeight() - margin;
                        contentStream.beginText();
                        contentStream.setFont(FONT_MONOSPACE, 10);
                        contentStream.setLeading(1.2f * 10);
                        contentStream.newLineAtOffset(margin, yPosition);
                    }
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -1.2f * 10); 
                    yPosition -= 1.2f * 10;
                }
            } else {
                contentStream.showText("No hay salida Nmap disponible.");
                yPosition -= leading;
            }
            contentStream.endText();
            yPosition -= leading * 2;


            // --- Tabla de Puertos / Servidores ---
            contentStream.beginText();
            contentStream.setFont(FONT_BOLD, 14);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Puertos y Servicios:");
            contentStream.endText();
            yPosition -= leading;

            List<Port> ports = scanResult.getOpenPorts(); // Usamos getOpenPorts()
            if (ports != null && !ports.isEmpty()) {
                contentStream.beginText();
                contentStream.setFont(FONT_BOLD, 10);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(String.format("%-10s %-10s %-15s %-20s %-15s", "PUERTO", "PROTO", "ESTADO", "SERVICIO", "VERSIÓN"));
                contentStream.endText();
                yPosition -= leading / 2; 

                for (Port port : ports) {
                    if (yPosition < margin + 50) { 
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = page.getMediaBox().getHeight() - margin;
                        contentStream.beginText();
                        contentStream.setFont(FONT_BOLD, 10);
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText(String.format("%-10s %-10s %-15s %-20s %-15s", "PUERTO", "PROTO", "ESTADO", "SERVICIO", "VERSIÓN"));
                        contentStream.endText();
                        yPosition -= leading / 2;
                    }
                    contentStream.beginText();
                    contentStream.setFont(FONT_PLAIN, 10);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(String.format("%-10s %-10s %-15s %-20s %-15s",
                            port.getPortNumber(), // CORREGIDO: Usar getPortNumber()
                            port.getProtocol(),
                            port.getState(),
                            port.getService(),
                            port.getVersion() != null ? port.getVersion() : "N/A"));
                    contentStream.endText();
                    yPosition -= leading / 2;
                }
            } else {
                contentStream.beginText();
                contentStream.setFont(FONT_PLAIN, 10);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("No se encontraron puertos.");
                contentStream.endText();
                yPosition -= leading;
            }
            yPosition -= leading; 

            // --- Detalles del Servidor ---
            contentStream.beginText();
            contentStream.setFont(FONT_BOLD, 14);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Detalles del Servidor:");
            contentStream.endText();
            yPosition -= leading;

            contentStream.beginText();
            contentStream.setFont(FONT_PLAIN, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            
            contentStream.showText("Objetivo: " + (scanResult.getTarget() != null ? scanResult.getTarget() : "N/A"));
            yPosition -= leading / 2;
            contentStream.newLineAtOffset(0, -leading / 2);
            contentStream.showText("IP: " + (scanResult.getIpAddress() != null ? scanResult.getIpAddress() : "N/A"));
            yPosition -= leading / 2;
            contentStream.newLineAtOffset(0, -leading / 2);
            contentStream.showText("Estado del Host: " + (scanResult.getHostStatus() != null ? scanResult.getHostStatus() : "N/A"));
            yPosition -= leading / 2;
            contentStream.newLineAtOffset(0, -leading / 2);
            contentStream.showText("Latencia: " + (scanResult.getLatency() != null ? scanResult.getLatency() : "N/A"));
            yPosition -= leading / 2;
            contentStream.newLineAtOffset(0, -leading / 2);
            contentStream.showText("SO (Guess): " + (scanResult.getOsGuess() != null ? scanResult.getOsGuess() : "N/A"));
            yPosition -= leading / 2;
            contentStream.newLineAtOffset(0, -leading / 2);
            contentStream.showText("Uptime: " + (scanResult.getUptime() != null ? scanResult.getUptime() : "N/A"));
            yPosition -= leading / 2;
            contentStream.newLineAtOffset(0, -leading / 2);
            
            List<String> hostnames = scanResult.getHostnames();
            contentStream.showText("Hostnames: " + (hostnames != null && !hostnames.isEmpty() ? String.join(", ", hostnames) : "N/A"));
            yPosition -= leading / 2;

            contentStream.endText();
            yPosition -= leading * 2;


            // --- Topología (Saltos de Ruta) ---
            contentStream.beginText();
            contentStream.setFont(FONT_BOLD, 14);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Topología (Saltos de Ruta):");
            contentStream.endText();
            yPosition -= leading;

            contentStream.beginText();
            contentStream.setFont(FONT_PLAIN, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            List<String> traceRouteHops = scanResult.getTraceRouteHops();
            if (traceRouteHops != null && !traceRouteHops.isEmpty()) {
                for (String hop : traceRouteHops) {
                    if (yPosition < margin + 50) { 
                        contentStream.endText();
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = page.getMediaBox().getHeight() - margin;
                        contentStream.beginText();
                        contentStream.setFont(FONT_PLAIN, 10);
                        contentStream.newLineAtOffset(margin, yPosition);
                    }
                    contentStream.showText(hop);
                    contentStream.newLineAtOffset(0, -leading / 2);
                    yPosition -= leading / 2;
                }
            } else {
                contentStream.showText("No se encontraron saltos de ruta (requiere --traceroute).");
                yPosition -= leading;
            }
            contentStream.endText();
            yPosition -= leading * 2;


            // --- Sección de Escaneos (placeholder) ---
            contentStream.beginText();
            contentStream.setFont(FONT_BOLD, 14);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Historial de Escaneos (Ejemplo):");
            contentStream.endText();
            yPosition -= leading;
            
            contentStream.beginText();
            contentStream.setFont(FONT_PLAIN, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Esta sección podría listar los escaneos guardados en ScansPanel.");
            contentStream.endText();


            contentStream.close();
            document.save(outputPath);
        }
    }
}
