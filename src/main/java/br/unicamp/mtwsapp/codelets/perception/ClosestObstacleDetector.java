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

import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

/**
 * @author Du
 */
public class ClosestObstacleDetector extends Codelet {
    private MemoryObject knownObstaclesMO;
    private MemoryObject closestObstacleMO;

    private final Creature creature;
    private final int reachDistance;

    public ClosestObstacleDetector(String name, Creature creature, int reachDistance) {
        this.creature = creature;
        this.reachDistance = reachDistance;
        this.setName(name);
    }

    @Override
    public void accessMemoryObjects() {

        if (getKnownObstaclesMO() == null)
            this.setKnownObstaclesMO((MemoryObject) this.getInput("KNOWN_OBSTACLES"));

        if (getClosestObstacleMO() == null)
            this.setClosestObstacleMO((MemoryObject) this.getOutput("CLOSEST_OBSTACLE"));
    }

    @Override
    public void proc() {

        Thing closestObstacle = null;

        List<Thing> obstacles = new CopyOnWriteArrayList<Thing>((List<Thing>) knownObstaclesMO.getI());

        if(obstacles != null) {
            if (obstacles.size() != 0) {

                obstacles.sort(Comparator.comparing(a -> getCreature().calculateDistanceTo(a)));

                Thing obstacle = obstacles.get(0);

                Thing thing = isNear(obstacle, getReachDistance());

                if (thing != null) {
                    closestObstacle = obstacle;
                }

                if (closestObstacle != null) {
                    if (getClosestObstacleMO().getI() == null || !getClosestObstacleMO().getI().equals(closestObstacle)) {
                        getClosestObstacleMO().setI(closestObstacle);
                    }

                } else {

                    closestObstacle = null;
                    getClosestObstacleMO().setI(closestObstacle);
                }
            } else {
                closestObstacle = null;
                getClosestObstacleMO().setI(closestObstacle);
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


    public MemoryObject getClosestObstacleMO() {
        return closestObstacleMO;
    }

    public void setClosestObstacleMO(MemoryObject closestObstacleMO) {
        this.closestObstacleMO = closestObstacleMO;
    }

    public Creature getCreature() {
        return creature;
    }

    public int getReachDistance() {
        return reachDistance;
    }

    public MemoryObject getKnownObstaclesMO() {
        return knownObstaclesMO;
    }

    public void setKnownObstaclesMO(MemoryObject knownObstaclesMO) {
        this.knownObstaclesMO = knownObstaclesMO;
    }
}
