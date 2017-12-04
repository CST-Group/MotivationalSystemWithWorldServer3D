package br.unicamp.mtwsapp.codelets.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

/**
 * @author Du
 */
public class ClosestAppleDetector extends Codelet {

    private MemoryObject knownMO;
    private MemoryObject closestAppleMO;
    private MemoryObject innerSenseMO;

    private final int reachDistance;
    private final Creature creature;


    public ClosestAppleDetector(String name, Creature creature, int reachDistance) {
        this.reachDistance = reachDistance;
        this.creature = creature;
        this.setName(name);
    }

    @Override
    public void accessMemoryObjects() {
        if (getKnownMO() == null)
            this.setKnownMO((MemoryObject) this.getInput("KNOWN_APPLES"));

        if (getInnerSenseMO() == null)
            this.setInnerSenseMO((MemoryObject) this.getInput("INNER"));

        if (getClosestAppleMO() == null)
            this.setClosestAppleMO((MemoryObject) this.getOutput("CLOSEST_APPLE"));


    }

    @Override
    public void proc() {
        Thing closestFood = null;

        List<Thing> apples = new CopyOnWriteArrayList<Thing>((List<Thing>) getKnownMO().getI());

        if(apples != null) {
            if (apples.size() != 0) {

                apples.sort(Comparator.comparing(a -> getCreature().calculateDistanceTo(a)));

                Thing food = apples.get(0);
                double dNew = getCreature().calculateDistanceTo(food);

                if (dNew <= getReachDistance()) {
                    closestFood = food;
                }

                if (closestFood != null) {
                    if (getClosestAppleMO().getI() == null || !getClosestAppleMO().getI().equals(closestFood)) {
                        getClosestAppleMO().setI(closestFood);
                    }

                } else {
                    //couldn't find any nearby apples
                    closestFood = null;
                    getClosestAppleMO().setI(closestFood);
                }
            } else {
                closestFood = null;
                getClosestAppleMO().setI(closestFood);
            }
        }

    }//end proc

    @Override
    public void calculateActivation() {

    }


    public MemoryObject getKnownMO() {
        return knownMO;
    }

    public void setKnownMO(MemoryObject knownMO) {
        this.knownMO = knownMO;
    }

    public MemoryObject getClosestAppleMO() {
        return closestAppleMO;
    }

    public void setClosestAppleMO(MemoryObject closestAppleMO) {
        this.closestAppleMO = closestAppleMO;
    }

    public MemoryObject getInnerSenseMO() {
        return innerSenseMO;
    }

    public void setInnerSenseMO(MemoryObject innerSenseMO) {
        this.innerSenseMO = innerSenseMO;
    }

    public int getReachDistance() {
        return reachDistance;
    }

    public Creature getCreature() {
        return creature;
    }
}
