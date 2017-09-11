/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unicamp.mtwsapp.codelets.motivationalbehaviors;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import br.unicamp.mtwsapp.codelets.soarplanning.SoarPickUpRemainingJewels;
import br.unicamp.mtwsapp.codelets.soarplanning.SoarPlanningCodelet;
import org.json.JSONException;
import org.json.JSONObject;
import ws3dproxy.model.Thing;

/**
 * @author Du
 */
public class GoToJewel extends Codelet {

    private MemoryObject knownJewels;
    private MemoryObject legsMO;
    private MemoryObject drivesMO;
    private MemoryObject nextActionMO;

    private int creatureBasicSpeed;

    public GoToJewel(String name, int creatureBasicSpeed) {
        this.setName(name);
        this.creatureBasicSpeed = creatureBasicSpeed;
    }

    @Override
    public void accessMemoryObjects() {

        if (drivesMO == null)
            drivesMO = (MemoryObject) this.getInput(MotivationalCodelet.OUTPUT_DRIVE_MEMORY);

        if (knownJewels == null)
            knownJewels = (MemoryObject) this.getInput("KNOWN_JEWELS");

        if (legsMO == null)
            legsMO = (MemoryObject) this.getOutput("LEGS_GO_JEWEL");

        if(nextActionMO == null){
            nextActionMO = (MemoryObject) this.getInput(SoarPlanningCodelet.OUTPUT_COMMAND_MO);
        }

    }

    @Override
    public void calculateActivation() {
        Drive drive = (Drive) drivesMO.getI();

        try {
            if (drive != null) {
                List<Object> nextAction = (List<Object>)nextActionMO.getI();
                if (nextAction != null && nextAction.size() > 0){
                    setActivation(0.5 + drive.getPriority());
                }
                else{
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
        synchronized (legsMO) {
            synchronized (jewels) {
                if (!jewels.isEmpty()) {
                    double jewelX = 0;
                    double jewelY = 0;

                    Thing jewel = jewels.get(0);

                    try {
                        List<Object> nextAction = (List<Object>)nextActionMO.getI();
                        if (nextAction != null && nextAction.size() > 0){
                            SoarPickUpRemainingJewels soarPickUpRemainingJewels = (SoarPickUpRemainingJewels) nextAction.get(0);
                            jewelX = soarPickUpRemainingJewels.x1;
                            jewelY = soarPickUpRemainingJewels.y1;
                        } else{
                            jewelX = jewel.getX1();
                            jewelY = jewel.getY1();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

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
