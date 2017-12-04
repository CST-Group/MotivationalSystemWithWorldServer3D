/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unicamp.mtwsapp.codelets.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

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
    public void proc() {
        if (getVisionMO().getI() != null && getKnownJewelsMO().getI() != null) {

            CopyOnWriteArrayList<Thing> vision = new CopyOnWriteArrayList<>((List<Thing>) getVisionMO().getI());
            List<Thing> objects = new ArrayList<>();

            vision.sort(Comparator.comparing(a -> getCreature().calculateDistanceTo(a)));

            if (vision.size() != 0) {
                for (Thing t : vision) {
                    if(t.getName().contains("Jewel")) {
                        for (Leaflet leaflet : getCreature().getLeaflets()) {
                            if (leaflet.ifInLeaflet(t.getMaterial().getColorName())) {
                                objects.add(t);
                                break;
                            }
                        }
                    }
                }
            }

            getKnownJewelsMO().setI(objects);
        } else {
            getKnownJewelsMO().setI(new ArrayList<>());
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
