
package brainstorm;

import brainstorm.info.BrainstormContext;
import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;

/**
 * Esta clase implementa el patrón de diseño Singleton para garantizar que solo exista una instancia
 * de {@link BrainstormContext} durante la ejecución del programa. Además, se encarga de inicializar
 * una conexión con el motor de MATLAB a través de {@link MatlabEngine}.
 * <p>
 * El patrón Singleton se implementa mediante un método estático {@link #getInstance()} que devuelve
 * la única instancia de {@link BrainstormContext}. Si la instancia no ha sido creada, se inicializa
 * junto con la conexión a MATLAB.
 * </p>
 * <p>
 * La instancia se almacena en un campo estático y volátil ({@link #instancia}) para garantizar
 * la visibilidad entre hilos en entornos multihilo.
 * </p>
 * 
 * @author raco1
 * @version 1.0
 * @see BrainstormContext
 * @see MatlabEngine
 */
public final class BrainstormStart {
    
    private static volatile BrainstormContext instancia;
    private static MatlabEngine eng;
    
    private BrainstormStart(){
        
    }
    
    
      /**
     * Devuelve la instancia única de {@link BrainstormContext}. Si la instancia no ha sido creada,
     * se inicializa junto con la conexión a MATLAB.
     * <p>
     * Este método es seguro para su uso en entornos multihilo gracias al uso de un campo volátil
     * y la comprobación doble (double-checked locking).
     * </p>
     *
     * @return La instancia única de {@link BrainstormContext}.
     * @throws EngineException Si ocurre un error al iniciar el motor de MATLAB.
     * @throws InterruptedException Si el hilo actual es interrumpido mientras espera la inicialización.
     */
    public static BrainstormContext getInstance() throws EngineException, InterruptedException{
        if(instancia==null){
            eng = MatlabEngine.startMatlab();
            instancia=new BrainstormContext(eng);
        }
        return instancia;
    }
    
}
