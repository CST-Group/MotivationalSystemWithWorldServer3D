/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unicamp.mtwsapp.codelets.motivationalbehaviors;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;

import java.awt.Point;
import java.awt.geom.Point2D;

import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import org.json.JSONException;
import org.json.JSONObject;
import ws3dproxy.model.Thing;

/**
 * @author Du
 */
public class GetClosestJewel extends Codelet {

    private MemoryObject closestJewelMO;
    private MemoryObject innerSenseMO;
    private MemoryObject drivesMO;
    private int reachDistance;
    private MemoryObject handsMO;
    Thing closestJewel;
    CreatureInnerSense cis;

    public GetClosestJewel(String name, int reachDistance) {
        this.setName(name);
        this.reachDistance = reachDistance;
    }

    @Override
    public void accessMemoryObjects() {
        if (drivesMO == null)
            drivesMO = (MemoryObject) this.getInput(MotivationalCodelet.OUTPUT_DRIVE_MEMORY);

        if (closestJewelMO == null)
            closestJewelMO = (MemoryObject) this.getInput("CLOSEST_JEWEL");

        if (handsMO == null)
            handsMO = (MemoryObject) this.getOutput("HANDS_GET_JEWEL");

        if(innerSenseMO == null){
            innerSenseMO = (MemoryObject) this.getInput("INNER");
        }
    }


    @Override
    public void calculateActivation() {
        /*try {
            if (closestJewelMO.getI() !=  null && (creature.getAttributes().getFuel()/1000) >= 0.4) {
                setActivation(1);
            } else {
                setActivation(0);
            }

        } catch (CodeletActivationBoundsException ex) {
            Logger.getLogger(GoToApple.class.getName()).log(Level.SEVERE, null, ex);
        }*/


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

    @Override
    public synchronized void proc() {
        String jewelName = "";
        closestJewel = (Thing) closestJewelMO.getI();
        cis = (CreatureInnerSense) innerSenseMO.getI();
        //Find distance between closest apple and self
        //If closer than reachDistance, eat the apple

        if (closestJewel != null) {
            double jewelX = 0;
            double jewelY = 0;
            try {
                jewelX = closestJewel.getX1();
                jewelY = closestJewel.getY1();
                jewelName = closestJewel.getName();

            } catch (Exception e) {
                e.printStackTrace();
            }

            double selfX = cis.getPosition().getX();
            double selfY = cis.getPosition().getY();

            Point2D pApple = new Point();
            pApple.setLocation(jewelX, jewelY);

            Point2D pSelf = new Point();
            pSelf.setLocation(selfX, selfY);

            double distance = pSelf.distance(pApple);
            JSONObject message = new JSONObject();
            try {
                if (distance < reachDistance) { //eat it	
                    message.put("OBJECT", jewelName);
                    message.put("ACTION", "PICKUP");
                    handsMO.setEvaluation(getActivation());
                    handsMO.setI(message.toString());

                } else {
                    handsMO.setEvaluation(getActivation());
                    handsMO.setI("");    //nothing
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            handsMO.setEvaluation(getActivation());
            handsMO.setI("");    //nothing
        }
    }
}
