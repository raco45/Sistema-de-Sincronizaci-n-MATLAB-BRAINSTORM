package brainstorm.info;

import com.mathworks.matlab.types.Struct;

/**
 *
 * @author raco1
 */
public class Subject {

    public int subjectIndex;
    public Struct sujeto;
    public String protocolo;

    public Subject(int index, Struct sujeto, String protocol) {
        this.subjectIndex = index;
        this.sujeto = sujeto;
        this.protocolo = protocol;
    }

    public String nombreSujeto() {
        try {
            String name = (String) this.sujeto.get("Name");
            return name;
        } catch (Exception e) {
              return null;

        }
    }
    public String fileName(){
        try{
            String fileName = (String) this.sujeto.get("FileName");
            return fileName;
        }catch(Exception e){
            return null;
        }
    }

    /**
     * @return the subjectIndex
     */
    public int getSubjectIndex() {
        return subjectIndex;
    }

    /**
     * @param subjectIndex the subjectIndex to set
     */
    public void setSubjectIndex(int subjectIndex) {
        this.subjectIndex = subjectIndex;
    }

    /**
     * @return the sujeto
     */
    public Struct getSujeto() {
        return sujeto;
    }

    /**
     * @param sujeto the sujeto to set
     */
    public void setSujeto(Struct sujeto) {
        this.sujeto = sujeto;
    }

    /**
     * @return the protocolo
     */
    public String getProtocolo() {
        return protocolo;
    }

    /**
     * @param protocolo the protocolo to set
     */
    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

}
