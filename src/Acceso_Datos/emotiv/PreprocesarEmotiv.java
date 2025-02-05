/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Acceso_Datos.emotiv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.swing.JFileChooser;

/**
 * Esta clase implementa el procesamiento de un archivo CSV de entrada para
 * generar dos archivos de salida: - Uno de señales (con sufijo _SIGNALS.csv),
 * en el que se renombra y normaliza la columna de tiempo, se eliminan prefijos
 * en las columnas EEG y se procesan los marcadores. - Uno de potencias (con
 * sufijo _POWERS.csv), en el que se calcula el promedio de las bandas Theta,
 * Alpha, BetaL, BetaH y Gamma en intervalos de 0.125 segundos.
 *
 * Se utiliza Apache Commons CSV para leer y escribir los archivos CSV.
 */
/**
 *
 * @author roman
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
                if (columns.length < 2) {
                    continue;
                }

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

    /**
     * Función generateFiles:
     *
     * Procesa un archivo CSV de entrada (filePath) y genera dos archivos de
     * salida: - nombre_SIGNALS.csv - nombre_POWERS.csv
     *
     * @param filePath ruta del archivo CSV de entrada.
     * @return una lista con las rutas de los archivos generados.
     * @throws IOException si ocurre algún error de I/O.
     */
    public static List<String> generateFiles() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona el archivo CSV");

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            System.out.println("No se seleccionó ningún archivo. Saliendo...");
            return null;
        }

        File inputFile = fileChooser.getSelectedFile();
        String filePath = inputFile.getAbsolutePath();
        // -------------------------------------------------------------------------
        // 1. Definir la lista de columnas de interés.
        List<String> columnsInterest = Arrays.asList(
                "Timestamp",
                "EEG.AF3", "EEG.F7", "EEG.F3", "EEG.FC5", "EEG.T7", "EEG.P7", "EEG.O1", "EEG.O2", "EEG.P8", "EEG.T8", "EEG.FC6", "EEG.F4", "EEG.F8", "EEG.AF4",
                "MarkerIndex", "MarkerType", "MarkerValueInt",
                "POW.AF3.Theta", "POW.AF3.Alpha", "POW.AF3.BetaL", "POW.AF3.BetaH", "POW.AF3.Gamma",
                "POW.F7.Theta", "POW.F7.Alpha", "POW.F7.BetaL", "POW.F7.BetaH", "POW.F7.Gamma",
                "POW.F3.Theta", "POW.F3.Alpha", "POW.F3.BetaL", "POW.F3.BetaH", "POW.F3.Gamma",
                "POW.FC5.Theta", "POW.FC5.Alpha", "POW.FC5.BetaL", "POW.FC5.BetaH", "POW.FC5.Gamma",
                "POW.T7.Theta", "POW.T7.Alpha", "POW.T7.BetaL", "POW.T7.BetaH", "POW.T7.Gamma",
                "POW.P7.Theta", "POW.P7.Alpha", "POW.P7.BetaL", "POW.P7.BetaH", "POW.P7.Gamma",
                "POW.O1.Theta", "POW.O1.Alpha", "POW.O1.BetaL", "POW.O1.BetaH", "POW.O1.Gamma",
                "POW.O2.Theta", "POW.O2.Alpha", "POW.O2.BetaL", "POW.O2.BetaH", "POW.O2.Gamma",
                "POW.P8.Theta", "POW.P8.Alpha", "POW.P8.BetaL", "POW.P8.BetaH", "POW.P8.Gamma",
                "POW.T8.Theta", "POW.T8.Alpha", "POW.T8.BetaL", "POW.T8.BetaH", "POW.T8.Gamma",
                "POW.FC6.Theta", "POW.FC6.Alpha", "POW.FC6.BetaL", "POW.FC6.BetaH", "POW.FC6.Gamma",
                "POW.F4.Theta", "POW.F4.Alpha", "POW.F4.BetaL", "POW.F4.BetaH", "POW.F4.Gamma",
                "POW.F8.Theta", "POW.F8.Alpha", "POW.F8.BetaL", "POW.F8.BetaH", "POW.F8.Gamma",
                "POW.AF4.Theta", "POW.AF4.Alpha", "POW.AF4.BetaL", "POW.AF4.BetaH", "POW.AF4.Gamma"
        );

        // -------------------------------------------------------------------------
        // 2. Detección del delimitador del archivo CSV:
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        br.readLine(); // Saltar la primera línea de metadata.
        char[] sampleBuffer = new char[1024];
        int numChars = br.read(sampleBuffer, 0, 1024);
        String sample = new String(sampleBuffer, 0, numChars);
        char delimiter = detectDelimiter(sample);
        br.close();

        // -------------------------------------------------------------------------
        // 3. Leer el CSV (omitiendo la línea de metadata) usando Apache Commons CSV.
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        reader.readLine(); // Saltar la primera línea (metadata)
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(delimiter).withFirstRecordAsHeader();
        CSVParser parser = new CSVParser(reader, format);
        Map<String, Integer> headerMap = parser.getHeaderMap();

        // -------------------------------------------------------------------------
        // 4. Verificar que el CSV contiene todas las columnas de interés.
        if (!headerMap.keySet().containsAll(columnsInterest)) {
            System.out.println("No se puede procesar: el archivo CSV no contiene todas las columnas requeridas.");
            parser.close();
            reader.close();
            return null;
        }

        // Leer todos los registros
        List<CSVRecord> records = parser.getRecords();
        parser.close();
        reader.close();

        // -------------------------------------------------------------------------
        // 5. Separar columnas en dos grupos:
        // - columnsSignals: aquellas sin "POW"
        // - powersColumns: columnas base ("Timestamp", "MarkerIndex", "MarkerType", "MarkerValueInt") más las que contienen "POW"
        List<String> columnsSignals = new ArrayList<>();
        List<String> powersColumns = new ArrayList<>();
        // Columnas base para potencias:
        powersColumns.add("Timestamp");
        powersColumns.add("MarkerIndex");
        powersColumns.add("MarkerType");
        powersColumns.add("MarkerValueInt");

        for (String col : columnsInterest) {
            if (col.contains("POW")) {
                if (!powersColumns.contains(col)) {
                    powersColumns.add(col);
                }
            } else {
                columnsSignals.add(col);
            }
        }

        // Extraer dos "dataframes" (representados aquí como Listas de Map)
        List<Map<String, String>> dfSignals = new ArrayList<>();
        List<Map<String, String>> dfPowers = new ArrayList<>();

        for (CSVRecord record : records) {
            // Para señales
            Map<String, String> signalRow = new LinkedHashMap<>();
            for (String col : columnsSignals) {
                signalRow.put(col, record.get(col));
            }
            dfSignals.add(signalRow);

            // Para potencias
            Map<String, String> powerRow = new LinkedHashMap<>();
            for (String col : powersColumns) {
                powerRow.put(col, record.get(col));
            }
            dfPowers.add(powerRow);
        }

        // -------------------------------------------------------------------------
        // 6. Procesar el DataFrame de señales (dfSignals):
        // a) Renombrar columnas: quitar el prefijo "EEG." y cambiar "Timestamp" a "Time".
        for (Map<String, String> row : dfSignals) {
            // Quitar prefijo "EEG."
            List<String> keys = new ArrayList<>(row.keySet());
            for (String key : keys) {
                if (key.startsWith("EEG.")) {
                    String newKey = key.replace("EEG.", "");
                    String value = row.remove(key);
                    row.put(newKey, value);
                }
            }
            // Renombrar "Timestamp" a "Time"
            if (row.containsKey("Timestamp")) {
                String value = row.remove("Timestamp");
                row.put("Time", value);
            }
        }

        // b) Corregir el primer valor de "Time": eliminar comas y puntos y convertir a entero.
        if (dfSignals.size() == 0) {
            System.out.println("No hay datos en el CSV.");
            return null;
        }
        Map<String, String> firstRow = dfSignals.get(0);
        String timeStr = firstRow.get("Time");
        timeStr = timeStr.replace(",", "").replace(".", "");
        long initialTimeUniversal = Long.parseLong(timeStr);
        double initialTimeSec = initialTimeUniversal / 1e6;

        // c) Procesar la columna "Time" para todas las filas:
        for (Map<String, String> row : dfSignals) {
            String tStr = row.get("Time");
            tStr = tStr.replace(".", "").replace(",", "");
            long tVal = Long.parseLong(tStr);
            double normalized = (tVal / 1e6) - initialTimeSec;
            normalized = Math.round(normalized * 1000.0) / 1000.0; // redondear a 3 decimales
            row.put("Time", String.valueOf(normalized));
        }

        // d) Rellenar valores faltantes en las columnas de markers y convertir a entero.
        for (Map<String, String> row : dfSignals) {
            String[] markers = {"MarkerIndex", "MarkerType", "MarkerValueInt"};
            for (String marker : markers) {
                String val = row.get(marker);
                if (val == null || val.trim().isEmpty()) {
                    row.put(marker, "0");
                } else {
                    try {
                        int intVal = Integer.parseInt(val);
                        row.put(marker, String.valueOf(intVal));
                    } catch (NumberFormatException e) {
                        row.put(marker, "0");
                    }
                }
            }
        }

        // e) Para todas las columnas (excepto la primera "Time"), eliminar puntos y comas y convertir a entero.
        for (Map<String, String> row : dfSignals) {
            for (Map.Entry<String, String> entry : row.entrySet()) {
                String key = entry.getKey();
                if (!key.equals("Time")) {
                    String val = entry.getValue();
                    if (val == null) {
                        val = "0";
                    }
                    val = val.replace(".", "").replace(",", "");
                    try {
                        long intVal = Long.parseLong(val);
                        row.put(key, String.valueOf(intVal));
                    } catch (NumberFormatException e) {
                        row.put(key, "0");
                    }
                }
            }
        }

        // -------------------------------------------------------------------------
        // Reordenar las columnas de dfSignals para que queden en el orden deseado:
        // Time, AF3, F7, F3, FC5, T7, P7, O1, O2, P8, T8, FC6, F4, F8, AF4, MarkerIndex, MarkerType, MarkerValueInt
        List<Map<String, String>> orderedDfSignals = new ArrayList<>();
        List<String> desiredOrder = Arrays.asList(
                "Time", "AF3", "F7", "F3", "FC5", "T7", "P7", "O1", "O2", "P8",
                "T8", "FC6", "F4", "F8", "AF4", "MarkerIndex", "MarkerType", "MarkerValueInt"
        );
        for (Map<String, String> row : dfSignals) {
            Map<String, String> newRow = new LinkedHashMap<>();
            for (String col : desiredOrder) {
                newRow.put(col, row.get(col));
            }
            orderedDfSignals.add(newRow);
        }

        // f) Se guarda el DataFrame final de señales en un archivo CSV con sufijo "_SIGNALS.csv".
        String signalPath = filePath.replace(".csv", "") + "_SIGNALS.csv";
        writeCSV(signalPath, orderedDfSignals);

        // -------------------------------------------------------------------------
        // 7. Procesar el DataFrame de potencias (dfPowers):
        // a) Renombrar "Timestamp" a "Time".
        for (Map<String, String> row : dfPowers) {
            if (row.containsKey("Timestamp")) {
                String value = row.remove("Timestamp");
                row.put("Time", value);
            }
        }

        // b) Procesar la columna "Time": eliminar puntos y comas y convertir a entero.
        for (Map<String, String> row : dfPowers) {
            String tStr = row.get("Time");
            tStr = tStr.replace(".", "").replace(",", "");
            long tVal = Long.parseLong(tStr);
            row.put("Time", String.valueOf(tVal));
        }

        // c) Rellenar valores faltantes en las columnas de markers y convertir a entero.
        for (Map<String, String> row : dfPowers) {
            String[] markers = {"MarkerIndex", "MarkerType", "MarkerValueInt"};
            for (String marker : markers) {
                String val = row.get(marker);
                if (val == null || val.trim().isEmpty()) {
                    row.put(marker, "0");
                } else {
                    try {
                        int intVal = Integer.parseInt(val);
                        row.put(marker, String.valueOf(intVal));
                    } catch (NumberFormatException e) {
                        row.put(marker, "0");
                    }
                }
            }
        }

        // d) Eliminar filas que contengan valores faltantes.
        Iterator<Map<String, String>> iter = dfPowers.iterator();
        while (iter.hasNext()) {
            Map<String, String> row = iter.next();
            boolean hasMissing = false;
            for (String key : row.keySet()) {
                if (row.get(key) == null || row.get(key).trim().isEmpty()) {
                    hasMissing = true;
                    break;
                }
            }
            if (hasMissing) {
                iter.remove();
            }
        }

        // e) Para todas las columnas a partir de la quinta (índice 4) (las columnas de potencias),
        // eliminar puntos y comas y convertir a entero.
        for (Map<String, String> row : dfPowers) {
            for (int i = 4; i < powersColumns.size(); i++) {
                String col = powersColumns.get(i);
                String val = row.get(col);
                if (val == null) {
                    val = "0";
                }
                val = val.replace(".", "").replace(",", "");
                try {
                    long intVal = Long.parseLong(val);
                    row.put(col, String.valueOf(intVal));
                } catch (NumberFormatException e) {
                    row.put(col, "0");
                }
            }
        }

        // f) Normalizar la columna "Time" de la misma forma que en dfSignals.
        for (Map<String, String> row : dfPowers) {
            String tStr = row.get("Time");
            long tVal = Long.parseLong(tStr);
            double normalized = (tVal / 1e6) - initialTimeSec;
            normalized = Math.round(normalized * 1000.0) / 1000.0;
            row.put("Time", String.valueOf(normalized));
        }

        // -------------------------------------------------------------------------
        // 8. Separar las columnas de potencias en “dataframes” para cada banda:
        int numRows = dfPowers.size();
        List<List<Double>> thetaData = new ArrayList<>();
        List<List<Double>> alphaData = new ArrayList<>();
        List<List<Double>> betaLData = new ArrayList<>();
        List<List<Double>> betaHData = new ArrayList<>();
        List<List<Double>> gammaData = new ArrayList<>();

        for (int i = 0; i < numRows; i++) {
            thetaData.add(new ArrayList<>());
            alphaData.add(new ArrayList<>());
            betaLData.add(new ArrayList<>());
            betaHData.add(new ArrayList<>());
            gammaData.add(new ArrayList<>());
        }

        // Se recorren las columnas (a partir de la quinta, es decir, las que contienen "POW")
        for (int i = 4; i < powersColumns.size(); i++) {
            String col = powersColumns.get(i);
            for (int r = 0; r < numRows; r++) {
                Map<String, String> row = dfPowers.get(r);
                String valStr = row.get(col);
                double val = 0;
                try {
                    val = Double.parseDouble(valStr);
                } catch (NumberFormatException e) {
                    val = 0;
                }
                if (col.contains("Theta")) {
                    thetaData.get(r).add(val);
                } else if (col.contains("Alpha")) {
                    alphaData.get(r).add(val);
                } else if (col.contains("BetaL")) {
                    betaLData.get(r).add(val);
                } else if (col.contains("BetaH")) {
                    betaHData.get(r).add(val);
                } else if (col.contains("Gamma")) {
                    gammaData.get(r).add(val);
                }
            }
        }

        // -------------------------------------------------------------------------
        // 9. Calcular las medias de cada banda para cada fila, redondeadas a 2 decimales.
        List<Double> thetaMeans = new ArrayList<>();
        List<Double> alphaMeans = new ArrayList<>();
        List<Double> betaLMeans = new ArrayList<>();
        List<Double> betaHMeans = new ArrayList<>();
        List<Double> gammaMeans = new ArrayList<>();

        for (int i = 0; i < numRows; i++) {
            thetaMeans.add(round(mean(thetaData.get(i)), 2));
            alphaMeans.add(round(mean(alphaData.get(i)), 2));
            betaLMeans.add(round(mean(betaLData.get(i)), 2));
            betaHMeans.add(round(mean(betaHData.get(i)), 2));
            gammaMeans.add(round(mean(gammaData.get(i)), 2));
        }

        // -------------------------------------------------------------------------
        // 10. Preparar arreglos de tiempo y de medias por intervalo.
        int rowsCount = numRows;
        double[] thetaPerInterval = new double[rowsCount];
        double[] alphaPerInterval = new double[rowsCount];
        double[] betaLPerInterval = new double[rowsCount];
        double[] betaHPerInterval = new double[rowsCount];
        double[] gammaPerInterval = new double[rowsCount];
        double timePerInterval = 0.125;
        double[] times = new double[rowsCount];

        for (int i = 0; i < rowsCount; i++) {
            times[i] = timePerInterval * (i + 1);
            thetaPerInterval[i] = thetaMeans.get(i);
            alphaPerInterval[i] = alphaMeans.get(i);
            betaLPerInterval[i] = betaLMeans.get(i);
            betaHPerInterval[i] = betaHMeans.get(i);
            gammaPerInterval[i] = gammaMeans.get(i);
        }

        // -------------------------------------------------------------------------
        // 11. Crear y guardar el DataFrame final de potencias medias.
        String powerMeanedPath = filePath.replace(".csv", "") + "_POWERS.csv";
        List<Map<String, String>> df2 = new ArrayList<>();
        for (int i = 0; i < rowsCount; i++) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("Time", String.valueOf(times[i]));
            row.put("Theta", String.valueOf(thetaPerInterval[i]));
            row.put("Alpha", String.valueOf(alphaPerInterval[i]));
            row.put("BetaL", String.valueOf(betaLPerInterval[i]));
            row.put("BetaH", String.valueOf(betaHPerInterval[i]));
            row.put("Gamma", String.valueOf(gammaPerInterval[i]));
            df2.add(row);
        }
        writeCSV(powerMeanedPath, df2);

        // Retornar las rutas de los archivos generados.
        List<String> outputPaths = new ArrayList<>();
        outputPaths.add(signalPath);
        outputPaths.add(powerMeanedPath);
        return outputPaths;
    }

    /**
     * Método auxiliar para detectar el delimitador del CSV a partir de una
     * muestra.
     *
     * @param sample Cadena con parte del contenido del CSV.
     * @return El carácter que se detecta como delimitador.
     */
    private static char detectDelimiter(String sample) {
        char[] candidates = {',', ';', '\t', '|'};
        char best = ',';
        int maxCount = 0;
        for (char delim : candidates) {
            int count = 0;
            for (int i = 0; i < sample.length(); i++) {
                if (sample.charAt(i) == delim) {
                    count++;
                }
            }
            if (count > maxCount) {
                maxCount = count;
                best = delim;
            }
        }
        return best;
    }

    /**
     * Método auxiliar para escribir un archivo CSV a partir de una lista de
     * filas (cada fila es un Map).
     *
     * @param filePath Ruta del archivo a generar.
     * @param data Lista de filas.
     * @throws IOException en caso de error de escritura.
     */
    private static void writeCSV(String filePath, List<Map<String, String>> data) throws IOException {
        if (data == null || data.isEmpty()) {
            return;
        }
        // Obtener el header a partir de las claves de la primera fila.
        List<String> headers = new ArrayList<>(data.get(0).keySet());
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[0])));
        for (Map<String, String> row : data) {
            List<String> rowData = new ArrayList<>();
            for (String header : headers) {
                rowData.add(row.get(header));
            }
            csvPrinter.printRecord(rowData);
        }
        csvPrinter.flush();
        csvPrinter.close();
        writer.close();
    }

    /**
     * Calcula el promedio de una lista de Double.
     *
     * @param list Lista de valores.
     * @return Promedio de los valores (o 0 si la lista está vacía).
     */
    private static double mean(List<Double> list) {
        if (list == null || list.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (double d : list) {
            sum += d;
        }
        return sum / list.size();
    }

    /**
     * Redondea un valor double a la cantidad de decimales indicados.
     *
     * @param value Valor a redondear.
     * @param places Número de decimales.
     * @return Valor redondeado.
     */
    private static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
