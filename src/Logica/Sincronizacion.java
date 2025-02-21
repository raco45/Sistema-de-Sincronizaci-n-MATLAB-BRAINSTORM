package logica;

import brainstorm.BrainstormStart;
import brainstorm.info.BrainstormContext;
import com.mathworks.engine.EngineException;
import com.mathworks.matlab.types.Struct;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author raco1
 */
public class Sincronizacion {

    public String rutaEmotiv;
    public String rutaNeulog;
    public String rutaMarcadoresEmotiv;
    public String rutaMarcadoresNeulog;
    public String emotivSync;
    public String neulogSync;
    public String rutaPowers;
    public String combine;
    public String markerName;
            
    private BrainstormContext bCon;

    public Sincronizacion(String emotiv, String neulog, String markerEmotiv, String markerNeulog) {
        this.rutaEmotiv = emotiv;
        this.rutaNeulog = neulog;
        this.rutaMarcadoresEmotiv=markerEmotiv;
        this.rutaMarcadoresNeulog=markerNeulog;
        try {
            bCon = BrainstormStart.getInstance();
        } catch (EngineException ex) {
            Logger.getLogger(Sincronizacion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Sincronizacion.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public void sincronizar() {
        try {
            String dataFileNameNeulog = bCon.reviewRawFile(rutaNeulog); // Ingresar datos de GSR y FC
            String dataFileNameEmotiv = bCon.reviewRawFile(rutaEmotiv); // ingresar datos de EEG
            
            //Cabiar el tipo de sensor en archivo Neulog
            bCon.changeDataType(dataFileNameNeulog);
            bCon.addEEGPositions(dataFileNameEmotiv);
            
            
            //Asignar marcadores a archivos de senales
            Struct dataNeulog = bCon.cargarMarcadores(dataFileNameNeulog, getRutaMarcadoresNeulog(), this.markerName);
            Struct dataEmotiv = bCon.cargarMarcadores(dataFileNameEmotiv, getRutaMarcadoresEmotiv(), this.markerName);
            
            String dataNeulogFileName = (String) dataNeulog.get("FileName");
            String dataEmotivFileName = (String) dataEmotiv.get("FileName");
            
            
            
            // Sincronizar eventos
            Struct[] archivos= bCon.syncEvents(dataNeulogFileName, dataEmotivFileName);
            
            this.emotivSync= (String) archivos[1].get("FileName");
            this.neulogSync= (String) archivos[0].get("FileName");
            // Combinar archivos sincronizados. 
            
            bCon.combineRecordings(emotivSync, neulogSync);
            
            //Obtener indice iStudy de los archivos  generados
            
//            double indexDataNeulog=(double) dataNeulog.get("iStudy");
//            double indexDataEmotiv= (double)dataEmotiv.get("iStudy");
//            double indexDataSyncNeulog= (double) archivos[0].get("iStudy");
//            double indexDataSyncEmotiv= (double) archivos[1].get("iStudy");
            
            System.out.println(dataNeulog);
            System.out.println(dataEmotiv);
            System.out.println(this.emotivSync);
            System.out.println(this.neulogSync);
            
            
            bCon.deleteStudy(dataNeulogFileName);
            bCon.deleteStudy(dataEmotivFileName);
            bCon.deleteStudy(this.emotivSync);
            bCon.deleteStudy(this.neulogSync);
            
            bCon.reload();
        } catch (Exception e) {

        }
    }

    
    public void resetSync(){
        this.setCombine("");
        this.setEmotivSync("");
        this.setNeulogSync("");
        this.setRutaEmotiv("");
        this.setRutaNeulog("");
        this.setRutaMarcadoresEmotiv("");
        this.setRutaMarcadoresNeulog("");
        this.setMarkerName("");
    }
    /**
     * @return the rutaEmotiv
     */
    public String getRutaEmotiv() {
        return rutaEmotiv;
    }

    /**
     * @param rutaEmotiv the rutaEmotiv to set
     */
    public void setRutaEmotiv(String rutaEmotiv) {
        this.rutaEmotiv = rutaEmotiv;
    }

    /**
     * @return the rutaNeulog
     */
    public String getRutaNeulog() {
        return rutaNeulog;
    }

    /**
     * @param rutaNeulog the rutaNeulog to set
     */
    public void setRutaNeulog(String rutaNeulog) {
        this.rutaNeulog = rutaNeulog;
    }

    /**
     * @return the emotivSync
     */
    public String getEmotivSync() {
        return emotivSync;
    }

    /**
     * @param emotivSync the emotivSync to set
     */
    public void setEmotivSync(String emotivSync) {
        this.emotivSync = emotivSync;
    }

    /**
     * @return the neulogSync
     */
    public String getNeulogSync() {
        return neulogSync;
    }

    /**
     * @param neulogSync the neulogSync to set
     */
    public void setNeulogSync(String neulogSync) {
        this.neulogSync = neulogSync;
    }

    /**
     * @return the combine
     */
    public String getCombine() {
        return combine;
    }

    /**
     * @param combine the combine to set
     */
    public void setCombine(String combine) {
        this.combine = combine;
    }

    /**
     * @return the rutaMarcadoresEmotiv
     */
    public String getRutaMarcadoresEmotiv() {
        return rutaMarcadoresEmotiv;
    }

    /**
     * @param rutaMarcadoresEmotiv the rutaMarcadoresEmotiv to set
     */
    public void setRutaMarcadoresEmotiv(String rutaMarcadoresEmotiv) {
        this.rutaMarcadoresEmotiv = rutaMarcadoresEmotiv;
    }

    /**
     * @return the rutaMarcadoresNeulog
     */
    public String getRutaMarcadoresNeulog() {
        return rutaMarcadoresNeulog;
    }

    /**
     * @param rutaMarcadoresNeulog the rutaMarcadoresNeulog to set
     */
    public void setRutaMarcadoresNeulog(String rutaMarcadoresNeulog) {
        this.rutaMarcadoresNeulog = rutaMarcadoresNeulog;
    }

    /**
     * @return the rutaPowers
     */
    public String getRutaPowers() {
        return rutaPowers;
    }

    /**
     * @param rutaPowers the rutaPowers to set
     */
    public void setRutaPowers(String rutaPowers) {
        this.rutaPowers = rutaPowers;
    }

    /**
     * @return the markerName
     */
    public String getMarkerName() {
        return markerName;
    }

    /**
     * @param markerName the markerName to set
     */
    public void setMarkerName(String markerName) {
        this.markerName = markerName;
    }

}
