package br.unicamp.mtwsapp.codelets.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public AppleDetector(Creature creature) {
        this.creature = creature;
    }

    @Override
    public void accessMemoryObjects() {
        synchronized (this) {
            if (visionMO == null)
                this.visionMO = (MemoryObject) this.getInput("VISION");
        }

        if (knownApplesMO == null)
            this.knownApplesMO = (MemoryObject) this.getOutput("KNOWN_APPLES");

        if (hiddenObjectsMO == null)
            this.hiddenObjectsMO = (MemoryObject) this.getInput("HIDDEN_THINGS");
    }

    @Override
    public void proc() {
        CopyOnWriteArrayList<Thing> vision;
        List<Thing> known;
        synchronized (visionMO) {
            //vision = Collections.synchronizedList((List<Thing>) visionMO.getI());

            vision = new CopyOnWriteArrayList((List<Thing>) visionMO.getI());
            known = Collections.synchronizedList((List<Thing>) knownApplesMO.getI());

            if (vision.size() != 0) {
                Comparator<Thing> comparator = new Comparator<Thing>() {
                    @Override
                    public int compare(Thing thing1, Thing thing2) {
                        int nearThing = creature.calculateDistanceTo(thing2) < creature.calculateDistanceTo(thing1) ? 1 : 0;
                        return nearThing;
                    }
                };

                Collections.sort(vision, comparator);
            }

            //known = new CopyOnWriteArrayList((List<Thing>) knownApplesMO.getI());
            synchronized (vision) {
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
            }

            List<Thing> hiddenThings = (List<Thing>) hiddenObjectsMO.getI();

            for (Thing thing: hiddenThings) {
                if(thing.hidden && !known.stream().anyMatch(x->x.getName().equals(thing.getName())))
                    known.add(thing);
            }

            /*hiddenThings.stream().forEach(thing -> {
                if (known.stream().filter(k-> k.getName().equals(thing.getName()) && k.hidden).collect(Collectors.toList()).size() == 0)
                    known.add(thing);
            });*/


            //if(!known.containsAll(Collections.synchronizedList(hiddenThings.stream().filter( t -> t.hidden).collect(Collectors.toList()))))
            //    known.addAll(Collections.synchronizedList(hiddenThings.stream().filter( t -> t.hidden).collect(Collectors.toList())));
        }

    }// end proc

    @Override
    public void calculateActivation() {

    }

}//end class

