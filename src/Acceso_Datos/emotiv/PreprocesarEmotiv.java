/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Acceso_Datos.emotiv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author raco1
 */
public class PreprocesarEmotiv {

    public static String traerArchivo() {

        try {

            String pythonScript = "./src/Logica/funciones_matlab/python/preprocessing.py"; // Ruta relativa al script
            List<String> command = new ArrayList<>();
            command.add("python"); // Comando para ejecutar Python
            command.add(pythonScript); // Ruta al script Python

            // Crear el proceso para ejecutar el script Python
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // Combinar salida estándar y errores
            Process process = pb.start();

            // Leer la salida del script Python
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            // Esperar a que el proceso termine
            int exitCode = process.waitFor();
            System.out.println("El script Python terminó con código de salida: " + exitCode);
            return "";

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
        public static String generarArchivoMarcadores(String inputCsv) throws IOException {
        // Obtener la carpeta padre del archivo CSV
        Path inputPath = Paths.get(inputCsv);
        Path parentDir = inputPath.getParent();
        String outputTxt = parentDir.resolve("output.txt").toString();

        // Abrir los archivos para lectura y escritura
        try (BufferedReader br = new BufferedReader(new FileReader(inputCsv));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputTxt))) {

            String line;
            boolean isHeader = true;

            // Leer el archivo CSV línea por línea
            while ((line = br.readLine()) != null) {
                // Dividir la línea en columnas
                String[] columns = line.split(",");

                // Si es la primera línea (header), encontrar los índices de las columnas necesarias
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // Asegurarse de que hay suficientes columnas
                if (columns.length < 2) continue;

                // Extraer las columnas necesarias
                String markerValueStr = columns[columns.length - 1]; // Última columna
                String timeStr = columns[0]; // Primera columna

                // Parsear valores
                int markerValue = Integer.parseInt(markerValueStr.trim());
                double time = Double.parseDouble(timeStr.trim());

                // Si MarkerValueInt no es 0, escribir en el archivo de salida
                if (markerValue != 0) {
                    bw.write(markerValue + ", " + time + ", 0\n");
                }
            }
        }

        // Retornar la ruta del archivo de salida
        return outputTxt;
    }

}
