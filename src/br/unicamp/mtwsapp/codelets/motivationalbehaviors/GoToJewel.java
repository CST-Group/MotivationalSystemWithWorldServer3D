/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unicamp.mtwsapp.codelets.motivationalbehaviors;


import br.unicamp.cst.bindings.soar.Plan;
import br.unicamp.cst.bindings.soar.PlanSelectionCodelet;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;

import java.util.*;

import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import br.unicamp.mtwsapp.codelets.soarplanning.SoarPlanSelectionCodelet;
import br.unicamp.mtwsapp.codelets.soarplanning.SoarPlan;
import br.unicamp.mtwsapp.codelets.soarplanning.SoarJewel;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import org.json.JSONException;
import org.json.JSONObject;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

/**
 * @author Du
 */
public class GoToJewel extends Codelet {

    private MemoryObject knownJewels;
    private MemoryObject legsMO;
    private MemoryObject drivesMO;
    private MemoryObject planSelectedMO;
    private MemoryObject innerSenseMO;

    private int creatureBasicSpeed;
    private Creature creature;

    public GoToJewel(String name, int creatureBasicSpeed, Creature creature) {
        this.setName(name);
        this.creatureBasicSpeed = creatureBasicSpeed;
        this.setCreature(creature);
    }

    @Override
    public void accessMemoryObjects() {

        if (drivesMO == null)
            drivesMO = (MemoryObject) this.getInput(MotivationalCodelet.OUTPUT_DRIVE_MEMORY);

        if (knownJewels == null)
            knownJewels = (MemoryObject) this.getInput("KNOWN_JEWELS");

        if (legsMO == null)
            legsMO = (MemoryObject) this.getOutput("LEGS_GO_JEWEL");

        if (planSelectedMO == null) {
            planSelectedMO = (MemoryObject) this.getInput(PlanSelectionCodelet.OUPUT_SELECTED_PLAN_MO);
        }

        if (getInnerSenseMO() == null) {
            setInnerSenseMO((MemoryObject) this.getInput("INNER"));
        }


    }

    @Override
    public void calculateActivation() {
        Drive drive = (Drive) drivesMO.getI();

        try {
            if (drive != null) {
                Plan plan = (Plan) planSelectedMO.getI();
                if (plan != null) {
                    setActivation(0.5 + drive.getPriority());
                } else {
                    setActivation(drive.getActivation());
                }

            } else {
                setActivation(0);
            }
        } catch (CodeletActivationBoundsException e) {
            e.printStackTrace();
        }

    }

    @Override
    public synchronized void proc() {
        List<Thing> jewels = Collections.synchronizedList((List<Thing>) knownJewels.getI());
        double jewelX = 0;
        double jewelY = 0;

        synchronized (legsMO) {
            synchronized (jewels) {
                Plan plan = (Plan) planSelectedMO.getI();

                if (!jewels.isEmpty() && plan == null) {
                    Thing jewel = jewels.get(0);

                    jewelX = jewel.getX1();
                    jewelY = jewel.getY1();

                    JSONObject message = new JSONObject();
                    try {
                        message.put("ACTION", "GOTO");
                        message.put("X", (int) jewelX);
                        message.put("Y", (int) jewelY);
                        message.put("SPEED", creatureBasicSpeed);

                        legsMO.setEvaluation(getActivation());
                        legsMO.setI(message.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (plan != null) {
                        try {

                            for (SoarJewel soarJewel : ((SoarPlan)plan.getContent()).getSoarJewels()) {
                                CreatureInnerSense cis = (CreatureInnerSense) innerSenseMO.getI();
                                Optional<Thing> first = cis.getThingsInWorld().stream().filter(t -> t.getName().equals(soarJewel.getName())).findFirst();

                                if (first.isPresent()) {
                                    jewelX = soarJewel.getX1();
                                    jewelY = soarJewel.getY1();
                                    break;
                                }
                            }

                            JSONObject message = new JSONObject();

                            message.put("ACTION", "GOTO");
                            message.put("X", (int) jewelX);
                            message.put("Y", (int) jewelY);
                            message.put("FROMPLAN", true);
                            message.put("SPEED", creatureBasicSpeed);

                            legsMO.setEvaluation(getActivation());
                            legsMO.setI(message.toString());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {

                        JSONObject message = new JSONObject();
                        try {
                            message.put("ACTION", "FORAGE");
                            legsMO.setI(message.toString());
                            legsMO.setEvaluation(getActivation());

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public Creature getCreature() {
        return creature;
    }

    public void setCreature(Creature creature) {
        this.creature = creature;
    }

    public MemoryObject getInnerSenseMO() {
        return innerSenseMO;
    }

    public void setInnerSenseMO(MemoryObject innerSenseMO) {
        this.innerSenseMO = innerSenseMO;
    }
}
