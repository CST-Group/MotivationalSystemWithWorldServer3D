package br.unicamp.mtwsapp.codelets.motivationalbehaviors;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import ws3dproxy.model.Thing;

/**
 *
 * @author Du
 */
public class Forage extends Codelet {

    private MemoryObject knownApplesMO;
    private MemoryObject knownJewelsMO;
    private MemoryObject legsMO;

    private List<Thing> known;

    /**
     * Default constructor
     */
    public Forage() {
        setName("Forage");
    }

    @Override
    public void accessMemoryObjects() {
        if(knownApplesMO == null)
            knownApplesMO = (MemoryObject) this.getInput("KNOWN_APPLES");

        if(knownJewelsMO == null)
            knownJewelsMO = (MemoryObject) this.getInput("KNOWN_JEWELS");

        if(legsMO == null)
            legsMO = (MemoryObject) this.getOutput("LEGS_FORAGE");
    }

    @Override
    public void calculateActivation() {
        try {
            if (((List<Thing>)knownJewelsMO.getI()).size() == 0 && ((List<Thing>)knownApplesMO.getI()).size() == 0) {
                setActivation(1);

            } else {
                setActivation(0);
            }
        } catch (CodeletActivationBoundsException ex) {
            Logger.getLogger(GoToApple.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized void proc() {
        known = new ArrayList<>();
        known.addAll((List<Thing>) knownApplesMO.getI());
        known.addAll((List<Thing>) knownJewelsMO.getI());
        if (known.size() == 0) {
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
