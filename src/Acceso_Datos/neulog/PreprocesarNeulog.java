package Acceso_Datos.neulog;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.util.Locale;

/**
 *
 * @author roman
 */
public class PreprocesarNeulog {

    public static String traerArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select NEULOG CSV file");

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            System.out.println("No se seleccionó ningún archivo. Saliendo...");
            return null;
        }

        File inputFile = fileChooser.getSelectedFile();
        String inputFilePath = inputFile.getAbsolutePath();

        // Verificar que el archivo tenga extensión .csv
        if (!inputFilePath.toLowerCase().endsWith(".csv")) {
            System.out.println("Archivo inválido: Solo se permiten archivos CSV.");
            return null;
        }

        String outputFilePath = inputFile.getParent() + File.separator + "file_neulog_GSR_BPM.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {

            // Eliminar las primeras 6 filas
            for (int i = 0; i < 6; i++) {
                if (br.readLine() == null) {
                    System.out.println("Archivo inválido: Menos de 6 filas.");
                    return null;
                }
            }

            // Leer la 7ma fila (cabecera original)
            String headerLine = br.readLine();
            if (headerLine == null) {
                System.out.println("Archivo inválido: No tiene 7 filas.");
                return null;
            }

            // Detectar el separador utilizado (punto y coma o coma)
            String inputDelimiter;
            if (headerLine.contains(";")) {
                inputDelimiter = ";";
            } else if (headerLine.contains(",")) {
                inputDelimiter = ",";
            } else {
                System.out.println("Archivo inválido: Delimitador no reconocido.");
                return null;
            }
            
            // Utilizar siempre coma para el archivo de salida
            String outputDelimiter = ",";

            // Separar la línea cabecera y validar que contenga 3 columnas
            String[] headerColumns = headerLine.split(Pattern.quote(inputDelimiter), -1);
            System.out.println(Arrays.toString(headerColumns));
            if (headerColumns.length != 3) {
                System.out.println("Archivo inválido: La 7ma fila no contiene 3 columnas.");
                return null;
            }

            // Validar que la primera columna sea "Time" y la segunda y tercera estén vacías
            if (!headerColumns[0].trim().equals("Time") ||
                !headerColumns[1].trim().isEmpty() ||
                !headerColumns[2].trim().isEmpty() ) {
                System.out.println("Archivo inválido: La 7ma fila no tiene el formato correcto.");
                return null;
            }

            // Escribir la cabecera renombrada en el archivo de salida usando el mismo separador
            bw.write("Time" + outputDelimiter + "BPM" + outputDelimiter + "µS");
            bw.newLine();

            // Procesar las líneas de datos restantes
            String line;
            double time = 0.0;
            while ((line = br.readLine()) != null) {
                // Se usa el delimitador de entrada para separar las columnas
                String[] columns = line.split(Pattern.quote(inputDelimiter), -1);
                // Se espera que cada línea tenga al menos 3 columnas
                if (columns.length < 3) {
                    continue; // O se puede manejar como error, según se requiera
                }
                // Al escribir, se usa outputDelimiter y se formatea time con Locale.US para asegurar punto decimal
                
                // Si ambas columnas de datos están vacías, se asume que no hay más datos válidos y se sale del bucle.
                if (columns[1].trim().isEmpty() && columns[2].trim().isEmpty()) {
                    break;
                }
                               
                bw.write(String.format(Locale.US, "%.1f", time) + outputDelimiter + columns[1].trim() + outputDelimiter + columns[2].trim());
                bw.newLine();
                time += 0.2;
            }

            System.out.println("Archivo procesado correctamente. Resultado en: " + outputFilePath);
            return outputFilePath;

        } catch (IOException e) {
            System.out.println("Ocurrió un error al procesar el archivo: " + e.getMessage());
            return null;
        }
    }

    public static String generarArchivoMarcadores(String ruta, String markerName, String emotivMarker) throws IOException {
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
            double doubleMarker = calcularDiferencia(emotivMarker);
            double secondTime = (double)numero + doubleMarker;
            if(doubleMarker==0){
                
                writer.write(String.format("%s, ", markerName) + numero + ", 0");
            }else{
                writer.write(String.format("%s, ", markerName) + numero + ", 0\n");
                writer.write(String.format("%s, ", markerName) + secondTime + ", 0");
            }
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
    
        public static double calcularDiferencia(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea1 = br.readLine();
            String linea2 = br.readLine();

            if (linea1 != null && linea2 != null) {
                String[] datos1 = linea1.split(",");
                String[] datos2 = linea2.split(",");

                if (datos1.length >= 2 && datos2.length >= 2) {
                    int valor1Columna1 = Integer.parseInt(datos1[0].trim());
                    int valor2Columna1 = Integer.parseInt(datos2[0].trim());
                    double valor1Columna2 = Double.parseDouble(datos1[1].trim());
                    double valor2Columna2 = Double.parseDouble(datos2[1].trim());

                    if (valor1Columna1 == valor2Columna1) {
                        return valor2Columna2 - valor1Columna2;
                    } else {
                        return 0;
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return 0; // Devuelve 0 si hay un error o si el archivo tiene menos de dos líneas.
    }
}

