package br.unicamp.mtwsapp.codelets.sensors;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;

import java.util.ArrayList;
import java.util.List;

import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

/**
 * @author Du
 */
public class Vision extends Codelet {

    private MemoryObject visionMO;
    private Creature c;


    public Vision(Creature nc) {
        this.c = nc;
    }

    @Override
    public void accessMemoryObjects() {
        visionMO = (MemoryObject) this.getOutput("VISION");
    }

    @Override
    public void proc() {
        c.updateState();
        synchronized (visionMO) {
            List<Thing> lt = new ArrayList<>();
            lt.addAll(c.getThingsInVision());
            visionMO.setI(lt);
        }
    }

    @Override
    public void calculateActivation() {
        try {
            setActivation(0);
        } catch (CodeletActivationBoundsException e) {
            e.printStackTrace();
        }
    }


}





