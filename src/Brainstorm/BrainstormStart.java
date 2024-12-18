
package brainstorm;

import brainstorm.info.BrainstormContext;
import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;

/**
 *
 * @author raco1
 */
public final class BrainstormStart {
    
    private static volatile BrainstormContext instancia;
    private static MatlabEngine eng;
    
    private BrainstormStart(){
        
    }
    
    public static BrainstormContext getInstance() throws EngineException, InterruptedException{
        if(instancia==null){
            eng = MatlabEngine.startMatlab();
            instancia=new BrainstormContext(eng);
        }
        return instancia;
    }
    
}
