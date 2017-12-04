package br.unicamp.mtwsapp.codelets.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Leaflet;
import ws3dproxy.model.Thing;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObstacleDetector extends Codelet {

    private MemoryObject visionMO;
    private MemoryObject knownObstaclesMO;
    private Creature creature;

    public ObstacleDetector(String name, Creature creature) {
        this.setCreature(creature);
        this.setName(name);
    }


    @Override
    public void accessMemoryObjects() {

        if (getVisionMO() == null)
            this.setVisionMO((MemoryObject) this.getInput("VISION"));


        if (getKnownObstaclesMO() == null)
            this.setKnownObstaclesMO((MemoryObject) this.getOutput("KNOWN_OBSTACLES"));
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        if (getVisionMO().getI() != null && getKnownObstaclesMO().getI() != null) {

            CopyOnWriteArrayList<Thing> vision = new CopyOnWriteArrayList<Thing>((List<Thing>) getVisionMO().getI());
            List<Thing> objects = new ArrayList<>();

            vision.sort(Comparator.comparing(a -> getCreature().calculateDistanceTo(a)));

            if (vision.size() != 0) {
                for (Thing t : vision) {
                    if ((t.getName().contains("Brick") || t.getName().contains("Delivery") || t.getName().contains("Food"))) {
                        objects.add(t);
                    } else {
                        for (Leaflet leaflet : getCreature().getLeaflets()) {
                            if (!leaflet.ifInLeaflet(t.getMaterial().getColorName())) {
                                objects.add(t);
                                break;
                            }
                        }
                    }
                }
            }

            getKnownObstaclesMO().setI(objects);

        }
    }

    public Creature getCreature() {
        return creature;
    }

    public void setCreature(Creature creature) {
        this.creature = creature;
    }

    public MemoryObject getVisionMO() {
        return visionMO;
    }

    public void setVisionMO(MemoryObject visionMO) {
        this.visionMO = visionMO;
    }

    public MemoryObject getKnownObstaclesMO() {
        return knownObstaclesMO;
    }

    public void setKnownObstaclesMO(MemoryObject knownObstaclesMO) {
        this.knownObstaclesMO = knownObstaclesMO;
    }
}
