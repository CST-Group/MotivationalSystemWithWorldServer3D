/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class ClosestJewelDetector extends Codelet {

    private MemoryObject knownMO;
    private MemoryObject closestJewelMO;
    private MemoryObject innerSenseMO;

    private final int reachDistance;
    private final Creature creature;

    public ClosestJewelDetector(String name, Creature creature, int reachDistance) {
        this.creature = creature;
        this.reachDistance = reachDistance;
        this.setName(name);
    }

    @Override
    public void accessMemoryObjects() {
        if (getKnownMO() == null)
            this.setKnownMO((MemoryObject) this.getInput("KNOWN_JEWELS"));

        if (getInnerSenseMO() == null)
            this.setInnerSenseMO((MemoryObject) this.getInput("INNER"));

        if (getClosestJewelMO() == null)
            this.setClosestJewelMO((MemoryObject) this.getOutput("CLOSEST_JEWEL"));
    }

    @Override
    public void proc() {
        Thing closestJewel = null;

        List<Thing> jewels = new CopyOnWriteArrayList<Thing>((List<Thing>) getKnownMO().getI());

        if(jewels != null) {
            if (jewels.size() > 0) {

                jewels.sort(Comparator.comparing(a -> getCreature().calculateDistanceTo(a)));

                Thing jewel = jewels.get(0);
                double dNew = getCreature().calculateDistanceTo(jewel);

                if (dNew <= getReachDistance()) {
                    closestJewel = jewel;
                }

                if (closestJewel != null) {
                    if (getClosestJewelMO().getI() == null || !getClosestJewelMO().getI().equals(closestJewel)) {
                        getClosestJewelMO().setI(closestJewel);
                    }

                } else {

                    closestJewel = null;
                    getClosestJewelMO().setI(closestJewel);
                }
            } else {
                closestJewel = null;
                getClosestJewelMO().setI(closestJewel);
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


    public int getReachDistance() {
        return reachDistance;
    }

    public Creature getCreature() {
        return creature;
    }
}
