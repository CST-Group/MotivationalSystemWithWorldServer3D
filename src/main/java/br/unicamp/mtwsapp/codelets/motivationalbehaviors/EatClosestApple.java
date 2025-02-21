package br.unicamp.mtwsapp.codelets.motivationalbehaviors;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import org.json.JSONException;
import org.json.JSONObject;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;

import ws3dproxy.model.Thing;

/**
 * @author Du
 */
public class EatClosestApple extends Codelet {

    private MemoryObject closestAppleMO;
    private MemoryObject innerSenseMO;
    private MemoryObject drivesMO;
    private MemoryObject hiddenApplesMO;
    private int reachDistance;
    private MemoryObject handsMO;
    Thing closestApple;
    CreatureInnerSense cis;

    public EatClosestApple(String name, int reachDistance) {
        this.setName(name);
        this.reachDistance = reachDistance;
    }

    @Override
    public void accessMemoryObjects() {

        if (drivesMO == null)
            drivesMO = (MemoryObject) this.getInput(MotivationalCodelet.OUTPUT_DRIVE_MEMORY);

        if (closestAppleMO == null)
            closestAppleMO = (MemoryObject) this.getInput("CLOSEST_APPLE");

        if (handsMO == null)
            handsMO = (MemoryObject) this.getOutput("HANDS_EAT_APPLE");

        if (hiddenApplesMO == null)
            hiddenApplesMO = (MemoryObject) this.getInput("HIDDEN_THINGS");

        if(innerSenseMO == null){
            innerSenseMO = (MemoryObject) this.getInput("INNER");
        }

    }

    @Override
    public void calculateActivation() {

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

    @Override
    public void proc() {
        String appleName = "";
        closestApple = (Thing) closestAppleMO.getI();
        cis = (CreatureInnerSense) innerSenseMO.getI();

        //Find distance between closest apple and self
        //If closer than reachDistance, eat the apple
        if (closestApple != null) {
            double appleX = 0;
            double appleY = 0;
            try {
                appleX = closestApple.getX1();
                appleY = closestApple.getY1();
                appleName = closestApple.getName();

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            double selfX = cis.getPosition().getX();
            double selfY = cis.getPosition().getY();

            Point2D pApple = new Point();
            pApple.setLocation(appleX, appleY);

            Point2D pSelf = new Point();
            pSelf.setLocation(selfX, selfY);

            double distance = pSelf.distance(pApple);
            JSONObject message = new JSONObject();
            try {
                if (distance < reachDistance) { //eat it
                    if (closestApple.hidden) {
                        message.put("OBJECT", appleName);
                        message.put("ACTION", "UNEARTH");
                        handsMO.setEvaluation(getActivation());
                        handsMO.setI(message.toString());
                    } else {
                        message.put("OBJECT", appleName);
                        message.put("ACTION", "EATIT");
                        handsMO.setEvaluation(getActivation());
                        handsMO.setI(message.toString());
                    }

                } else {
                    handsMO.setEvaluation(getActivation());
                    handsMO.setI("");
                }

            } catch (JSONException e) {

                e.printStackTrace();
            }
        } else {
            handsMO.setEvaluation(getActivation());
            handsMO.setI("");
        }

    }

}
