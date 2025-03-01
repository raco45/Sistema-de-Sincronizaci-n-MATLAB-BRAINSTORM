package brainstorm.info;

import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;
import com.mathworks.engine.MatlabExecutionException;
import com.mathworks.engine.MatlabSyntaxException;
import com.mathworks.matlab.types.Struct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Clase creada para acceder a todas las funciones de Brainstorm
 *
 * @author raco1
 */
public class BrainstormContext {

    private MatlabEngine eng;
    private Boolean isBrainstorm = false;
    public Protocolo protocol;
    public Subject subject;
    public Study study;

    public BrainstormContext(MatlabEngine eng) {
        this.eng = eng;
    }

    /**
     * Funcion para iniciar Brainstorm en segundo plano
     */
    public void startBrainstorm() {
        try {
            eng.eval("brainstorm nogui;", null, null);
            this.setIsBrainstorm(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Inicia la interfaz de brainstorm
     */
    public void openGUI() {
        try {
            eng.eval("brainstorm start");
        } catch (Exception e) {

        }
    }

    /**
     * Accede a la ruta de la bd de Brainstorm
     *
     * @return Retorna la ruta con la Bd de brainstorm
     */
    public String homeDirectory() {
        try {
            eng.eval("homeDirectory=bst_get('BrainstormDbDir')");
            String ruta = (String) eng.getVariable("homeDirectory");
            return ruta;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Se encarga de cargar un protocolo recibiendo el indice del protocolo.
     *
     * @param index
     */
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

    /**
     * Se encarga de cargar un sujeto recibiendo el indice del sujeto.
     *
     * @param iSubject
     */
    public void setSubject(int iSubject) {
        try {
            if (iSubject > 0) {
                eng.eval(String.format("sSubject=bst_get('Subject', %s)", iSubject));
                Struct sSubject = (Struct) eng.getVariable("sSubject");
                String protocolo = this.currentProtocolName();
                Subject sujeto = new Subject(iSubject, sSubject, protocolo);
                this.setStudy(null);
                this.setSubject(sujeto);
//                eng.eval(String.format("bst_set('Subject',%s,sSubject)", iSubject));
            } else {
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
    public ArrayList<Study> subjectStudies(String subject) {
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
//                    System.out.println(nameAux);
//                    System.out.println(nameSubject);
//                    System.out.println(aux);
                    aux += 1;
                }
            }
            return estudios;
        } catch (Exception e) {
            return null;
        }
    }

    // Recibe un indice y nos entrega el estudio 
    public void setStudyContext(int index) {
        try {
            ArrayList estudios = this.subjectStudies(this.getSubject().nombreSujeto());
//            estudios.get(index);
//            eng.eval(String.format("study=bst_get('Study',%s)", index));
//            Struct estudio = (Struct) eng.getVariable("study");
            this.setStudy((Study) estudios.get(index - 1));
//            this.setStudy(new Study(index, estudio,this.getSubject().nombreSujeto()));
        } catch (Exception e) {

        }
    }

    //Retorna una lista con los nombres de los sujetos en un protocolo
    public String[] protocolSubjects() {
        try {
            eng.eval("protocolSubject=bst_get('ProtocolSubjects')");
            Struct sujetos = (Struct) eng.getVariable("protocolSubject");
            Struct defaul = (Struct) sujetos.get("DefaultSubject");
//            System.out.println(defaul.get("Name"));
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
//                    System.out.println("prueba " + nameAux);
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
//            System.out.println("Numero de sujetos " + number);
            return number;
        } catch (Exception e) {
            return 0;
        }
    }

    //Retorna el sujeto al que pertenece un estudio
    public void getSubjectFileName(String fileName) {
        try {
            eng.eval(String.format("sujeto=bst_get('Subject','%s', 1)", fileName));
//            System.out.println("Logrado");
        } catch (Exception e) {
            Struct aux = new Struct();
            e.printStackTrace();
            //return aux;
        }
    }

    //Funcion para eliminar un estudio 
    public void deleteStudy(String fileName) {
        try {
            int aux = 1;
            Struct[] protocolStudies = this.protocolStudies();
            for (Struct study : protocolStudies) {

                Struct data = (Struct) study.get("Data");
                if (data != null) {
                    String nameAux = (String) data.get("FileName");
                    System.out.println(nameAux);
                    if (nameAux.equals(fileName)) {
                        eng.eval(String.format("errFolders = db_delete_studies( %s )", aux));

                    }
                }

                aux += 1;
            }

        } catch (Exception e) {

        }
    }

    //Obtener el channel de un estudio
    public void channelStudy() {
        try {
            eng.eval(String.format("channel=bst_get('ChannelForStudy',%s)", this.getStudy().studyIndex));
            Struct fileName = (Struct) eng.getVariable("channel");
            String name = (String) fileName.get("FileName");
        } catch (InterruptedException ex) {
            Logger.getLogger(BrainstormContext.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MatlabSyntaxException ex) {
            Logger.getLogger(BrainstormContext.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CancellationException ex) {
            Logger.getLogger(BrainstormContext.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EngineException ex) {
            Logger.getLogger(BrainstormContext.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(BrainstormContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Sirve para cambiar el tipo de data al correcto durante la sincronizacion. tipoSensor= 1 para FC, 0 para GSR
    public void changeDataType(String name) throws InterruptedException {
        try {
//            eng.eval(String.format("ChannelMat = in_bst_channel('%s')",name)); // Struct con informacion del channel file
            eng.eval(String.format("sInputs = bst_process('GetInputStruct', '%s')", name)); //Struct para pasarle a un proceso

            eng.eval(String.format("preua=bst_process('CallProcess', 'process_channel_settype', sInputs, [],'sensortypes', 'EEG', 'newtype', 'NEULOG')", name)); // Processo para cambiarle el tipo a un sensor

//            eng.eval(String.format("preua=bst_process('CallProcess', 'process_channel_settype', sInputs, [],'sensortypes', 'BPM', 'newtype', 'NUELOG')", name)); // Processo para cambiarle el tipo a un sensor
        } catch (MatlabSyntaxException ex) {
            Logger.getLogger(BrainstormContext.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CancellationException ex) {
            Logger.getLogger(BrainstormContext.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EngineException ex) {
            Logger.getLogger(BrainstormContext.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(BrainstormContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Funcion para importar una nueva grabacion a un sujeto seleccionado
    public String reviewRawFile(String ruta) {
        try {
            eng.eval("sFiles=[]");

            eng.eval("bst_report('Start',sFiles)");

            String s1 = this.subject.nombreSujeto();
            eng.eval(String.format("sFiles = bst_process('CallProcess', 'process_import_data_raw', sFiles, [], ...\n"
                    + "    'subjectname',    '%1$s', ...\n"
                    + "    'datafile',       {'%2$s', 'EEG-WS-CSV'}, ...\n"
                    + "    'channelreplace', 1, ...\n"
                    + "    'channelalign',   1, ...\n"
                    + "    'evtmode',        'value')", s1, ruta));

            this.subjectStudiesArray(s1);
            this.reload();
            Struct prueb = eng.getVariable("sFiles");
            String dataFileName = (String) prueb.get("FileName");
            return dataFileName;
        } catch (Exception e) {
            return null;
        }
    }

    // Cargar marcadores y convertir en eventos simples
    public Struct cargarMarcadores(String dataFileName, String ruta, String eventName) {
        try {
            eng.eval(String.format("sFiles = {...\n"
                    + "    '%s'}", dataFileName));
            eng.eval(String.format("RawFiles = {...\n"
                    + "    '%s'};", ruta));
            eng.eval("sFiles = bst_process('CallProcess', 'process_evt_import', sFiles, [], ...\n"
                    + "    'evtfile', {RawFiles{1}, 'CSV-TIME'}, ...\n"
                    + "    'evtname', 'New', ...\n"
                    + "    'delete',  0);");

            eng.eval(String.format("sFiles = bst_process('CallProcess', 'process_evt_simple', sFiles, [], ...\n"
                    + "    'eventname', '%s', ...\n"
                    + "    'method',    'start');", eventName));

            Struct prueb = eng.getVariable("sFiles");
            String dataName = (String) prueb.get("FileName");
            this.reload();
            return prueb;

        } catch (Exception e) {
            return null;
        }
    }

    //Sincronizar eventos. Recibe el data file name de los archivos a sincronizar. Me va a lanzar un array de struct, tengo que sacar los dos dataFileName de ahi
    public Struct[] syncEvents(String dataNameNeulog, String dataNameEmotiv, String markerName) {
        try {
            eng.eval(String.format("sFiles = {...\n"
                    + "    '%1s', ...\n"
                    + "    '%2s'};", dataNameNeulog, dataNameEmotiv));

            eng.eval(String.format(
                    "sFiles = bst_process('CallProcess', 'process_sync_recordings', sFiles, [], ...\n"
                    + "    'src',          '%s', ...\n"
                    + "    'method',       'xcorr'); ", markerName));

            Struct[] files = (Struct[]) eng.getVariable("sFiles");
            return files;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Combine recordings sincronizadas
    public String combineRecordings(String dataNeulog, String dataEmotiv) {
        try {
            eng.eval(String.format("sFiles = {...\n"
                    + "    '%1$s', ...\n"
                    + "    '%2$s'};", dataNeulog, dataEmotiv));

            eng.eval("sFiles = bst_process('CallProcess', 'process_combine_recordings', sFiles, [], ...\n"
                    + "    'condition', 'Combined');");

            Struct prueba = (Struct) eng.getVariable("sFiles");
            String dataName = (String) prueba.get("FileName");
            double in = (double) prueba.get("iStudy");
            System.out.println(in);
            this.reload();
            return dataName;
        } catch (Exception e) {
            return null;
        }
    }

    //Agregar las la posicion de los electrodos
    public void addEEGPositions(String name) {
        try {
            eng.eval(String.format("sFiles = {...\n"
                    + "    '%s'};", name));
            String archivoPos = "src\\positionsFile\\emotiv_epoc.pos";
            eng.eval(String.format("RawFiles = {...\n"
                    + "    '%s'}", archivoPos));

            eng.eval("sFiles = bst_process('CallProcess', 'process_channel_addloc', sFiles, [], ...\n"
                    + "    'channelfile', {RawFiles{1}, 'POLHEMUS'}, ...\n"
                    + "    'usedefault',  '', ...  % \n"
                    + "    'fixunits',    1, ...\n"
                    + "    'vox2ras',     1, ...\n"
                    + "    'mrifile',     {'', ''}, ...\n"
                    + "    'fiducials',   [])");
        } catch (Exception e) {

        }
    }

    public void videoPowers(String videoFileName) {
        try {
            eng.eval(String.format("[iNewFiles, OutputVideoFiles] = import_video(%1$s, '%2$s')", this.study.getStudyIndex(), videoFileName));
        } catch (Exception e) {

        }
    }

    public String scaleValues(String dataFileName, String type, double factor) {
        try {
            eng.eval(String.format("sFiles = {...\n"
                    + "    '%s'};", dataFileName));
            eng.eval(String.format("sFiles = bst_process('CallProcess', 'process_scale', sFiles, [], ...\n"
                    + "    'factor',      %1s, ...\n"
                    + "    'sensortypes', '%2s');", factor, type));

            System.out.println("Exitoso");
            Struct prueb = eng.getVariable("sFiles");
            String dataName = (String) prueb.get("FileName");
            return dataName;
        } catch (Exception e) {
            System.out.println("No exitoso");
            return null;
        }
    }

    public String changeStudyName(String dataFileName, String name) {
        try {
            eng.eval(String.format("sFiles = {...\n"
                    + "    '%s'}", dataFileName));
            eng.eval(String.format("sFiles = bst_process('CallProcess', 'process_set_comment', sFiles, [], ...\n"
                    + "    'tag',           '%s', ...\n"
                    + "    'isindex',       1)", name));
            Struct prueb = eng.getVariable("sFiles");
            String dataName = (String) prueb.get("FileName");
            this.reload();
            return dataName;
        } catch (Exception e) {
            return null;
        }
    }

    public void generarImagenes() throws InterruptedException, ExecutionException {
        String filePath = this.homeDirectory() + "\\" + this.protocol.nombreProtocolo() + "\\" + "data" + "\\";
        String dataPath = filePath + "\\" + this.study.dataFileName();
        String channelPath = filePath + "\\" + this.study.channelFileName();
        String path = filePath + "\\" + this.subject.nombreSujeto() + "\\" + this.study.nombreStudy();
        System.out.println(filePath);
        System.out.println(dataPath);
        System.out.println(channelPath);
        try {
            eng.eval(String.format("movie_path=frequencies_128Hz('%1$s','%2$s','%3$s')", path, dataPath, channelPath));
            String ruta = (String) eng.getVariable("movie_path");
            System.out.println(ruta);
            this.videoPowers(ruta);
        } catch (MatlabExecutionException e) {
            System.out.println("Wopos");
        }
    }

    //Generar peliculas
    public void generateTimeSeries() {
        try {
            eng.eval(String.format("datas='%s'", this.getStudy().dataFileName()));
            eng.eval("neulog = view_timeseries(datas, 'NEULOG',[])");
            eng.eval("eeg = view_timeseries(datas, 'EEG', [])");

//            eng.eval("movegui(eeg,'northeast');");
//            eng.eval("eeg.Position = [100, 500, 500, 400]");
//            eng.eval("neulog.Position = [700, 500, 500, 400];");
//
//            eng.eval("figure_timeseries('SetDisplayMode', eeg, 'Column');");
            ////
//            eng.eval("figure_timeseries('SetDisplayMode', neulog, 'Butterfly');");
            eng.eval("panel_record('SetDisplayMode', eeg, 'Column');");
            eng.eval("panel_record('SetDisplayMode', neulog, 'Butterfly');");

            eng.eval("mapa=view_topography(datas, 'EEG', '2DDisc')");
//            this.scaleValues();

            if (this.study.isVideo()) {
                String videoName = this.study.dataVideoFileName();
                String filePath = this.homeDirectory() + "\\" + this.protocol.nombreProtocolo() + "\\" + "data" + "\\";
                eng.eval(String.format("pow=view_video('%s', 'VideoReader', 0)", filePath + videoName));

                eng.eval("organizarFiguras(eeg,mapa,pow,neulog)");
            } else {
                System.out.println("prueb");
                eng.eval("arreglar(eeg,neulog,mapa)");
            }

        } catch (Exception e) {

        }
    }

    public void addPath() {
        try {
            String ruta;
            URL resourceUrl = getClass().getResource("/interfaz/Graphics");

            // Verificar si estamos ejecutando desde un JAR
            if (resourceUrl != null && resourceUrl.getProtocol().equals("jar")) {
                // Crear directorio temporal
                File tempDir = crearDirectorioTemporal();
                // Extraer recursos del JAR
                extraerRecursosDelJar(resourceUrl, tempDir);
                ruta = tempDir.getAbsolutePath();
            } else {
                // Ruta normal cuando se ejecuta desde IDE
                ruta = System.getProperty("user.dir") + "/src/interfaz/Graphics";
                ruta = ruta.replace("\\", "/");
            }

            // Verificar y agregar al path de MATLAB
            String verificarPath = "any(strcmp(strsplit(path, ';'), '" + ruta + "'))";
            boolean enPath = eng.feval("eval", verificarPath);

            if (!enPath) {
                eng.feval("addpath", ruta);
                System.out.println("Ruta agregada al path de MATLAB: " + ruta);
            } else {
                System.out.println("La ruta ya está en el path de MATLAB.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File crearDirectorioTemporal() throws IOException {
        File tempDir = Files.createTempDirectory("matlab-graphics").toFile();
        // Eliminar al cerrar la aplicación
        tempDir.deleteOnExit();
        return tempDir;
    }

    private void extraerRecursosDelJar(URL resourceUrl, File tempDir) throws IOException {
        String jarPath = resourceUrl.getPath().split("!")[0].replace("file:", "");
        jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8.toString());

        // Corrección para Windows
        if (jarPath.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("win")) {
            jarPath = jarPath.substring(1);
        }

        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jar.entries();
            String directorioDestino = "interfaz/Graphics/";

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(directorioDestino)) {
                    String rutaRelativa = entry.getName().substring(directorioDestino.length());
                    File archivoDestino = new File(tempDir, rutaRelativa);

                    if (entry.isDirectory()) {
                        archivoDestino.mkdirs();
                    } else {
                        archivoDestino.getParentFile().mkdirs();
                        try (InputStream is = jar.getInputStream(entry)) {
                            Files.copy(is, archivoDestino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        }
    }

    //Elimiar un protocolo el protocolo cargado o current protocol
    public void deleteProtocol() {
        try {
            eng.eval("db_delete_protocol(0,1)");
            this.resetContext();
            this.setProtocol(this.currentProtocolIndex());
        } catch (Exception e) {

        }
    }

    public void resetContext() {
        this.setSubject(null);
        this.setProtocol(null);
        this.setStudy(null);
    }

    public void deleteSubject() {
        try {
            eng.eval(String.format("db_delete_subjects(%s)", this.getSubject().subjectIndex));
            this.resetContext();
            this.setProtocol(this.currentProtocolIndex());
        } catch (Exception e) {

        }
    }

    // Volver a cargar la BD
    public void reload() {
        try {
            eng.eval("db_reload_database('current')");
//            eng.eval("bst_memory('UnloadAll','Forced')");
//            eng.eval(String.format("db_reload_subjects(%s)", index));
        } catch (Exception e) {

        }
    }

    public int crearProtocolo() {
        try {
            eng.eval("iProtocol=gui_edit_protocol('create')");
            int aux = (int) eng.getVariable("iProtocol");
            this.setProtocol(this.currentProtocolIndex());
            return aux;
        } catch (Exception e) {
            return 0;
        }
    }

    public void createProtocol(String protocolName) {
        try {
            eng.eval(String.format("ProtocolName = file_standardize('%s');", protocolName));
            eng.eval("sProtocol = db_template('ProtocolInfo');");
            eng.eval(String.format("    sProtocol.Comment           = '%s';", protocolName));
            eng.eval(String.format("sProtocol.SUBJECTS          = fullfile('%1s', '%2s', 'anat');", this.homeDirectory(), protocolName));
            eng.eval(String.format("sProtocol.STUDIES           = fullfile('%1s', '%2s', 'data');", this.homeDirectory(), protocolName));
            eng.eval("sProtocol.UseDefaultAnat    = 1;");
            eng.eval("sProtocol.UseDefaultChannel = 0;");
            eng.eval("iProtocol = db_edit_protocol('create', sProtocol);");

            double aux = (double) eng.getVariable("iProtocol");
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println("aux");
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            if (aux <= 0) {
                JOptionPane.showMessageDialog(null, "Could not create Protocol");
            } else {
                this.setProtocol(aux);
                eng.eval("    sTemplate = bst_get('AnatomyDefaults', 'ICBM152');");
                eng.eval("    db_set_template(0, sTemplate(1), 0);");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double createSujeto(String nombreSubject) {
        try {
            eng.eval(String.format("[sSubject,iSubject]=db_add_subject('%s', [], 1, 0)", nombreSubject));
            eng.eval("bst_memory('UnloadAll', 'Forced');");
            eng.eval("db_reload_subjects(iSubject)");
            eng.eval("db_save()");
            double aux = (double) eng.getVariable("iSubject");
            return aux;
        } catch (Exception e) {
            return -1;

        }
    }

    public int closeBrainstorm() {
        try {
            eng.eval("brainstorm stop");

        } catch (InterruptedException ex) {
            Logger.getLogger(BrainstormContext.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MatlabSyntaxException ex) {
            Logger.getLogger(BrainstormContext.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CancellationException ex) {
            Logger.getLogger(BrainstormContext.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EngineException ex) {
            Logger.getLogger(BrainstormContext.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(BrainstormContext.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 3;
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
