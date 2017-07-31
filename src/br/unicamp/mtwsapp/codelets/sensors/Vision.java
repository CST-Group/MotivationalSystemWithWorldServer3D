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


    public Vision(String name, Creature nc) {
        this.setName(name);
        this.setC(nc);
    }

    @Override
    public void accessMemoryObjects() {
        if(getVisionMO() ==null) {
            setVisionMO((MemoryObject) this.getOutput("VISION"));
        }
    }

    @Override
    public synchronized void proc() {
        synchronized (getC()) {
            getC().updateState();
            List<Thing> lt = new ArrayList<>();
            lt.addAll(getC().getThingsInVision());
            getVisionMO().setI(lt);
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


    public MemoryObject getVisionMO() {
        return visionMO;
    }

    public void setVisionMO(MemoryObject visionMO) {
        this.visionMO = visionMO;
    }

    public Creature getC() {
        return c;
    }

    public void setC(Creature c) {
        this.c = c;
    }
}





