package br.unicamp.mtwsapp.codelets.sensors;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import ws3dproxy.model.Creature;

/**
 * @author Du
 */

public class InnerSense extends Codelet {

    private MemoryObject innerSenseMO;
    private Creature c;
    private CreatureInnerSense cis;

    public InnerSense(Creature nc) {
        c = nc;
    }

    @Override
    public void accessMemoryObjects() {
        innerSenseMO = (MemoryObject) this.getOutput("INNER");
        cis = (CreatureInnerSense) innerSenseMO.getI();
    }

    public void proc() {
        cis.setPosition(c.getPosition());
        cis.setPitch(c.getPitch());
        cis.setFov(c.getFOV());
        cis.setFuel(c.getFuel());
        cis.setLeafletList(c.getLeaflets());
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
