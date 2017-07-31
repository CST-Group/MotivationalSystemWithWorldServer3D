package br.unicamp.mtwsapp.codelets.motivationalbehaviors;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * Created by du on 31/03/17.
 */
public class RandomMove extends Codelet {

    private MemoryObject drivesMO;
    private MemoryObject legsMO;


    public RandomMove(String name){
        this.setName(name);
    }

    @Override
    public void accessMemoryObjects() {

        if (drivesMO == null)
            drivesMO = (MemoryObject) this.getInput(MotivationalCodelet.OUTPUT_DRIVE_MEMORY);

        if (legsMO == null)
            legsMO = (MemoryObject) this.getOutput("LEGS_RANDOM_MOVE");

    }

    @Override
    public synchronized void calculateActivation() {

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

        JSONObject message = new JSONObject();

        try {

            Random random = new Random();

            message.put("ACTION", "RANDOM");
            message.put("SPEED", 3);
            message.put("X", random.nextInt(800));
            message.put("Y", random.nextInt(600));
            legsMO.setEvaluation(getActivation());
            legsMO.setI(message.toString());

        } catch (JSONException e) {

            e.printStackTrace();
        }
    }

}

