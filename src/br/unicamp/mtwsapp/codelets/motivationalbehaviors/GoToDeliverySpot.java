package br.unicamp.mtwsapp.codelets.motivationalbehaviors;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import org.json.JSONException;
import org.json.JSONObject;
import ws3dproxy.model.Creature;

/**
 * @author Du
 */

public class GoToDeliverySpot extends Codelet {

    private MemoryObject deliverySpotMO;
    private MemoryObject legsMO;
    private MemoryObject drivesMO;
    private MemoryObject innerSenseMO;

    private int creatureBasicSpeed;
    private Creature creature;

    public GoToDeliverySpot(String name, int creatureBasicSpeed, Creature creature) {
        this.setName(name);
        this.setCreatureBasicSpeed(creatureBasicSpeed);
        this.setCreature(creature);
    }

    @Override
    public void accessMemoryObjects() {

        if (getDrivesMO() == null)
            setDrivesMO((MemoryObject) this.getInput(MotivationalCodelet.OUTPUT_DRIVE_MEMORY));

        if (getLegsMO() == null)
            setLegsMO((MemoryObject) this.getOutput("LEGS_GO_JEWEL"));

        if (getInnerSenseMO() == null) {
            setInnerSenseMO((MemoryObject) this.getInput("INNER"));
        }

    }

    @Override
    public void calculateActivation() {
        Drive drive = (Drive) getDrivesMO().getI();

        try {
            setActivation(drive.getActivation());
        } catch (CodeletActivationBoundsException e) {
            e.printStackTrace();
        }

    }

    @Override
    public synchronized void proc() {

        double jewelX = 0;
        double jewelY = 0;

        synchronized (getLegsMO()) {

            if (getLegsMO() != null) {
                jewelX = 0;
                jewelY = 0;

                JSONObject message = new JSONObject();
                try {
                    message.put("ACTION", "GOTO");
                    message.put("X", (int) jewelX);
                    message.put("Y", (int) jewelY);
                    message.put("SPEED", getCreatureBasicSpeed());

                    getLegsMO().setEvaluation(getActivation());
                    getLegsMO().setI(message.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {

                JSONObject message = new JSONObject();
                try {
                    message.put("ACTION", "FORAGE");
                    getLegsMO().setI(message.toString());
                    getLegsMO().setEvaluation(getActivation());

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
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

    public MemoryObject getInnerSenseMO() {
        return innerSenseMO;
    }

    public void setInnerSenseMO(MemoryObject innerSenseMO) {
        this.innerSenseMO = innerSenseMO;
    }

    public MemoryObject getDeliverySpotMO() {
        return deliverySpotMO;
    }

    public void setDeliverySpotMO(MemoryObject deliverySpotMO) {
        this.deliverySpotMO = deliverySpotMO;
    }

    public MemoryObject getLegsMO() {
        return legsMO;
    }

    public void setLegsMO(MemoryObject legsMO) {
        this.legsMO = legsMO;
    }

    public MemoryObject getDrivesMO() {
        return drivesMO;
    }

    public void setDrivesMO(MemoryObject drivesMO) {
        this.drivesMO = drivesMO;
    }

    public int getCreatureBasicSpeed() {
        return creatureBasicSpeed;
    }

    public void setCreatureBasicSpeed(int creatureBasicSpeed) {
        this.creatureBasicSpeed = creatureBasicSpeed;
    }
}

