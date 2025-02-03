package Acceso_Datos.neulog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;

/**
 *
 * @author raco1
 */
public class PreprocesarNeulog {

    public static String traerArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona el archivo CSV");

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            System.out.println("No se seleccionó ningún archivo. Saliendo...");
            return null;
        }

        File inputFile = fileChooser.getSelectedFile();
        String inputFilePath = inputFile.getAbsolutePath();
        String outputFilePath = inputFile.getParent() + File.separator + "archivo_filtrado_neulog.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
                BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            boolean headerFound = false;

            double time = (double) 0;

            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");

                try {
                    // Verifica si la línea tiene los encabezados requeridos
                    if (columns[0].trim().equalsIgnoreCase("Time")) {

                        // Escribe los encabezados en el archivo de salida
                        bw.write("Time,BPM,µS");
                        bw.newLine();
                        headerFound = true;
                    } else if (headerFound) {
                        // Escribe los datos debajo de los encabezados
                        bw.write(String.valueOf(time) + "," + columns[1].trim() + "," + columns[2].trim());
                        time += 0.2;
                        bw.newLine();
                    }
                } catch (Exception e) {
                    break;
                }

            }

            System.out.println("Archivo procesado correctamente. Resultado en: " + outputFilePath);
            return outputFilePath;

        } catch (IOException e) {
            System.out.println("Ocurrió un error al procesar el archivo: " + e.getMessage());
            return null;
        }
    }

}
