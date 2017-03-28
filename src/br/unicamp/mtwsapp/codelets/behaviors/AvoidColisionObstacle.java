/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unicamp.mtwsapp.codelets.behaviors;

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

/**
 *
 * @author Du
 */
public class AvoidColisionObstacle extends Codelet {

    private MemoryObject closestObstacleMO;
    private MemoryObject innerSenseMO;
    private MemoryObject drivesMO;
    private MemoryObject legsMO;
    private Creature creature;

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
    }

    @Override
    public void calculateActivation() {
        /*try {
            Thing brick = (Thing) closestObstacleMO.getI();

            if (brick != null) {
                setActivation(1);
            } else {

                setActivation(0);

            }

        } catch (CodeletActivationBoundsException ex) {
            Logger.getLogger(AvoidColisionObstacle.class.getName()).log(Level.SEVERE, null, ex);
        }*/

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
            try {

                message.put("OBJECT", obstacleName);
                message.put("ACTION", "AVOID");
                legsMO.setEvaluation(getActivation());
                legsMO.setI(message.toString());

            } catch (JSONException e) {

                e.printStackTrace();
            }
        } else {
            legsMO.setEvaluation(getActivation());
            legsMO.setI("");
        }
    }

}
