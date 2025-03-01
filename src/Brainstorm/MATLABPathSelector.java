package brainstorm;

import javax.swing.*;
import java.io.File;

public class MATLABPathSelector {

    public static String selectMATLABPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Carpeta de MATLAB");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new File("C:\\Program Files\\MATLAB"));

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            String path = selectedDir.getAbsolutePath();
            if (new File(path + "\\bin\\matlab.exe").exists()) {
                return path;
            } else {
                JOptionPane.showMessageDialog(null, "Ruta inválida: No se encontró matlab.exe", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }
}
