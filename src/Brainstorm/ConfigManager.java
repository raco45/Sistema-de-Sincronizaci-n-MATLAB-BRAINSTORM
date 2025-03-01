package brainstorm;

import java.io.*;
import java.util.Properties;

public class ConfigManager {

    private static final String CONFIG_FILE = "matlab_config.properties";

    public static void savePath(String path) {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            Properties props = new Properties();
            props.setProperty("matlab.path", path);
            props.store(writer, "Configuración de MATLAB");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadPath() {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Properties props = new Properties();
            props.load(reader);
            return props.getProperty("matlab.path");
        } catch (IOException e) {
            return null;
        }
    }
}
