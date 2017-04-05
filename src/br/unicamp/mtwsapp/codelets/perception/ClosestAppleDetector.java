package br.unicamp.mtwsapp.codelets.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;

import java.util.Collection;
import java.util.Collections;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
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


    public ClosestAppleDetector(Creature creature, int reachDistance) {
        this.reachDistance = reachDistance;
        this.creature = creature;
    }

    @Override
    public void accessMemoryObjects() {
        if(knownMO==null)
            this.knownMO = (MemoryObject) this.getInput("KNOWN_APPLES");

        if(innerSenseMO==null)
            this.innerSenseMO = (MemoryObject) this.getInput("INNER");

        if(closestAppleMO==null)
            this.closestAppleMO = (MemoryObject) this.getOutput("CLOSEST_APPLE");


    }

    @Override
    public void proc() {
        Thing closest_apple = null;
        known = Collections.synchronizedList((List<Thing>) knownMO.getI());

        synchronized (known) {
            if (known.size() != 0) {
                //Iterate over objects in vision, looking for the closest a pple
                CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(known);
                for (Thing t : myknown) {
                    String objectName = t.getName();
                    if (objectName.contains("PFood") || objectName.contains("NPFood")) {

                        double Dnew = creature.calculateDistanceTo(t);

                        if (Dnew <= reachDistance) {
                            closest_apple = t;
                        }

                    }
                }

                if (closest_apple != null) {
                    if (closestAppleMO.getI() == null || !closestAppleMO.getI().equals(closest_apple)) {
                        closestAppleMO.setI(closest_apple);
                    }

                } else {
                    //couldn't find any nearby apples
                    closest_apple = null;
                    closestAppleMO.setI(closest_apple);
                }
            } else { // if there are no known apples closest_apple must be null
                closest_apple = null;
                closestAppleMO.setI(closest_apple);
            }
        }
    }//end proc

    @Override
    public void calculateActivation() {

    }

   

}
