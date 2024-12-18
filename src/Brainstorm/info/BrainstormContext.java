
package brainstorm.info;

import com.mathworks.engine.MatlabEngine;
import com.mathworks.matlab.types.Struct;
import java.util.concurrent.ExecutionException;


public class BrainstormContext {
    
    private MatlabEngine eng;
    private Boolean isBrainstorm=false;
    
    
    public BrainstormContext(MatlabEngine eng){
        this.eng=eng;
    }
    
    
    //Iniciar la libreria Brainstorm
    
    public void startBrainstorm(){
        try{
            eng.eval("brainstorm nogui;", null, null);
            this.setIsBrainstorm(true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    // Informacion del protocolo cargado
    
    public double currentProtocolIndex(){
        try{
            eng.eval("indexProtocol=bst_get('iProtocol')");
            double indice= (double) eng.getVariable("indexProtocol");
            System.out.println("Indice del protocolo: "+ indice);
            return indice;
        }catch(IllegalStateException | InterruptedException | ExecutionException e){
            e.printStackTrace();
            return -1;
        }
    }
    public String currentProtocolName(){
        try{
            eng.eval("infoProtocol=bst_get('ProtocolInfo')");
            Struct protocolo= eng.getVariable("infoProtocol");
            String nombreProtocolo=(String) protocolo.get("Comment"); //Nombre del protocolo cargado
            return nombreProtocolo;
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }
    
    public String currentSujectName(){
        try{
            eng.eval("currentSubject=bst_get('Subject')");
            Struct sujeto= eng.getVariable("currentSubject");
            String name= (String) sujeto.get("Name");
            return name;
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }
    // Da una lista con el nombre del estudio/archivo cargado en el protocolo actual
    public String currentStudyName(){
        try{
            eng.eval("currentStudy=bst_get('Study')");
            Struct study= eng.getVariable("currentStudy");
            String name = (String) study.get("Name");
            System.out.println(name.replaceAll("@raw",""));
            return name.replaceAll("@raw","");
            
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }
    
    // Da una lista con los nombres de los estudios
    public String[] subjectStudies(){
        try{
            eng.eval("infoStudies=bst_get('ProtocolStudies')");
            Struct infoEstudios= (Struct) eng.getVariable("infoStudies");
            Struct[] detalleEstudios= (Struct[]) infoEstudios.get("Study"); // Array de estudios
            eng.eval("estudios=bst_get('StudyCount')");
            String[] lista= new String[detalleEstudios.length];
            int aux=0;
            String nameAux="";
            for(Struct estudio: detalleEstudios){
                nameAux= (String)estudio.get("Name");
                this.getSubjectFileName(nameAux);
                lista[aux]=nameAux;
                aux+=1;
                System.out.println(nameAux);
            }
            return lista;
        }catch(Exception e){
            String[] lista= {""};
            e.printStackTrace();
            return lista;
        }
    }
    
    //Retorna el sujeto al que pertenece un estudio
    public void getSubjectFileName(String fileName){
        try{
            eng.eval(String.format("sujeto=bst_get('Subject','%s', 1)",fileName));
            System.out.println("Logrado");
        }catch(Exception e ){
            Struct aux= new Struct();
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
    
}
