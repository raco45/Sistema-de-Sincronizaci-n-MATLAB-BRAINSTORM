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
        fileChooser.setDialogTitle("Select CSV file");

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            System.out.println("No se seleccionó ningún archivo. Saliendo...");
            return null;
        }

        File inputFile = fileChooser.getSelectedFile();
        String inputFilePath = inputFile.getAbsolutePath();
        String outputFilePath = inputFile.getParent() + File.separator + "file_neulog_GSR_BPM.csv";

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

    public static String generarArchivoMarcadores(String ruta, String markerName) throws IOException {
        // Pedir al usuario que ingrese un número
        int numero = 0;
        while (true) {
            String input="";
            String lastNumber = getLastTimeValue(ruta);
            try{
                input = JOptionPane.showInputDialog("Add start time (seconds):");
                
            }catch(Exception e){
                continue;
            }

            // Validar que el usuario haya ingresado un número
            if (input == null || input.isEmpty() || !input.matches("\\d+")) {
                JOptionPane.showMessageDialog(null, "Not a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            double ultimoNumero = Double.parseDouble(lastNumber);
            numero = Integer.parseInt(input);

            if (numero >= ultimoNumero) {
                JOptionPane.showMessageDialog(null, "You can't input a time bigger than the recording time", "Error", JOptionPane.ERROR_MESSAGE);
            }else if(numero<=0){
                JOptionPane.showMessageDialog(null, "You can't input a time lower or equal to zero", "Error", JOptionPane.ERROR_MESSAGE);
            }else{
                break;
            }
        }

        try {
            // Eliminar la parte del nombre del archivo de la ruta
            File archivo = new File(ruta);
            String rutaSinArchivo = archivo.getParent();

            // Crear el archivo con el formato especificado
            String nombreArchivo = "marker_file_" + numero + ".txt";
            String rutaCompleta = rutaSinArchivo + "\\" + nombreArchivo; // Usar "\" para Windows

            FileWriter writer = new FileWriter(rutaCompleta);
            writer.write(String.format("%s, ", markerName) + numero + ", 0");
            writer.close();

            return rutaCompleta; // Retornar la ruta completa del archivo generado

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to generate File: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static String getLastTimeValue(String filePath) throws IOException {
        String lastTimeValue = null;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Saltar la primera línea (encabezados) si existe
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > 0) {
                    lastTimeValue = values[0];
                }
            }
        }

        return lastTimeValue;
    }
}
