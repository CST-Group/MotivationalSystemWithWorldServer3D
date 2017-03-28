/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codelets.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import memory.CreatureInnerSense;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;
import ws3dproxy.util.Constants;

/**
 *
 * @author Du
 */
public class ClosestJewelDetector extends Codelet {

    private MemoryObject knownMO;
    private MemoryObject closestJewelMO;
    private MemoryObject innerSenseMO;

    private List<Thing> known;
    private final int reachDistance;
    private final Creature creature;

    public ClosestJewelDetector(Creature creature, int reachDistance) {
        this.creature = creature;
        this.reachDistance = reachDistance;
    }

    @Override
    public void accessMemoryObjects() {
        if(knownMO==null)
            this.knownMO = (MemoryObject) this.getInput("KNOWN_JEWELS");

        if(innerSenseMO == null)
            this.innerSenseMO = (MemoryObject) this.getInput("INNER");

        if(closestJewelMO==null)
            this.closestJewelMO = (MemoryObject) this.getOutput("CLOSEST_JEWEL");
    }

    @Override
    public void proc() {
        Thing closest_jewel = null;
        known = Collections.synchronizedList((List<Thing>) knownMO.getI());
        synchronized (known) {
            if (!known.isEmpty()) {
                //Iterate over objects in vision, looking for the closest apple
                CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(known);
                for (Thing t : myknown) {
                    String objectName = t.getName();
                    if (objectName.contains("Jewel")) {

                        double Dnew = creature.calculateDistanceTo(t);

                        if (Dnew <= reachDistance) {
                            closest_jewel = t;
                        }

                    }
                }

                if (closest_jewel != null) {
                    if (closestJewelMO.getI() == null || !closestJewelMO.getI().equals(closest_jewel)) {
                        closestJewelMO.setI(closest_jewel);
                    }

                } else {
                    //couldn't find any nearby apples
                    closest_jewel = null;
                    closestJewelMO.setI(closest_jewel);
                }
            } else { // if there are no known apples closest_apple must be null
                closest_jewel = null;
                closestJewelMO.setI(closest_jewel);
            }
        }
    }//end proc

    @Override
    public void calculateActivation() {

    }


}
