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
import ws3dproxy.model.Leaflet;

import java.util.ArrayList;
import java.util.List;

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
            setLegsMO((MemoryObject) this.getOutput("LEGS_GO_DELIVERY_SPOT"));

        if (getInnerSenseMO() == null) {
            setInnerSenseMO((MemoryObject) this.getInput("INNER"));
        }

    }

    @Override
    public void calculateActivation() {
        Drive drive = (Drive) getDrivesMO().getI();
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


        if (getLegsMO() != null) {

            List<Leaflet> leafletCompleted = new ArrayList<>();
            CreatureInnerSense innerSense = (CreatureInnerSense) getInnerSenseMO().getI();

            creature.getLeaflets().forEach(leaflet -> {
                if (leaflet.getSituation() == 1)
                    leafletCompleted.add(leaflet);
            });

            if (leafletCompleted.size() > 0) {

                double jewelX = innerSense.getDeliverySpotPosition().getX();
                double jewelY = innerSense.getDeliverySpotPosition().getY();

                JSONObject message = new JSONObject();
                try {
                    message.put("ACTION", "GOTO");
                    message.put("X", (int) jewelX);
                    message.put("Y", (int) jewelY);
                    message.put("SPEED", getCreatureBasicSpeed());
                    message.put("LEAFLETS", leafletCompleted);

                    getLegsMO().setEvaluation(getActivation());
                    getLegsMO().setI(message.toString());
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

