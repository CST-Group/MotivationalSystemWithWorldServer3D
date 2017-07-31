package br.unicamp.mtwsapp.codelets.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

/**
 *
 * @author Du
 */
public class ClosestAppleDetector extends Codelet {

    private MemoryObject knownMO;
    private MemoryObject closestAppleMO;
    private MemoryObject innerSenseMO;

    private List<Thing> known;
    private final int reachDistance;
    private final Creature creature;


    public ClosestAppleDetector(String name, Creature creature, int reachDistance) {
        this.reachDistance = reachDistance;
        this.creature = creature;
        this.setName(name);
    }

    @Override
    public void accessMemoryObjects() {
        if(getKnownMO() ==null)
            this.setKnownMO((MemoryObject) this.getInput("KNOWN_APPLES"));

        if(getInnerSenseMO() ==null)
            this.setInnerSenseMO((MemoryObject) this.getInput("INNER"));

        if(getClosestAppleMO() ==null)
            this.setClosestAppleMO((MemoryObject) this.getOutput("CLOSEST_APPLE"));


    }

    @Override
    public synchronized void proc() {
        Thing closest_apple = null;
        setKnown(Collections.synchronizedList((List<Thing>) getKnownMO().getI()));

        synchronized (getKnown()) {
            if (getKnown().size() != 0) {
                //Iterate over objects in vision, looking for the closest a pple
                CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(getKnown());
                for (Thing t : myknown) {
                    String objectName = t.getName();
                    if (objectName.contains("PFood") || objectName.contains("NPFood")) {

                        double Dnew = getCreature().calculateDistanceTo(t);

                        if (Dnew <= getReachDistance()) {
                            closest_apple = t;
                        }

                    }
                }

                if (closest_apple != null) {
                    if (getClosestAppleMO().getI() == null || !getClosestAppleMO().getI().equals(closest_apple)) {
                        getClosestAppleMO().setI(closest_apple);
                    }

                } else {
                    //couldn't find any nearby apples
                    closest_apple = null;
                    getClosestAppleMO().setI(closest_apple);
                }
            } else { // if there are no known apples closest_apple must be null
                closest_apple = null;
                getClosestAppleMO().setI(closest_apple);
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

    public List<Thing> getKnown() {
        return known;
    }

    public void setKnown(List<Thing> known) {
        this.known = known;
    }

    public int getReachDistance() {
        return reachDistance;
    }

    public Creature getCreature() {
        return creature;
    }
}
