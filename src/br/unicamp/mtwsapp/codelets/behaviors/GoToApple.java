package br.unicamp.mtwsapp.codelets.behaviors;

import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import org.json.JSONException;
import org.json.JSONObject;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;

import java.util.List;

import ws3dproxy.model.Thing;

/**
 * @author Du
 */
public class GoToApple extends Codelet {

    private MemoryObject knownApplesMO;
    private MemoryObject drivesMO;
    private MemoryObject legsMO;
    private int creatureBasicSpeed;

    public GoToApple(int creatureBasicSpeed) {
        this.creatureBasicSpeed = creatureBasicSpeed;
    }

    @Override
    public void accessMemoryObjects() {

        if (drivesMO == null)
            drivesMO = (MemoryObject) this.getInput(MotivationalCodelet.OUTPUT_DRIVE_MEMORY);

        if (knownApplesMO == null)
            knownApplesMO = (MemoryObject) this.getInput("KNOWN_APPLES");

        if (legsMO == null)
            legsMO = (MemoryObject) this.getOutput("LEGS_GO_APPLE");
    }

    @Override
    public void calculateActivation() {
        /*List<Thing> apples = (List<Thing>) knownApplesMO.getI();
        try {

            if ((creature.getAttributes().getFuel() / 1000) >= 0.4) {
                setActivation(0);
            } else {
                if (!apples.isEmpty()) {
                    setActivation(1);
                } else {
                    setActivation(0);
                }
            }

        } catch (CodeletActivationBoundsException ex) {
            Logger.getLogger(GoToApple.class.getName()).log(Level.SEVERE, null, ex);
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
        // Find distance between creature and closest apple
        //If far, go towards it
        //If close, stops

        List<Thing> apples = (List<Thing>) knownApplesMO.getI();

        synchronized (legsMO) {
            synchronized (apples) {
                if (!apples.isEmpty()) {
                    double appleX = 0;
                    double appleY = 0;
                    try {
                        appleX = apples.get(0).getX1();
                        appleY = apples.get(0).getY1();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    JSONObject message = new JSONObject();
                    try {

                        message.put("ACTION", "GOTO");
                        message.put("X", (int) appleX);
                        message.put("Y", (int) appleY);
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
