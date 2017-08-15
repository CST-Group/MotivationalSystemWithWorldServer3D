package br.unicamp.mtwsapp.codelets.motivationalbehaviors;

import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import org.json.JSONException;
import org.json.JSONObject;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

/**
 * @author Du
 */
public class GoToApple extends Codelet {

    private MemoryObject knownApplesMO;
    private MemoryObject drivesMO;
    private MemoryObject legsMO;

    private int creatureBasicSpeed;
    private Creature creature;

    public GoToApple(String name, int creatureBasicSpeed, Creature creature) {
        this.setName(name);
        this.creatureBasicSpeed = creatureBasicSpeed;
        this.creature = creature;
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

        List<Thing> apples = Collections.synchronizedList((List<Thing>) knownApplesMO.getI());

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

                } else {
                    JSONObject message = new JSONObject();
                    try {
                        message.put("ACTION", "FORAGE");
                        legsMO.setI(message.toString());
                        legsMO.setEvaluation(getActivation());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

}
