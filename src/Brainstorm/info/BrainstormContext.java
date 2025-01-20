package brainstorm.info;

import com.mathworks.engine.MatlabEngine;
import com.mathworks.matlab.types.Struct;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class BrainstormContext {

    private MatlabEngine eng;
    private Boolean isBrainstorm = false;
    public Protocolo protocol;
    public Subject subject;
    public Study study;

    public BrainstormContext(MatlabEngine eng) {
        this.eng = eng;
    }

    //Iniciar la libreria Brainstorm
    public void startBrainstorm() {
        try {
            eng.eval("brainstorm nogui;", null, null);
            this.setIsBrainstorm(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Ruta del directorio de Brainstorm
    public String homeDirectory() {
        try {
            eng.eval("homeDirectory=bst_get('BrainstormDbDir')");
            String ruta = (String) eng.getVariable("homeDirectory");
            System.out.println(ruta);
            return ruta;
        } catch (Exception e) {
            return "";
        }
    }

    //Se encarga de colocar y cargar el nuevo protocolo elegido
    public void setProtocol(double index) {
        try {
            eng.eval(String.format("bst_set('iProtocol',%s)", index));
            eng.eval(String.format("db_load_protocol(%s)", index));
            eng.eval("infoProtocol=bst_get('ProtocolInfo')");
            Struct protocolo = eng.getVariable("infoProtocol");
            Protocolo protocol = new Protocolo((int) index, protocolo);
            this.setProtocol(protocol);
            System.out.println("Working");
        } catch (Exception e) {
            System.out.println("Not working");
        }
    }

    //Se encarga de colocar un sujeto seleccionado
    public void setSubject(int iSubject) {
        try {
            if (iSubject > 0) {
                eng.eval(String.format("sSubject=bst_get('Subject', %s)", iSubject));
                Struct sSubject = (Struct) eng.getVariable("sSubject");
                String protocolo = this.currentProtocolName();
                Subject sujeto = new Subject(iSubject, sSubject, protocolo);
                this.setSubject(sujeto);
                eng.eval(String.format("bst_set('Subject',%s,sSubject)", iSubject));
            }else{
                this.setSubject(null);
            }

//            eng.eval("protocolSubject=bst_get('ProtocolSubjects')");
//            Struct sujetos = (Struct) eng.getVariable("protocolSubject");
        } catch (IllegalStateException | InterruptedException | ExecutionException e) {
            System.out.println("Not working, setSubject");
        }
    }

    //Se encarga de cargar el nuevo protocolo
    public void loadProtocol() {
        try {
            eng.eval("loaded=bst_get('isProtocolLoaded')");
            double loaded = (double) eng.getVariable("loaded");
            System.out.println("loaded" + loaded);
        } catch (Exception e) {

        }
    }

    // Lista de protocolos
    public String[] protocolList() {
        // Especifica la ruta del directorio que deseas explorar
        String rutaDirectorio = this.homeDirectory(); // Cambia esto por la ruta deseada
        File directorio = new File(rutaDirectorio);

        // Verifica si la ruta es un directorio
        if (directorio.isDirectory()) {
            // Lista los nombres de las carpetas dentro del directorio
            String[] carpetas = directorio.list((current, name) -> new File(current, name).isDirectory());
            return carpetas;
        } else {
            System.out.println("La ruta especificada no es un directorio.");
            return null;
        }
    }

    // Indice de un protocolo por su nombre, retorna 0 si no hay datos del protocolo
    public double protocolIndex(String protocolName) {
        try {
            eng.eval(String.format("indexProtocol=bst_get('Protocol','%s')", protocolName));
            double indice = (double) eng.getVariable("indexProtocol");

            System.out.println("El indice de " + protocolName + " es " + indice);
            return indice;
        } catch (Exception e) {
            System.out.println("Vacio");
            return 0;
        }
    }

    // Informacion del protocolo cargado
    public double currentProtocolIndex() {
        try {
            eng.eval("indexProtocol=bst_get('iProtocol')");
            double indice = (double) eng.getVariable("indexProtocol");
            System.out.println("Indice del protocolo: " + indice);
            return indice;
        } catch (IllegalStateException | InterruptedException | ExecutionException e) {
            return 0;
        }
    }

    public String currentProtocolName() {
        try {
            eng.eval("infoProtocol=bst_get('ProtocolInfo')");
            Struct protocolo = eng.getVariable("infoProtocol");
            String nombreProtocolo = (String) protocolo.get("Comment"); //Nombre del protocolo cargado
            return nombreProtocolo;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String currentSujectName() {
        try {
            eng.eval("currentSubject=bst_get('Subject')");
            Struct sujeto = eng.getVariable("currentSubject");
            String name = (String) sujeto.get("Name");
            return name;
        } catch (Exception e) {
//            e.printStackTrace();
            return "";
        }
    }

    // Da una lista con el nombre del estudio/archivo cargado en el protocolo actual
    public String currentStudyName() {
        try {
            eng.eval("currentStudy=bst_get('Study')");
            Struct study = eng.getVariable("currentStudy");
            String name = (String) study.get("Name");
            System.out.println(name.replaceAll("@raw", ""));
            return name.replaceAll("@raw", "");

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // Da una lista con los nombres de los estudios dentro de un protocolo
    public Struct[] protocolStudies() {
        try {
            eng.eval("infoStudies=bst_get('ProtocolStudies')");
            Struct infoEstudios = (Struct) eng.getVariable("infoStudies");
            Struct[] detalleEstudios = (Struct[]) infoEstudios.get("Study"); // Array de estudios
            eng.eval("estudios=bst_get('StudyCount')");

            return detalleEstudios;
        } catch (Exception e) {
            Struct[] lista = {};
            e.printStackTrace();
            return lista;
        }
    }

    // Nos entrega un arrayList con los estudios que pertenecen a un sujeto en especifico
    public ArrayList subjectStudies(String subject) {
        try {
            ArrayList estudios = new ArrayList();
            Struct[] protocolStudies = this.protocolStudies();
            int aux = 1;
            String nameAux = "";
            for (Struct estudio : protocolStudies) {
                nameAux = (String) estudio.get("Name");
                String nameSubject = (String) estudio.get("BrainStormSubject");
                if (nameSubject.contains(subject)) {
                    Study subjectStudy = new Study(aux, estudio, subject);
                    estudios.add(subjectStudy);
                    System.out.println(nameAux);
                    System.out.println(nameSubject);
                    System.out.println(aux);
                }
                aux += 1;
            }
            return estudios;
        } catch (Exception e) {
            return null;
        }
    }
    // Nos entrega un arrayList con los estudios que pertenecen a un sujeto en especifico
    public String[] subjectStudiesArray(String subject) {
        try {
            int capa = this.subjectStudies(subject).size();
            String[] estudios = new String[(int) capa];
            Struct[] protocolStudies = this.protocolStudies();
            int aux = 0;
            String nameAux = "";
            for (Struct estudio : protocolStudies) {
                nameAux = (String) estudio.get("Name");
                String nameSubject = (String) estudio.get("BrainStormSubject");
                if (nameSubject.contains(subject)) {
                    estudios[aux] = nameAux;
                    System.out.println(nameAux);
                    System.out.println(nameSubject);
                    System.out.println(aux);
                    aux += 1;
                }
            }
            return estudios;
        } catch (Exception e) {
            return null;
        }
    }

    
    // Recibe un indice y nos entrega el estudio 
    public void setStudyContext(int index){
        try{
            eng.eval(String.format("study=bst_get('Study',%s)", index));
            Struct estudio = (Struct) eng.getVariable("study");
            
            this.setStudy(new Study(index, estudio,this.getSubject().nombreSujeto()));
        }catch(Exception e ){
            
        }
    }
    
    //Retorna una lista con los nombres de los sujetos en un protocolo
    public String[] protocolSubjects() {
        try {
            eng.eval("protocolSubject=bst_get('ProtocolSubjects')");
            Struct sujetos = (Struct) eng.getVariable("protocolSubject");
            Struct defaul = (Struct) sujetos.get("DefaultSubject");
            System.out.println(defaul.get("Name"));
            double capa = this.numberSubjects();
            String[] lista = new String[(int) capa];
            String nameAux = "";
            if (capa <= 1) {
                Struct sujetoUnico = (Struct) sujetos.get("Subject");
                nameAux = (String) sujetoUnico.get("Name");
                lista[0] = nameAux;
                return lista;
            } else {
                Struct[] listaSujetos = (Struct[]) sujetos.get("Subject");
                int aux = 0;
                for (Struct sujeto : listaSujetos) {
                    nameAux = (String) sujeto.get("Name");
                    lista[aux] = nameAux;
                    aux += 1;
                    System.out.println("prueba " + nameAux);
                }
                return lista;
            }

        } catch (Exception e) {
            String[] lista = {""};
            return lista;
        }
    }

    //Retorna el numero de sujetos en un protocolo
    public double numberSubjects() {
        try {
            eng.eval("number=bst_get('SubjectCount')");
            double number = (double) eng.getVariable("number");
            System.out.println("Numero de sujetos " + number);
            return number;
        } catch (Exception e) {
            return 0;
        }
    }

    //Retorna el sujeto al que pertenece un estudio
    public void getSubjectFileName(String fileName) {
        try {
            eng.eval(String.format("sujeto=bst_get('Subject','%s', 1)", fileName));
            System.out.println("Logrado");
        } catch (Exception e) {
            Struct aux = new Struct();
            e.printStackTrace();
            //return aux;
        }
    }

    /**
     * @return the isBrainstorm
     */
    public Boolean getIsBrainstorm() {
        return isBrainstorm;
    }

    /**
     * @param isBrainstorm the isBrainstorm to set
     */
    public void setIsBrainstorm(Boolean isBrainstorm) {
        this.isBrainstorm = isBrainstorm;
    }

    /**
     * @return the protocol
     */
    public Protocolo getProtocol() {
        return protocol;
    }

    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(Protocolo protocol) {
        this.protocol = protocol;
    }

    /**
     * @return the subject
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    /**
     * @return the study
     */
    public Study getStudy() {
        return study;
    }

    /**
     * @param study the study to set
     */
    public void setStudy(Study study) {
        this.study = study;
    }

}
