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
import java.util.concurrent.CopyOnWriteArrayList;

import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import br.unicamp.mtwsapp.codelets.soarplanning.SoarPlan;
import br.unicamp.mtwsapp.codelets.soarplanning.SoarJewel;
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
    private MemoryObject jewelsCollectedMO;

    private int creatureBasicSpeed;
    private Creature creature;

    public GoToJewel(String name, int creatureBasicSpeed, Creature creature) {
        this.setName(name);
        this.setCreatureBasicSpeed(creatureBasicSpeed);
        this.setCreature(creature);
    }

    @Override
    public void accessMemoryObjects() {

        if (getDrivesMO() == null)
            setDrivesMO((MemoryObject) this.getInput(MotivationalCodelet.OUTPUT_DRIVE_MEMORY));

        if (getKnownJewels() == null)
            setKnownJewels((MemoryObject) this.getInput("KNOWN_JEWELS"));

        if (getLegsMO() == null)
            setLegsMO((MemoryObject) this.getOutput("LEGS_GO_JEWEL"));

        if (getPlanSelectedMO() == null) {
            setPlanSelectedMO((MemoryObject) this.getInput(PlanSelectionCodelet.OUPUT_SELECTED_PLAN_MO));
        }

        if (getInnerSenseMO() == null) {
            setInnerSenseMO((MemoryObject) this.getInput("INNER"));
        }

        if (getJewelsCollectedMO() == null) {
            setJewelsCollectedMO((MemoryObject) this.getInput("JEWELS_COLLECTED"));
        }


    }

    @Override
    public void calculateActivation() {
        Drive drive = (Drive) getDrivesMO().getI();

        try {
            if (drive != null) {
                Plan plan = (Plan) getPlanSelectedMO().getI();
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
    public void proc() {
        List<Thing> jewels = new CopyOnWriteArrayList<Thing>((List<Thing>) getKnownJewels().getI());

        double jewelX = 0;
        double jewelY = 0;

        Plan plan = (Plan) getPlanSelectedMO().getI();

        if (!jewels.isEmpty() && plan == null) {

            jewels.sort(Comparator.comparing(a -> getCreature().calculateDistanceTo(a)));

            Thing jewel = jewels.get(0);

            jewelX = jewel.getX1();
            jewelY = jewel.getY1();

            JSONObject message = new JSONObject();
            try {
                message.put("ACTION", "GOTO");
                message.put("X", (int) jewelX);
                message.put("Y", (int) jewelY);
                message.put("SPEED", getCreatureBasicSpeed());

                getLegsMO().setEvaluation(getActivation());
                getLegsMO().setI(message.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            if (plan != null) {
                try {

                    for (SoarJewel soarJewel : ((SoarPlan) plan.getContent()).getSoarJewels()) {

                        if (soarJewel.getCaptured() == 0) {
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
                    message.put("SPEED", getCreatureBasicSpeed());

                    getLegsMO().setEvaluation(getActivation());
                    getLegsMO().setI(message.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

                JSONObject message = new JSONObject();
                try {
                    message.put("ACTION", "FORAGE");
                    getLegsMO().setI(message.toString());
                    getLegsMO().setEvaluation(getActivation());

                } catch (JSONException e) {
                    e.printStackTrace();
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

    public MemoryObject getKnownJewels() {
        return knownJewels;
    }

    public void setKnownJewels(MemoryObject knownJewels) {
        this.knownJewels = knownJewels;
    }

    public MemoryObject getLegsMO() {
        return legsMO;
    }

    public void setLegsMO(MemoryObject legsMO) {
        this.legsMO = legsMO;
    }

    public MemoryObject getDrivesMO() {
        return drivesMO;
    }

    public void setDrivesMO(MemoryObject drivesMO) {
        this.drivesMO = drivesMO;
    }

    public MemoryObject getPlanSelectedMO() {
        return planSelectedMO;
    }

    public void setPlanSelectedMO(MemoryObject planSelectedMO) {
        this.planSelectedMO = planSelectedMO;
    }

    public MemoryObject getJewelsCollectedMO() {
        return jewelsCollectedMO;
    }

    public void setJewelsCollectedMO(MemoryObject jewelsCollectedMO) {
        this.jewelsCollectedMO = jewelsCollectedMO;
    }

    public int getCreatureBasicSpeed() {
        return creatureBasicSpeed;
    }

    public void setCreatureBasicSpeed(int creatureBasicSpeed) {
        this.creatureBasicSpeed = creatureBasicSpeed;
    }
}
