/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package Logica.funciones_matlab.preprosygraficas;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.mathworks.engine.MatlabEngine;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
/**
 *
 * @author Usuario
 */
public class PreProsYGraficas {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Crear un nuevo marco (ventana)
        JFrame frame = new JFrame("Sincronizador");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Establecer el diseño del marco
        frame.setLayout(new FlowLayout());

        // Crear el primer botón
        JButton prepross = new JButton("Preprocesar archivos");
        prepross.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {  
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

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                JOptionPane.showMessageDialog(frame, "¡Botón Preprocesamiento clickeado!");
            }
               
        });

        // Crear el segundo botón
        JButton freqs = new JButton("Frecuencias (Powers + FFT)");
        freqs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                            // Seleccionar el primer archivo
                   JFileChooser fileChooser = new JFileChooser();
                   fileChooser.setDialogTitle("Selecciona el archivo powers CSV");
                   int result = fileChooser.showOpenDialog(null);
                   if (result != JFileChooser.APPROVE_OPTION) {
                       System.out.println("Operación cancelada.");
                       return;
                   }
                   String file1 = fileChooser.getSelectedFile().getAbsolutePath();

                   // Seleccionar el segundo archivo
                   fileChooser.setDialogTitle("Selecciona el archivo señales CSV");
                   result = fileChooser.showOpenDialog(null);
                   if (result != JFileChooser.APPROVE_OPTION) {
                       System.out.println("Operación cancelada.");
                       return;
                   }
                   String file2 = fileChooser.getSelectedFile().getAbsolutePath();

                   // Iniciar el motor de MATLAB
                   MatlabEngine eng = MatlabEngine.startMatlab();

                   // Añadir la carpeta del script al path de MATLAB
                   String scriptFolder = "./src/Logica/funciones_matlab/matlab" ;
                   eng.eval("addpath('"+scriptFolder+"');", null, null);

                   // Ejecutar la función MATLAB con los archivos seleccionados como argumentos
                    String matlabCommand = String.format("graphic_powers_and_FFT_optimized('%s', '%s');", 
                                       file1,
                                       file2);
                   eng.eval(matlabCommand, null, null);

                   // Mostrar mensaje de confirmación
                   System.out.println("El script MATLAB se ejecutó correctamente.");

                } catch (Exception ee) {
                    
                }
            }
        });
        JButton time_serie = new JButton("Series de tiempo (EEG, FC y RGP)");
        time_serie.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            try {
                // Iniciar la conexión con MATLAB
                System.out.println("Conectando con MATLAB...");
                MatlabEngine eng = MatlabEngine.startMatlab();
                
                String scriptFolder_signals = "./src/Logica/funciones_matlab/matlab/brainstorm" ;
                eng.eval("addpath('"+scriptFolder_signals+"');", null, null);
 
                String matlabCommand = String.format("graphic_time_series;");
                eng.eval(matlabCommand, null, null);
                   
                // Mostrar mensaje de confirmación
                System.out.println("El script MATLAB se ejecutó correctamente.");

            } catch (Exception eee) {
                eee.printStackTrace();
            }
        }
        });
        
        
        JButton heat_model = new JButton("Mapa de calor cerebral");
        heat_model.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            try {
                // Iniciar la conexión con MATLAB
                System.out.println("Conectando con MATLAB...");
                MatlabEngine eng = MatlabEngine.startMatlab();
                
                String scriptFolder_signals = "./src/Logica/funciones_matlab/matlab/brainstorm" ;
                eng.eval("addpath('"+scriptFolder_signals+"');", null, null);
 
                String matlabCommand = String.format("heat_map;");
                eng.eval(matlabCommand, null, null);
                   
                // Mostrar mensaje de confirmación
                System.out.println("El script MATLAB se ejecutó correctamente.");

            } catch (Exception eee) {
                eee.printStackTrace();
            }
        }
        });
        // Agregar los botones al marco
        frame.add(prepross);
        frame.add(freqs);
        frame.add(time_serie);
        frame.add(heat_model);
        
        // Establecer la visibilidad del marco
        frame.setVisible(true);
    }
}

