/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Acceso_Datos;

import com.mathworks.engine.MatlabEngine;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author raco1
 */
public class MainData {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
             try {
            // Iniciar el motor de MATLAB
            System.out.println("Iniciando MATLAB...");
            MatlabEngine matlabEngine = MatlabEngine.startMatlab();
            System.out.println("MATLAB iniciado con éxito.");

            // Crear figuras en MATLAB
            System.out.println("Creando figuras...");
            matlabEngine.eval("fig1 = figure('Name', 'Figura 1'); plot(rand(10,1));");
            matlabEngine.eval("fig2 = figure('Name', 'Figura 2'); plot(sin(1:0.1:10));");
            matlabEngine.eval("fig3 = figure('Name', 'Figura 3'); plot(cos(1:0.1:10));");
            matlabEngine.eval("fig4 = figure('Name', 'Figura 4'); plot(tan(1:0.1:10));");

            // Pasar las figuras a la función createEmbeddedFigures
            System.out.println("Creando figura embebida...");
            matlabEngine.feval("createEmbeddedFigures(fig1, fig2, fig3, fig4)");
            System.out.println("Figura embebida creada correctamente.");

            // Mantener MATLAB abierto hasta presionar Enter
            System.out.println("Presiona Enter para cerrar...");
            System.in.read();

            // Cerrar el motor de MATLAB
            matlabEngine.close();
            System.out.println("Motor de MATLAB cerrado.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
