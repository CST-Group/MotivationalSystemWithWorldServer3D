package br.unicamp.mtwsapp.codelets.motor;


import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import org.json.JSONException;
import org.json.JSONObject;

import br.unicamp.cst.core.entities.Codelet;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import ws3dproxy.CommandExecException;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

/**
 * @author Du
 */

public class HandsActionCodelet extends Codelet {

    private MemoryContainer behaviorsMC;
    private MemoryObject hiddenApplesMO;

    private String previousHandsAction = "";
    private Creature c;
    private Random r = new Random();
    static Logger log = Logger.getLogger(HandsActionCodelet.class.getCanonicalName());

    public HandsActionCodelet(Creature nc) {
        c = nc;
    }

    @Override
    public void accessMemoryObjects() {
        if(behaviorsMC == null)
            behaviorsMC = (MemoryContainer) this.getInput("BEHAVIORS_MC");

        if (hiddenApplesMO == null)
            hiddenApplesMO = (MemoryObject) this.getInput("HIDDEN_THINGS");


    }

    public void proc() {
        synchronized (behaviorsMC) {

            if(behaviorsMC.getI() != null) {
                String command = (String) behaviorsMC.getI();

                if (!command.equals("") && (!command.equals(previousHandsAction))) {
                    JSONObject jsonAction;
                    try {
                        jsonAction = new JSONObject(command);
                        if (jsonAction.has("ACTION") && jsonAction.has("OBJECT")) {
                            String action = jsonAction.getString("ACTION");
                            String objectName = jsonAction.getString("OBJECT");
                            if (action.equals("PICKUP")) {
                                try {
                                    c.putInSack(objectName);
                                } catch (Exception e) {

                                }
                                log.info("Sending PUT IN SACK command to agent:****** " + objectName + "**********");

                            }
                            if (action.equals("EATIT")) {
                                try {
                                    c.eatIt(objectName);
                                } catch (Exception e) {

                                }

                                log.info("Sending EAT command to agent:****** " + objectName + "**********");
                            }
                            if (action.equals("BURY")) {
                                try {
                                    c.hideIt(objectName);
                                } catch (Exception e) {

                                }

                                log.info("Sending BURY command to agent:****** " + objectName + "**********");
                            }
                            if(action.equals("UNEARTH")){

                                try {
                                    c.unhideIt(objectName);
                                    c.eatIt(objectName);
                                } catch (CommandExecException e) {
                                    e.printStackTrace();
                                }

                                List<Thing> things = (List<Thing>) hiddenApplesMO.getI();

                                for (int i = 0; i < things.size(); i++) {
                                    if (things.get(i).getName().equals(objectName)) {
                                        things.get(i).hidden = false;
                                        break;
                                    }
                                }

                                log.info("Sending UNEARTH command to agent:****** " + objectName + "**********");
                            }


                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                previousHandsAction = command;
            }
        }
    }//end proc

    @Override
    public void calculateActivation() {

    }


}
