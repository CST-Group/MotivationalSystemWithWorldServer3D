package codelets.behaviors;

import br.unicamp.cst.behavior.subsumption.SubsumptionAction;
import br.unicamp.cst.behavior.subsumption.SubsumptionArchitecture;

import java.awt.Point;
import java.awt.geom.Point2D;

import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import com.sun.org.apache.bcel.internal.classfile.Code;
import org.json.JSONException;
import org.json.JSONObject;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import memory.CreatureInnerSense;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

/**
 * @author Du
 */
public class EatClosestApple extends Codelet {

    private MemoryObject closestAppleMO;
    private MemoryObject innerSenseMO;
    private MemoryObject drivesMO;
    private int reachDistance;
    private MemoryObject handsMO;
    Thing closestApple;
    CreatureInnerSense cis;

    public EatClosestApple(int reachDistance) {
        this.reachDistance = reachDistance;
    }

    @Override
    public void accessMemoryObjects() {

        if (drivesMO == null)
            drivesMO = (MemoryObject) this.getInput(MotivationalCodelet.OUTPUT_DRIVE_MEMORY);

        if (closestAppleMO == null)
            closestAppleMO = (MemoryObject) this.getInput("CLOSEST_APPLE");

        if (innerSenseMO == null)
            innerSenseMO = (MemoryObject) this.getInput("INNER");

        if (handsMO == null)
            handsMO = (MemoryObject) this.getOutput("HANDS_EAT_APPLE");

    }

    @Override
    public void calculateActivation() {

        /*try {
            if (closestAppleMO.getI() != null) {
                setActivation(1);
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
                    message.put("OBJECT", appleName);
                    message.put("ACTION", "EATIT");
                    handsMO.setEvaluation(getActivation());
                    handsMO.setI(message.toString());

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
