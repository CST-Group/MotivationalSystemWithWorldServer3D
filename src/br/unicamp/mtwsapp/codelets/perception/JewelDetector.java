/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unicamp.mtwsapp.codelets.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;

import java.util.ArrayList;
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

    public JewelDetector(String name, Creature creature) {
        this.setCreature(creature);
        this.setName(name);
    }

    @Override
    public void accessMemoryObjects() {

        if (getVisionMO() == null)
            this.setVisionMO((MemoryObject) this.getInput("VISION"));


        if (getKnownJewelsMO() == null)
            this.setKnownJewelsMO((MemoryObject) this.getOutput("KNOWN_JEWELS"));
    }

    @Override
    public synchronized void proc() {
        List<Thing> vision = null;
        List<Thing> known = null;

        if (getVisionMO().getI() != null && getKnownJewelsMO().getI() != null) {
            vision = new ArrayList<>(Collections.synchronizedList((List<Thing>) getVisionMO().getI()));
            known = Collections.synchronizedList((List<Thing>) getKnownJewelsMO().getI());

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
                            for (Leaflet leaflet : getCreature().getLeaflets()) {
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

    public MemoryObject getKnownJewelsMO() {
        return knownJewelsMO;
    }

    public void setKnownJewelsMO(MemoryObject knownJewelsMO) {
        this.knownJewelsMO = knownJewelsMO;
    }

    public Creature getCreature() {
        return creature;
    }

    public void setCreature(Creature creature) {
        this.creature = creature;
    }
}
