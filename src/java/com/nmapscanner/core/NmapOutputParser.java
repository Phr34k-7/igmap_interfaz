package com.nmapscanner.core;

import com.nmapscanner.model.Port;
import com.nmapscanner.model.ScanResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NmapOutputParser {

    public ScanResult parseOutput(String nmapOutput) {
        if (nmapOutput == null || nmapOutput.isEmpty()) {
            // Usar el constructor que NmapOutputParser espera
            ScanResult emptyResult = new ScanResult("N/A", "N/A", "N/A", "No hay salida de Nmap para parsear.");
            emptyResult.setRawOutput(nmapOutput);
            return emptyResult;
        }

        String target = "N/A";
        String ipAddress = "N/A";
        String osGuess = "N/A";
        String commandUsed = "N/A";
        String hostStatus = "N/A";
        String latency = "N/A";
        String uptime = "N/A";
        String scanDuration = "N/A";
        List<String> hostnames = new ArrayList<>();
        List<Port> ports = new ArrayList<>(); // Renombrado de openPorts a ports para claridad
        List<String> traceRouteHops = new ArrayList<>();


        // --- Regex para extraer información clave ---
        
        // Target (IP/Hostname)
        Pattern targetPattern = Pattern.compile("Nmap scan report for (.+?) \\((.+?)\\)");
        Matcher targetMatcher = targetPattern.matcher(nmapOutput);
        if (targetMatcher.find()) {
            target = targetMatcher.group(1).trim();
            ipAddress = targetMatcher.group(2).trim();
        } else {
            Pattern simpleTargetPattern = Pattern.compile("Nmap scan report for ([\\w.-]+(?:\\.[\\w.-]+)*)");
            Matcher simpleTargetMatcher = simpleTargetPattern.matcher(nmapOutput);
            if(simpleTargetMatcher.find()){
                target = simpleTargetMatcher.group(1).trim();
                // Si solo encontramos un target sin IP, la IP queda en N/A
            }
        }

        // Command Used
        Pattern commandPattern = Pattern.compile("Nmap command: (.+)");
        Matcher commandMatcher = commandPattern.matcher(nmapOutput);
        if (commandMatcher.find()) {
            commandUsed = commandMatcher.group(1).trim();
        }

        // Host Status and Latency
        Pattern hostStatusPattern = Pattern.compile("Host is (up|down)(?: \\((.+?) latency\\))?");
        Matcher hostStatusMatcher = hostStatusPattern.matcher(nmapOutput);
        if (hostStatusMatcher.find()) {
            hostStatus = hostStatusMatcher.group(1).trim();
            if (hostStatusMatcher.group(2) != null) {
                latency = hostStatusMatcher.group(2).trim();
            }
        }
        
        // Hostnames (puede haber múltiples líneas con "Nmap scan report for")
        Pattern hostnamePattern = Pattern.compile("Nmap scan report for (\\S+)(?: \\((.+?)\\))?");
        Matcher hostnameMatcher = hostnamePattern.matcher(nmapOutput);
        java.util.Set<String> uniqueHostnames = new java.util.HashSet<>();
        while (hostnameMatcher.find()) {
            String hostname = hostnameMatcher.group(1).trim();
            String ip = hostnameMatcher.group(2);
            // Evitar añadir el target principal de nuevo si ya está en target/ipAddress
            if (!hostname.equals(target) && !hostname.equals(ipAddress)) { 
                uniqueHostnames.add(hostname + (ip != null ? " (" + ip + ")" : ""));
            }
        }
        hostnames.addAll(new ArrayList<>(uniqueHostnames));

        // Ports
        Pattern portPattern = Pattern.compile("(\\d+)/(\\w+)\\s+([\\w\\-]+)\\s+([\\w\\-]+)\\s*(.*)");
        Matcher portMatcher = portPattern.matcher(nmapOutput);
        while (portMatcher.find()) {
            try {
                int portNumber = Integer.parseInt(portMatcher.group(1));
                String protocol = portMatcher.group(2).trim();
                String state = portMatcher.group(3).trim();
                String service = portMatcher.group(4).trim();
                String version = portMatcher.group(5).trim();
                
                ports.add(new Port(portNumber, protocol, state, service, version));

            } catch (NumberFormatException ex) {
                System.err.println("Error parseando número de puerto: " + portMatcher.group(1));
            }
        }

        // OS Guess
        Pattern osPattern = Pattern.compile("OS details: ([^\\n]+)");
        Matcher osMatcher = osPattern.matcher(nmapOutput);
        if (osMatcher.find()) {
            osGuess = osMatcher.group(1).trim();
        }

        // Uptime
        Pattern uptimePattern = Pattern.compile("Uptime: (.+?)(?:\\s+\\([^)]+\\))?"); // Captura hasta el final de la línea o paréntesis
        Matcher uptimeMatcher = uptimePattern.matcher(nmapOutput);
        if(uptimeMatcher.find()){
            uptime = uptimeMatcher.group(1).trim();
        }

        // Scan Duration (e.g., "Nmap done: 1 IP address (1 host up) scanned in 9.54 seconds")
        Pattern scanTimePattern = Pattern.compile("scanned in ([\\d.]+ seconds)");
        Matcher scanTimeMatcher = scanTimePattern.matcher(nmapOutput);
        if (scanTimeMatcher.find()) {
            scanDuration = scanTimeMatcher.group(1).trim();
        }


        // Traceroute Hops
        Pattern traceRouteSectionPattern = Pattern.compile("TRACEROUTE:(.*?)(?=\\n\\n|\\nNmap done:|$)", Pattern.DOTALL);
        Matcher traceRouteSectionMatcher = traceRouteSectionPattern.matcher(nmapOutput);
        if (traceRouteSectionMatcher.find()) {
            String traceSection = traceRouteSectionMatcher.group(1);
            Pattern hopPattern = Pattern.compile("^\\s*(\\d+)\\s+([\\d.]+ms)\\s+(.+)$", Pattern.MULTILINE);
            Matcher hopMatcher = hopPattern.matcher(traceSection);
            while (hopMatcher.find()) {
                traceRouteHops.add(hopMatcher.group(0).trim()); // Añade toda la línea del hop
            }
        }
        
        // Crear el objeto ScanResult y poblarlo
        ScanResult result = new ScanResult(target, ipAddress, osGuess, nmapOutput);
        result.setCommandUsed(commandUsed);
        result.setHostStatus(hostStatus);
        result.setLatency(latency);
        result.setHostnames(hostnames);
        result.setUptime(uptime);
        result.setOpenPorts(ports); // Setear la lista completa de puertos
        result.setTraceRouteHops(traceRouteHops);
        result.setScanDuration(scanDuration); // Setear la duración del escaneo

        return result;
    }
}
