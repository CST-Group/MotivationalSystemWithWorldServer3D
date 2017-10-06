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

import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

/**
 * @author Du
 */
public class ClosestObstacleDetector extends Codelet {
    private MemoryObject visionMO;
    private MemoryObject closestObstacleMO;
    private MemoryObject innerSenseMO;

    private List<Thing> known;
    private final Creature creature;
    private final int reachDistance;

    public ClosestObstacleDetector(String name, Creature creature, int reachDistance) {
        this.creature = creature;
        this.reachDistance = reachDistance;
        this.setName(name);
    }

    @Override
    public void accessMemoryObjects() {
        if (getVisionMO() == null)
            this.setVisionMO((MemoryObject) this.getInput("VISION"));

        if (getInnerSenseMO() == null)
            this.setInnerSenseMO((MemoryObject) this.getInput("INNER"));

        if (getClosestObstacleMO() == null)
            this.setClosestObstacleMO((MemoryObject) this.getOutput("CLOSEST_OBSTACLE"));
    }

    @Override
    public synchronized void proc() {
        setKnown(Collections.synchronizedList((List<Thing>) getVisionMO().getI()));

        synchronized (getKnown()) {
            if (!getKnown().isEmpty()) {

                CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(getKnown());
                for (Thing t : myknown) {

                    double Dnew = getCreature().calculateDistanceTo(t);

                    if (Dnew <= getReachDistance()) {
                        getClosestObstacleMO().setI(t);
                        break;
                    }

                }
            } else {
                getClosestObstacleMO().setI(null);
            }
        }
    }//end proc

    public Thing isNear(Thing thing, double gap) {
        Thing result = null;

        if (((thing.getAttributes().getX1() - gap) <= getCreature().getPosition().getX() && (thing.getAttributes().getX2() + gap) >= getCreature().getPosition().getX())
                && ((thing.getAttributes().getY1() - gap) <= getCreature().getPosition().getY() && (thing.getAttributes().getY2() + gap) >= getCreature().getPosition().getY())) {
            result = thing;
        }
        return result;
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

    public MemoryObject getClosestObstacleMO() {
        return closestObstacleMO;
    }

    public void setClosestObstacleMO(MemoryObject closestObstacleMO) {
        this.closestObstacleMO = closestObstacleMO;
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

    public Creature getCreature() {
        return creature;
    }

    public int getReachDistance() {
        return reachDistance;
    }
}
