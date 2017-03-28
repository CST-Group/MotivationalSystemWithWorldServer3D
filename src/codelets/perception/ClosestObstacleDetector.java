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

import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

/**
 *
 * @author Du
 */
public class ClosestObstacleDetector extends Codelet {
     private MemoryObject visionMO;
    private MemoryObject closestObstacleMO;
    private MemoryObject innerSenseMO;

    private List<Thing> known;
    private final Creature creature;
    private final int reachDistance;

    public ClosestObstacleDetector(Creature creature, int reachDistance) {
        this.creature = creature;
        this.reachDistance = reachDistance;
    }

    @Override
    public void accessMemoryObjects() {
        if(visionMO == null)
            this.visionMO = (MemoryObject) this.getInput("VISION");

        if(innerSenseMO == null)
            this.innerSenseMO = (MemoryObject) this.getInput("INNER");

        if(closestObstacleMO == null)
            this.closestObstacleMO = (MemoryObject) this.getOutput("CLOSEST_OBSTACLE");
    }

    @Override
    public void proc() {
        Thing closest_obstacle = null;
        known = Collections.synchronizedList((List<Thing>) visionMO.getI());
        closestObstacleMO.setI(closest_obstacle);
        
        synchronized (known) {
            if (!known.isEmpty()) {
                //Iterate over objects in vision, looking for the closest apple
                CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(known);
                for (Thing t : myknown) {
                    String objectName = t.getName();
                    if (objectName.contains("Brick")) {
                        closest_obstacle = isNear(t, reachDistance);
                    }
                }

                if (closest_obstacle != null) {
                    if (closestObstacleMO.getI() == null || !closestObstacleMO.getI().equals(closest_obstacle)) {
                        closestObstacleMO.setI(closest_obstacle);
                    }

                } else {
                    
                    closest_obstacle = null;
                    closestObstacleMO.setI(closest_obstacle);
                }
            } else { 
                closest_obstacle = null;
                closestObstacleMO.setI(closest_obstacle);
            }
        }
    }//end proc

    public Thing isNear(Thing thing, double gap) {
        Thing result = null;

        if (((thing.getAttributes().getX1() - gap) <= creature.getPosition().getX() && (thing.getAttributes().getX2() + gap) >= creature.getPosition().getX())
                && ((thing.getAttributes().getY1() - gap) <= creature.getPosition().getY() && (thing.getAttributes().getY2() + gap) >= creature.getPosition().getY())) {
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

    
}
