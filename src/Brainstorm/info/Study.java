package brainstorm.info;

import com.mathworks.matlab.types.Struct;
import javax.swing.JOptionPane;

/**
 * Clase para poder extraer informacion de un estudio
 * @author raco1
 */
public class Study {

    public int studyIndex;
    public Struct study;
    public String subject;

    public Study(int index, Struct study, String subject) {
        this.studyIndex = index;
        this.study = study;
        this.subject = subject;
    }

    public String nombreStudy() {
        try {
            String name = (String) this.study.get("Name");
            return name;
        } catch (Exception e) {
            return null;
        }

    }

    public String fileNombreStudy() {
        try {
            String name = (String) this.study.get("FileName");
            return name;
        } catch (Exception e) {
            return null;
        }
    }

    public String dataFileName() {
        try {
            Struct data = (Struct) this.study.get("Data");
            String name = (String) data.get("FileName");
            return name;
        } catch (Exception e) {
            return null;
        }
    }

    public String dataVideoFileName() {
        try {
            Struct data = (Struct) this.study.get("Image");
            String name = (String) data.get("FileName");
            return name;
        } catch (Exception e) {

            Struct[] data = (Struct[]) this.study.get("Image");
            String[] videoNames = new String[data.length];
            int aux = 0;
            for (Struct image : data) {
                videoNames[aux] = (String) image.get("FileName");
                aux += 1;
            }
            String seleccionada = mostrarSelector(videoNames);

            // Mostrar la opción seleccionada
            if (seleccionada != null) {
                System.out.println("Seleccionaste: " + seleccionada);
                return seleccionada;
            } else {
                System.out.println("No seleccionaste ninguna opción.");
                return null;
            }
        }
    }

    public boolean isVideo() {
        try {
            Struct data = (Struct) this.study.get("Image");
            if(data!=null){
                
                return true;
            }else{
                return false;
            }
        } catch (Exception e) {
            Struct[] data = (Struct[]) this.study.get("Image");
            if(data!=null){
                return true;
            }else{
                return false;
            }
        }
    }

    public String mostrarSelector(String[] opciones) {
        // Mostrar el JOptionPane con una lista de opciones
        return (String) JOptionPane.showInputDialog(
                null, // Ventana principal (null para que sea independiente)
                "What Video do you want to display:", // Mensaje
                "Video Selector", // Título de la ventana
                JOptionPane.QUESTION_MESSAGE, // Tipo de mensaje
                null, // Icono (null para usar el predeterminado)
                opciones, // Lista de opciones
                opciones[0] // Opción predeterminada (opcional)
        );
    }

    public String channelFileName() {
        try {
            Struct channel = (Struct) this.study.get("Channel");
            String name = (String) channel.get("FileName");
            return name;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return the studyIndex
     */
    public int getStudyIndex() {
        return studyIndex;
    }

    /**
     * @param studyIndex the studyIndex to set
     */
    public void setStudyIndex(int studyIndex) {
        this.studyIndex = studyIndex;
    }

    /**
     * @return the study
     */
    public Struct getStudy() {
        return study;
    }

    /**
     * @param study the study to set
     */
    public void setStudy(Struct study) {
        this.study = study;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }
}
