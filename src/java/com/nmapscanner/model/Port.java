package com.nmapscanner.model;

public class Port {
    private int portNumber;
    private String protocol;
    private String state;
    private String service;
    private String version;
    private String info; 

    public Port(int portNumber, String protocol, String state, String service, String version, String info) {
        this.portNumber = portNumber;
        this.protocol = protocol;
        this.state = state;
        this.service = service;
        this.version = version;
        this.info = info; 
    }

    public Port(int portNumber, String protocol, String state, String service, String version) {
        this(portNumber, protocol, state, service, version, null); 
    }

    // Getters
    public int getPortNumber() {
        return portNumber;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getState() {
        return state;
    }

    public String getService() {
        return service;
    }

    public String getVersion() {
        return version;
    }

    public String getInfo() {
        return info;
    }

    // Setters
    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "Port{" +
               "portNumber=" + portNumber +
               ", protocol='" + protocol + '\'' +
               ", state='" + state + '\'' +
               ", service='" + service + '\'' +
               ", version='" + version + '\'' +
               ", info='" + info + '\'' + 
               '}';
    }
}
