package com.nmapscanner.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap; // Necesario para getPortStateCounts
import java.text.SimpleDateFormat; // Necesario para getScanTime

public class ScanResult {
    private String target;
    private String ipAddress;
    private String commandUsed;
    private String rawOutput;
    private List<Port> openPorts; // Contiene todos los puertos (open, closed, filtered)
    private String hostStatus;
    private String latency;
    private List<String> hostnames;
    private String osGuess; // Usamos osGuess como nombre consistente
    private String uptime;
    private List<String> traceRouteHops;
    private Date timestamp; // Fecha/hora de inicio del escaneo
    private String scanDuration; // Duración del escaneo (ej. "9.54s")

    // Constructor principal que NmapOutputParser usará
    public ScanResult(String target, String ipAddress, String osGuess, String rawOutput) {
        this.target = target;
        this.ipAddress = ipAddress;
        this.osGuess = osGuess;
        this.rawOutput = rawOutput;
        this.openPorts = new ArrayList<>();
        this.hostnames = new ArrayList<>();
        this.traceRouteHops = new ArrayList<>();
        this.timestamp = new Date(); // Se inicializa al crear el objeto
        
        // Valores por defecto para evitar nulls en la UI/PDF
        this.hostStatus = "N/A";
        this.latency = "N/A";
        this.uptime = "N/A";
        this.commandUsed = "N/A";
        this.scanDuration = "N/A";
    }

    // Constructor secundario (si se usa en algún otro lugar con menos argumentos)
    public ScanResult(String target, String rawOutput) {
        this(target, "N/A", "N/A", rawOutput); // Llama al constructor principal
    }
    
    // Otro constructor si solo se da el target y el comando (ej. de ScanInputPanel)
    public ScanResult(String target, String commandUsed, String rawOutput) {
        this(target, "N/A", "N/A", rawOutput); // Llama al constructor principal
        this.commandUsed = commandUsed;
    }


    // --- Getters y Setters ---

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCommandUsed() {
        return commandUsed;
    }

    public void setCommandUsed(String commandUsed) {
        this.commandUsed = commandUsed;
    }

    public String getRawOutput() {
        return rawOutput;
    }

    public void setRawOutput(String rawOutput) {
        this.rawOutput = rawOutput;
    }

    public List<Port> getOpenPorts() {
        return openPorts;
    }

    public void setOpenPorts(List<Port> openPorts) {
        this.openPorts = openPorts;
    }

    public void addPort(Port port) { // Método para añadir un solo puerto
        if (this.openPorts == null) { this.openPorts = new ArrayList<>(); }
        this.openPorts.add(port);
    }

    public String getHostStatus() {
        return hostStatus;
    }

    public void setHostStatus(String hostStatus) {
        this.hostStatus = hostStatus;
    }

    public String getLatency() {
        return latency;
    }

    public void setLatency(String latency) {
        this.latency = latency;
    }

    public List<String> getHostnames() {
        if (hostnames == null) { this.hostnames = new ArrayList<>(); }
        return hostnames;
    }

    public void setHostnames(List<String> hostnames) {
        this.hostnames = hostnames;
    }
    
    // getOsDetails() es un alias para getOsGuess() para compatibilidad
    public String getOsDetails() {
        return osGuess;
    }

    public String getOsGuess() {
        return osGuess;
    }

    public void setOsGuess(String osGuess) {
        this.osGuess = osGuess;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public List<String> getTraceRouteHops() {
        if (traceRouteHops == null) { this.traceRouteHops = new ArrayList<>(); }
        return traceRouteHops;
    }

    public void setTraceRouteHops(List<String> traceRouteHops) {
        this.traceRouteHops = traceRouteHops;
    }

    public void addTraceRouteHop(String hop) {
        if (this.traceRouteHops == null) { this.traceRouteHops = new ArrayList<>(); }
        this.traceRouteHops.add(hop);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getScanDuration() { // Nuevo getter para la duración
        return scanDuration;
    }

    public void setScanDuration(String scanDuration) { // Nuevo setter para la duración
        this.scanDuration = scanDuration;
    }

    // Método para obtener el tiempo de escaneo formateado (para UI)
    public String getScanTime() {
        if (timestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            return sdf.format(timestamp);
        }
        return "N/A";
    }
    
    // Método para calcular y devolver el conteo de estados de puertos
    public Map<String, Integer> getPortStateCounts() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("open", 0);
        counts.put("closed", 0);
        counts.put("filtered", 0);
        counts.put("unfiltered", 0); 
        counts.put("unknown", 0); // Por si acaso

        if (openPorts != null) {
            for (Port port : openPorts) {
                String state = port.getState().toLowerCase();
                counts.put(state, counts.getOrDefault(state, 0) + 1);
            }
        }
        return counts;
    }

    @Override
    public String toString() {
        return "ScanResult{" +
               "target='" + target + '\'' +
               ", ipAddress='" + ipAddress + '\'' +
               ", commandUsed='" + commandUsed + '\'' +
               ", rawOutput='" + (rawOutput != null && rawOutput.length() > 50 ? rawOutput.substring(0, 50) + "..." : rawOutput) + '\'' +
               ", openPorts=" + (openPorts != null ? openPorts.size() : 0) +
               ", hostStatus='" + hostStatus + '\'' +
               ", latency='" + latency + '\'' +
               ", hostnames=" + (hostnames != null ? hostnames.size() : 0) +
               ", osGuess='" + osGuess + '\'' +
               ", uptime='" + uptime + '\'' +
               ", traceRouteHops=" + (traceRouteHops != null ? traceRouteHops.size() : 0) +
               ", timestamp=" + timestamp +
               ", scanDuration='" + scanDuration + '\'' +
               '}';
    }
}
