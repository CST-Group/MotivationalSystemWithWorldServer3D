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

import java.awt.Point;
import java.awt.geom.Point2D;

import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
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
public class GetClosestJewel extends Codelet {

    private MemoryObject closestJewelMO;
    private MemoryObject innerSenseMO;
    private MemoryObject drivesMO;
    private MemoryObject planSelectedMO;
    private MemoryObject jewelsCollectedMO;

    private int reachDistance;
    private MemoryObject handsMO;
    private Thing closestJewel;
    private CreatureInnerSense cis;
    private Creature creature;

    public GetClosestJewel(String name, int reachDistance, Creature creature) {
        this.setName(name);
        this.setReachDistance(reachDistance);
        this.setCreature(creature);
    }

    @Override
    public void accessMemoryObjects() {
        if (getDrivesMO() == null)
            setDrivesMO((MemoryObject) this.getInput(MotivationalCodelet.OUTPUT_DRIVE_MEMORY));

        if (getClosestJewelMO() == null)
            setClosestJewelMO((MemoryObject) this.getInput("CLOSEST_JEWEL"));

        if (getHandsMO() == null)
            setHandsMO((MemoryObject) this.getOutput("HANDS_GET_JEWEL"));

        if (getInnerSenseMO() == null) {
            setInnerSenseMO((MemoryObject) this.getInput("INNER"));
        }

        if (getPlanSelectedMO() == null) {
            setPlanSelectedMO((MemoryObject) this.getInput(PlanSelectionCodelet.OUPUT_SELECTED_PLAN_MO));
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
        String jewelName = "";
        setClosestJewel((Thing) getClosestJewelMO().getI());
        setCis((CreatureInnerSense) getInnerSenseMO().getI());

        double jewelX = 0;
        double jewelY = 0;

        if (getClosestJewel() != null) {
            try {
                Plan plan = (Plan) getPlanSelectedMO().getI();
                if (plan != null) {

                    for (SoarJewel soarJewel : ((SoarPlan) plan.getContent()).getSoarJewels()) {

                        if (soarJewel.getCaptured() == 0) {
                            jewelX = soarJewel.getX1();
                            jewelY = soarJewel.getY1();
                            break;
                        }
                    }
                } else {
                    jewelX = getClosestJewel().getX1();
                    jewelY = getClosestJewel().getY1();
                    jewelName = getClosestJewel().getName();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            double selfX = getCis().getPosition().getX();
            double selfY = getCis().getPosition().getY();

            Point2D pApple = new Point();
            pApple.setLocation(jewelX, jewelY);

            Point2D pSelf = new Point();
            pSelf.setLocation(selfX, selfY);

            double distance = pSelf.distance(pApple);
            JSONObject message = new JSONObject();
            try {
                if (distance < getReachDistance()) { //eat it
                    message.put("OBJECT", jewelName);
                    message.put("ACTION", "PICKUP");
                    getHandsMO().setEvaluation(getActivation());
                    getHandsMO().setI(message.toString());

                } else {
                    getHandsMO().setEvaluation(getActivation());
                    getHandsMO().setI("");    //nothing
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            getHandsMO().setEvaluation(getActivation());
            getHandsMO().setI("");    //nothing
        }
    }

    public double calculateDistanceToJewel(SoarJewel soarJewel, Creature creature) {

        double distance = Math.sqrt(Math.pow((creature.getPosition().getX() - soarJewel.getX1()), 2) +
                Math.pow((creature.getPosition().getY() - soarJewel.getY1()), 2));

        return distance;

    }

    public Creature getCreature() {
        return creature;
    }

    public void setCreature(Creature creature) {
        this.creature = creature;
    }

    public MemoryObject getClosestJewelMO() {
        return closestJewelMO;
    }

    public void setClosestJewelMO(MemoryObject closestJewelMO) {
        this.closestJewelMO = closestJewelMO;
    }

    public MemoryObject getInnerSenseMO() {
        return innerSenseMO;
    }

    public void setInnerSenseMO(MemoryObject innerSenseMO) {
        this.innerSenseMO = innerSenseMO;
    }

    public MemoryObject getDrivesMO() {
        return drivesMO;
    }

    public void setDrivesMO(MemoryObject drivesMO) {
        this.drivesMO = drivesMO;
    }

    public int getReachDistance() {
        return reachDistance;
    }

    public void setReachDistance(int reachDistance) {
        this.reachDistance = reachDistance;
    }

    public MemoryObject getHandsMO() {
        return handsMO;
    }

    public void setHandsMO(MemoryObject handsMO) {
        this.handsMO = handsMO;
    }

    public Thing getClosestJewel() {
        return closestJewel;
    }

    public void setClosestJewel(Thing closestJewel) {
        this.closestJewel = closestJewel;
    }

    public CreatureInnerSense getCis() {
        return cis;
    }

    public void setCis(CreatureInnerSense cis) {
        this.cis = cis;
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
}
