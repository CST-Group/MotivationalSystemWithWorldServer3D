package br.unicamp.mtwsapp.codelets.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

/**
 * @author Du
 */
public class AppleDetector extends Codelet {

    private MemoryObject visionMO;
    private MemoryObject knownApplesMO;
    private Creature creature;
    private MemoryObject hiddenObjectsMO;

    public AppleDetector(String name, Creature creature) {
        this.setCreature(creature);
        this.setName(name);
    }

    @Override
    public void accessMemoryObjects() {

        if (getVisionMO() == null)
            this.setVisionMO((MemoryObject) this.getInput("VISION"));

        if (getKnownApplesMO() == null)
            this.setKnownApplesMO((MemoryObject) this.getOutput("KNOWN_APPLES"));

        if (getHiddenObjectsMO() == null)
            this.setHiddenObjectsMO((MemoryObject) this.getInput("HIDDEN_THINGS"));
    }

    @Override
    public void proc() {
        if ((getVisionMO().getI() != null && getKnownApplesMO().getI() != null)) {

            List<Thing> vision = new CopyOnWriteArrayList<>((List<Thing>) getVisionMO().getI());
            List<Thing> objects = new ArrayList<>();

            vision.sort(Comparator.comparing(a -> getCreature().calculateDistanceTo(a)));

            if (vision.size() != 0) {
                for (Thing t : vision) {
                    if (t.getName().contains("Food")) {
                        objects.add(t);
                    }
                }
            }

            if(getHiddenObjectsMO().getI() != null) {
                List<Thing> hiddenThings = (List<Thing>) getHiddenObjectsMO().getI();
                objects.addAll(hiddenThings);
            }

            getKnownApplesMO().setI(objects);

        } else {
            if(getHiddenObjectsMO().getI() != null) {
                List<Thing> objects = new ArrayList<>();
                List<Thing> hiddenThings = (List<Thing>) getHiddenObjectsMO().getI();
                objects.addAll(hiddenThings);
                getKnownApplesMO().setI(objects);
            } else {
                getKnownApplesMO().setI(new ArrayList<>());
            }
        }

    }

    @Override
    public void calculateActivation() {

    }

    public MemoryObject getVisionMO() {
        return visionMO;
    }

    public void setVisionMO(MemoryObject visionMO) {
        this.visionMO = visionMO;
    }

    public MemoryObject getKnownApplesMO() {
        return knownApplesMO;
    }

    public void setKnownApplesMO(MemoryObject knownApplesMO) {
        this.knownApplesMO = knownApplesMO;
    }

    public Creature getCreature() {
        return creature;
    }

    public void setCreature(Creature creature) {
        this.creature = creature;
    }

    public MemoryObject getHiddenObjectsMO() {
        return hiddenObjectsMO;
    }

    public void setHiddenObjectsMO(MemoryObject hiddenObjectsMO) {
        this.hiddenObjectsMO = hiddenObjectsMO;
    }
}//end class

