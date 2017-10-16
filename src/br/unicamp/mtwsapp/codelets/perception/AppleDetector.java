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
    public synchronized void proc() {
        List<Thing> vision = Collections.synchronizedList((List<Thing>) getVisionMO().getI());
        List<Thing> known = Collections.synchronizedList((List<Thing>) getKnownApplesMO().getI());;

        if (vision.size() != 0) {
            Comparator<Thing> comparator = new Comparator<Thing>() {
                @Override
                public int compare(Thing thing1, Thing thing2) {
                    int nearThing = getCreature().calculateDistanceTo(thing2) < getCreature().calculateDistanceTo(thing1) ? 1 : 0;
                    return nearThing;
                }
            };

            Collections.sort(vision, comparator);
        }

        //known = new CopyOnWriteArrayList((List<Thing>) knownApplesMO.getI());

        if (vision.size() != 0) {
            for (Thing t : vision) {
                boolean found = false;
                synchronized (known) {
                    CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(known);
                    for (Thing e : myknown) {
                        if (t.getName().equals(e.getName())) {
                            found = true;
                            break;
                        }
                    }
                    if (found == false && t.getName().contains("Food")) {
                        known.add(t);
                    }
                }

            }
        } else {
            known.removeAll(known);
        }

        List<Thing> hiddenThings = (List<Thing>) getHiddenObjectsMO().getI();

        for (Thing thing : hiddenThings) {
            if (!known.stream().anyMatch(x -> x.getName().equals(thing.getName())))
                known.add(thing);
        }

        getKnownApplesMO().setI(known);

    }// end proc

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

