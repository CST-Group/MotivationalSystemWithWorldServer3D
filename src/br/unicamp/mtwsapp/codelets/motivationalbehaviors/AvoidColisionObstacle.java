/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unicamp.mtwsapp.codelets.motivationalbehaviors;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;

import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import org.json.JSONException;
import org.json.JSONObject;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

import java.util.List;

/**
 *
 * @author Du
 */
public class AvoidColisionObstacle extends Codelet {

    private MemoryObject closestObstacleMO;
    private MemoryObject innerSenseMO;
    private MemoryObject drivesMO;
    private MemoryObject legsMO;
    private MemoryObject handsMO;
    private MemoryObject knownJewelsMO;
    private MemoryObject hiddenObjectsMO;

    Thing closestObstacle;
    CreatureInnerSense cis;

    public AvoidColisionObstacle() {
    }

    @Override
    public void accessMemoryObjects() {

        if (drivesMO == null)
            drivesMO = (MemoryObject) this.getInput(MotivationalCodelet.OUTPUT_DRIVE_MEMORY);

        if(closestObstacle == null)
            closestObstacleMO = (MemoryObject) this.getInput("CLOSEST_OBSTACLE");

        if(innerSenseMO == null)
            innerSenseMO = (MemoryObject) this.getInput("INNER");

        if(legsMO == null)
            legsMO = (MemoryObject) this.getOutput("LEGS_AVOID_DANGER");

        if(handsMO == null)
            handsMO = (MemoryObject) this.getOutput("HANDS_AVOID_DANGER");

        if(knownJewelsMO == null)
            knownJewelsMO = (MemoryObject) this.getInput("KNOWN_JEWELS");

        if(hiddenObjectsMO == null)
            hiddenObjectsMO = (MemoryObject) this.getOutput("HIDDEN_THINGS");


    }

    @Override
    public void calculateActivation() {
        synchronized (drivesMO) {
            Drive drive = (Drive) drivesMO.getI();
            try {
                if (drive != null) {
                    setActivation(drive.getActivation());
                } else {
                    setActivation(0);
                }
            } catch (CodeletActivationBoundsException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void proc() {
        String obstacleName = "";
        closestObstacle = (Thing) closestObstacleMO.getI();
        cis = (CreatureInnerSense) innerSenseMO.getI();

        //Find distance between closest apple and self
        //If closer than reachDistance, eat the apple
        if (closestObstacle != null) {
            JSONObject message = new JSONObject();
            if(closestObstacle.getName().contains("Brick")) {
                try {

                    message.put("OBJECT", obstacleName);
                    message.put("ACTION", "AVOID");
                    legsMO.setEvaluation(getActivation());
                    legsMO.setI(message.toString());
                    handsMO.setEvaluation(getActivation());
                    handsMO.setI("");

                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }
            else{

                if(closestObstacle.getName().contains("Jewel")){
                    List<Thing> jewels = (List<Thing>) knownJewelsMO.getI();
                    if(!jewels.contains(closestObstacle)){
                        try {
                            message.put("OBJECT", closestObstacle.getName());
                            message.put("ACTION", "BURY");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        handsMO.setEvaluation(getActivation());
                        handsMO.setI(message.toString());
                        legsMO.setEvaluation(getActivation());
                        legsMO.setI("");
                    }
                    else{
                        try {
                            message.put("OBJECT", closestObstacle.getName());
                            message.put("ACTION", "PICKUP");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        handsMO.setEvaluation(getActivation());
                        handsMO.setI(message.toString());
                        legsMO.setEvaluation(getActivation());
                        legsMO.setI("");
                    }
                }
                else{
                    try {
                        message.put("OBJECT", closestObstacle.getName());
                        message.put("ACTION", "BURY");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    closestObstacle.hidden = true;

                    List<Thing> things = (List<Thing>) hiddenObjectsMO.getI();

                    if(things.size() == 0 )
                    {
                        things.add(closestObstacle);
                    }
                    else {
                        for (int i = 0; i < things.size(); i++) {
                            if (!things.get(i).getName().equals(closestObstacle.getName())) {
                                things.add(closestObstacle);
                            }
                        }
                    }

                    handsMO.setEvaluation(getActivation());
                    handsMO.setI(message.toString());

                    legsMO.setEvaluation(getActivation());
                    legsMO.setI("");
                }
            }
        } else {
            legsMO.setEvaluation(getActivation());
            legsMO.setI("");
            handsMO.setEvaluation(getActivation());
            handsMO.setI("");
        }
    }

}
