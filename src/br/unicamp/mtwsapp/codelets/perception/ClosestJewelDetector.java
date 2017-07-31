/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class ClosestJewelDetector extends Codelet {

    private MemoryObject knownMO;
    private MemoryObject closestJewelMO;
    private MemoryObject innerSenseMO;

    private List<Thing> known;
    private final int reachDistance;
    private final Creature creature;

    public ClosestJewelDetector(String name, Creature creature, int reachDistance) {
        this.creature = creature;
        this.reachDistance = reachDistance;
        this.setName(name);
    }

    @Override
    public void accessMemoryObjects() {
        if(getKnownMO() ==null)
            this.setKnownMO((MemoryObject) this.getInput("KNOWN_JEWELS"));

        if(getInnerSenseMO() == null)
            this.setInnerSenseMO((MemoryObject) this.getInput("INNER"));

        if(getClosestJewelMO() ==null)
            this.setClosestJewelMO((MemoryObject) this.getOutput("CLOSEST_JEWEL"));
    }

    @Override
    public synchronized void proc() {
        Thing closest_jewel = null;
        setKnown(Collections.synchronizedList((List<Thing>) getKnownMO().getI()));
        synchronized (getKnown()) {
            if (!getKnown().isEmpty()) {
                //Iterate over objects in vision, looking for the closest apple
                CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(getKnown());
                for (Thing t : myknown) {
                    String objectName = t.getName();
                    if (objectName.contains("Jewel")) {

                        double Dnew = getCreature().calculateDistanceTo(t);

                        if (Dnew <= getReachDistance()) {
                            closest_jewel = t;
                        }

                    }
                }

                if (closest_jewel != null) {
                    if (getClosestJewelMO().getI() == null || !getClosestJewelMO().getI().equals(closest_jewel)) {
                        getClosestJewelMO().setI(closest_jewel);
                    }

                } else {
                    //couldn't find any nearby apples
                    closest_jewel = null;
                    getClosestJewelMO().setI(closest_jewel);
                }
            } else { // if there are no known apples closest_apple must be null
                closest_jewel = null;
                getClosestJewelMO().setI(closest_jewel);
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

    public MemoryObject getClosestJewelMO() {
        return closestJewelMO;
    }

    public void setClosestJewelMO(MemoryObject closestJewelMO) {
        this.closestJewelMO = closestJewelMO;
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
