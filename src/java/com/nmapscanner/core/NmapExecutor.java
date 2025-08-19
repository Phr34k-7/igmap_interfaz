package com.nmapscanner.core;

import com.nmapscanner.model.ScanResult; // Necesario para el Consumer<ScanResult>
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.function.Consumer; // Importar Consumer

public class NmapExecutor {
    private Process nmapProcess;
    private StringBuilder fullOutput = new StringBuilder();
    private Consumer<String> outputConsumer; // Para enviar líneas individuales
    private Consumer<ScanResult> scanResultConsumer; // Para enviar el resultado final del parseo
    private Consumer<String> errorConsumer; // Para enviar errores a la UI

    // Constructor que acepta los consumidores para la salida
    public NmapExecutor(Consumer<String> outputConsumer, Consumer<String> errorConsumer, Consumer<ScanResult> scanResultConsumer) {
        this.outputConsumer = outputConsumer;
        this.errorConsumer = errorConsumer;
        this.scanResultConsumer = scanResultConsumer;
    }

    // Método principal para ejecutar un comando Nmap
    public void executeNmapCommand(String target, String options, boolean useSudo) {
        String sudoPrefix = useSudo ? "sudo " : "";
        String command = sudoPrefix + "nmap " + options + " " + target;
        fullOutput.setLength(0); // Limpiar salida anterior

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            processBuilder.redirectErrorStream(true); // Redirigir stderr a stdout

            nmapProcess = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(nmapProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                fullOutput.append(line).append("\n");
                if (outputConsumer != null) {
                    outputConsumer.accept(line); // Enviar línea a la UI
                }
            }

            int exitCode = nmapProcess.waitFor();
            if (outputConsumer != null) {
                outputConsumer.accept("\nNmap finalizó con código de salida: " + exitCode + "\n");
            }
            
            // Aquí no parseamos ni enviamos el ScanResult, eso lo hará el SwingWorker en ScanInputPanel
            // Este método solo se encarga de ejecutar y recolectar la salida.

        } catch (IOException e) {
            String errorMessage = "Error al ejecutar Nmap. Asegúrate de que Nmap está instalado y en tu PATH.\n" + e.getMessage();
            if (errorConsumer != null) {
                errorConsumer.accept(errorMessage);
            }
            e.printStackTrace();
        } catch (InterruptedException e) {
            String errorMessage = "El proceso Nmap fue interrumpido: " + e.getMessage();
            if (errorConsumer != null) {
                errorConsumer.accept(errorMessage);
            }
            Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
        } finally {
            nmapProcess = null; // Limpiar la referencia al proceso
        }
    }

    // Método para detener el escaneo en curso
    public static void stopCurrentScan() {
        // Necesitamos una referencia al proceso actual para detenerlo.
        // Esto es complicado con una instancia simple, la mejor forma sería
        // pasar el proceso a una clase singleton o manejarlo en el SwingWorker.
        // Por ahora, para simplificar y que compile, si el proceso es accesible:
        // Idealmente, el SwingWorker en ScanInputPanel debería tener control directo sobre su proceso.
        // Como 'nmapProcess' no es estático, necesitamos una forma de acceder a él.
        // La implementación actual de ScanInputPanel asume un método estático.
        // Vamos a modificar ScanInputPanel para que detenga su propio proceso.
        // Por lo tanto, este método estático aquí realmente no se usará, pero lo dejo por si acaso.
        System.out.println("Método stopCurrentScan() llamado. La detención se maneja mejor en el SwingWorker.");
    }

    // Método para obtener la salida completa una vez finalizado el escaneo
    public String getFullOutput() {
        return fullOutput.toString();
    }
}
