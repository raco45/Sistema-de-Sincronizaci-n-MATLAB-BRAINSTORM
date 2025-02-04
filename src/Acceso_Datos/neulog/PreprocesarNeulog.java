package Acceso_Datos.neulog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

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

    public static String generarArchivoMarcadores(String ruta) {
        // Pedir al usuario que ingrese un número
        String input = JOptionPane.showInputDialog("Ingrese un número:");

        // Validar que el usuario haya ingresado un número
        if (input == null || input.isEmpty() || !input.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "Debe ingresar un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        int numero = Integer.parseInt(input);

        try {
            // Eliminar la parte del nombre del archivo de la ruta
            File archivo = new File(ruta);
            String rutaSinArchivo = archivo.getParent();

            // Crear el archivo con el formato especificado
            String nombreArchivo = "archivo_" + numero + ".txt";
            String rutaCompleta = rutaSinArchivo + "\\" + nombreArchivo; // Usar "\" para Windows

            FileWriter writer = new FileWriter(rutaCompleta);
            writer.write("100, " + numero + ", 0");
            writer.close();

            return rutaCompleta; // Retornar la ruta completa del archivo generado

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al generar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
