
package brainstorm.info;

import com.mathworks.matlab.types.Struct;

/**
 *
 * @author raco1
 */
public class Protocolo {
    public int index;
    public Struct protocolo;
    
    
    public Protocolo(int index, Struct protocolo){
        this.index=index;
        this.protocolo=protocolo;
    }
    
    
    
    public String nombreProtocolo(){
        String nombreProtocolo = (String) this.protocolo.get("Comment"); //Nombre del protocolo cargado
        return nombreProtocolo;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return the protocolo
     */
    public Struct getProtocolo() {
        return protocolo;
    }

    /**
     * @param protocolo the protocolo to set
     */
    public void setProtocolo(Struct protocolo) {
        this.protocolo = protocolo;
    }
}
