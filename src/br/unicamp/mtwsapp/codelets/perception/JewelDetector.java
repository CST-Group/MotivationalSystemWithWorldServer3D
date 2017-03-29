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
import ws3dproxy.model.Leaflet;
import ws3dproxy.model.Thing;

/**
 * @author Du
 */
public class JewelDetector extends Codelet {

    private MemoryObject visionMO;
    private MemoryObject knownJewelsMO;
    private Creature creature;

    public JewelDetector(Creature creature) {
        this.creature = creature;
    }

    @Override
    public void accessMemoryObjects() {
        synchronized (this) {
            if (visionMO == null)
                this.visionMO = (MemoryObject) this.getInput("VISION");
        }

        if (knownJewelsMO == null)
            this.knownJewelsMO = (MemoryObject) this.getOutput("KNOWN_JEWELS");
    }

    @Override
    public void proc() {
        CopyOnWriteArrayList<Thing> vision;
        List<Thing> known;
        synchronized (visionMO) {
            if (visionMO.getI() != null && knownJewelsMO.getI() != null) {
                vision = new CopyOnWriteArrayList((List<Thing>) visionMO.getI());
                known = Collections.synchronizedList((List<Thing>) knownJewelsMO.getI());

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
                                if (found == false && t.getName().contains("Jewel")) {
                                    for (Leaflet leaflet : creature.getLeaflets()) {
                                        if (leaflet.ifInLeaflet(t.getMaterial().getColorName())) {
                                            known.add(t);
                                            break;
                                        }
                                    }
                                }
                            }

                        }
                    } else {
                        known.removeAll(known);
                    }

                }
            }
        }
    }// end proc

    @Override
    public void calculateActivation() {

    }
}
