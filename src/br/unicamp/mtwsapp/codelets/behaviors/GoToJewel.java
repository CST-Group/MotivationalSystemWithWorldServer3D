/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unicamp.mtwsapp.codelets.behaviors;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;

import java.util.List;

import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import org.json.JSONException;
import org.json.JSONObject;
import ws3dproxy.model.Thing;

/**
 *
 * @author Du
 */
public class GoToJewel extends Codelet {

    private MemoryObject knownJewels;
    private MemoryObject legsMO;
    private MemoryObject drivesMO;

    private int creatureBasicSpeed;

    public GoToJewel(int creatureBasicSpeed) {
        this.creatureBasicSpeed = creatureBasicSpeed;
    }

    @Override
    public void accessMemoryObjects() {

        if (drivesMO == null)
            drivesMO = (MemoryObject) this.getInput(MotivationalCodelet.OUTPUT_DRIVE_MEMORY);

        if(knownJewels == null)
            knownJewels = (MemoryObject) this.getInput("KNOWN_JEWELS");

        if(legsMO == null)
            legsMO = (MemoryObject) this.getOutput("LEGS_GO_JEWEL");

    }

    @Override
    public void calculateActivation() {
        /*try {

            if ((creature.getAttributes().getFuel() / 1000) >= 0.4) {
                List<Thing> jewels = (List<Thing>) knownJewels.getI();
                if (!jewels.isEmpty()) {
                    setActivation(1);

                } else {
                    setActivation(0);
                }
            } else {
                setActivation(0);
            }

        } catch (CodeletActivationBoundsException ex) {
            Logger.getLogger(GoToApple.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        synchronized (drivesMO) {
            Drive drive = (Drive) drivesMO.getI();
            try {
                if (drive != null)
                    setActivation(drive.getActivation());
                else
                    setActivation(0);
            } catch (CodeletActivationBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void proc() {

        List<Thing> jewels = (List<Thing>) knownJewels.getI();
        synchronized (legsMO) {
            synchronized (jewels) {
                if (!jewels.isEmpty()) {
                    double jewelX = 0;
                    double jewelY = 0;

                    Thing jewel = jewels.get(0);

                    try {
                        jewelX = jewel.getX1();
                        jewelY = jewel.getY1();

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
                }
                else{
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
