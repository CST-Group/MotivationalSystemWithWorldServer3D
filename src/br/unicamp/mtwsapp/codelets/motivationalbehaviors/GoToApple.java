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
import java.util.concurrent.CopyOnWriteArrayList;

import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

/**
 * @author Du
 */
public class GoToApple extends Codelet {

    private MemoryObject knownApplesMO;
    private MemoryObject drivesMO;
    private MemoryObject legsMO;
    private Creature creature;

    private int creatureBasicSpeed;

    public GoToApple(String name, int creatureBasicSpeed, Creature creature) {
        this.setName(name);
        this.creatureBasicSpeed = creatureBasicSpeed;
        setCreature(creature);
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
    public void proc() {

        if (knownApplesMO.getI() != null) {

            List<Thing> apples = new CopyOnWriteArrayList<Thing>((List<Thing>) knownApplesMO.getI());

            if (apples != null) {
                if (apples.size() > 0) {

                    apples.sort(Comparator.comparing(a -> getCreature().calculateDistanceTo(a)));
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

    public Creature getCreature() {
        return creature;
    }

    public void setCreature(Creature creature) {
        this.creature = creature;
    }
}
