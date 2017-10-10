package br.unicamp.mtwsapp.codelets.motor;


import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.unicamp.cst.core.entities.Codelet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import ws3dproxy.CommandExecException;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

/**
 * @author Du
 */

public class HandsActionCodelet extends Codelet {

    private MemoryContainer behaviorsMC;
    private MemoryObject hiddenApplesMO;
    private MemoryObject visionMO;
    private MemoryObject jewelsCollectedMO;


    private List<String> jewelsCollected;
    private String previousHandsAction = "";
    private Creature c;
    private Random r = new Random();
    private static Logger log = Logger.getLogger(HandsActionCodelet.class.getCanonicalName());

    public HandsActionCodelet(String name, Creature nc) {
        setC(nc);
        this.setName(name);
        setJewelsCollected(new ArrayList<>());
    }

    public static Logger getLog() {
        return log;
    }

    public static void setLog(Logger log) {
        HandsActionCodelet.log = log;
    }

    @Override
    public void accessMemoryObjects() {
        if (getBehaviorsMC() == null)
            setBehaviorsMC((MemoryContainer) this.getInput("BEHAVIORS_MC"));

        if (getHiddenApplesMO() == null)
            setHiddenApplesMO((MemoryObject) this.getOutput("HIDDEN_THINGS"));

        if (getVisionMO() == null)
            setVisionMO((MemoryObject) this.getInput("VISION"));


        if (getJewelsCollectedMO() == null)
            setJewelsCollectedMO((MemoryObject) this.getOutput("JEWELS_COLLECTED"));


    }

    public void proc() {

        if (getBehaviorsMC().getI() != null) {
            String command = (String) getBehaviorsMC().getI();
            if (!command.equals("") && !command.equals(getPreviousHandsAction())) {
                JSONObject jsonAction;
                try {
                    jsonAction = new JSONObject(command);
                    if (jsonAction.has("ACTION") && jsonAction.has("OBJECT")) {
                        String action = jsonAction.getString("ACTION");
                        String objectName = jsonAction.getString("OBJECT");
                        if (action.equals("PICKUP")) {

                            try {

                                getC().putInSack(objectName);
                                getJewelsCollected().add(objectName);
                                getC().rotate(3);

                            } catch (CommandExecException e) {
                                e.printStackTrace();
                            }
                            getLog().info("Sending PUT IN SACK command to agent:****** " + objectName + "**********");

                        }
                        if (action.equals("DELIVERY")) {

                            JSONArray leaflets = jsonAction.getJSONArray("LEAFLETS");

                            for (int i = 0; i < leaflets.length(); i++) {
                                try {
                                    getC().deliverLeaflet(leaflets.get(i).toString());
                                } catch (Exception e) {

                                }
                            }

                            getLog().info("Sending DELIVERY command to agent:****** " + objectName + "**********");
                        }

                        if (action.equals("EATIT")) {

                            try {

                                getC().eatIt(objectName);

                                getC().rotate(3);

                            } catch (Exception e) {

                            }
                            getLog().info("Sending EAT command to agent:****** " + objectName + "**********");

                        }
                        if (action.equals("BURY")) {

                            try {

                                getC().hideIt(objectName);

                                List<Thing> vision = Collections.synchronizedList((List<Thing>) getVisionMO().getI());

                                List<Thing> thingsA = vision.stream().filter(v -> v.getName().equals(objectName)).collect(Collectors.toList());

                                if (thingsA.size() > 0) {
                                    Thing closestObstacle = thingsA.get(0);
                                    if (closestObstacle.getName().contains("Food")) {
                                        closestObstacle.hidden = true;

                                        List<Thing> things = (List<Thing>) getHiddenApplesMO().getI();

                                        if (things.size() == 0) {
                                            things.add(closestObstacle);
                                        } else {
                                            for (int i = 0; i < things.size(); i++) {
                                                if (!things.get(i).getName().equals(closestObstacle.getName())) {
                                                    things.add(closestObstacle);
                                                }
                                            }
                                        }
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            getLog().info("Sending BURY command to agent:****** " + objectName + "**********");

                        }
                        if (action.equals("UNEARTH")) {

                            try {

                                getC().unhideIt(objectName);

                                getC().eatIt(objectName);

                                getC().rotate(3);

                            } catch (CommandExecException e) {
                                e.printStackTrace();
                            }

                            List<Thing> things = (List<Thing>) getHiddenApplesMO().getI();
                            for (int i = 0; i < things.size(); i++) {
                                if (things.get(i).getName().equals(objectName)) {
                                    things.remove(i);
                                    break;
                                }
                            }
                            getLog().info("Sending UNEARTH command to agent:****** " + objectName + "**********");

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setPreviousHandsAction(command);
        }

        getJewelsCollectedMO().setI(getJewelsCollected());

    }

    @Override
    public void calculateActivation() {

    }

    public MemoryContainer getBehaviorsMC() {
        return behaviorsMC;
    }

    public void setBehaviorsMC(MemoryContainer behaviorsMC) {
        this.behaviorsMC = behaviorsMC;
    }

    public MemoryObject getHiddenApplesMO() {
        return hiddenApplesMO;
    }

    public void setHiddenApplesMO(MemoryObject hiddenApplesMO) {
        this.hiddenApplesMO = hiddenApplesMO;
    }

    public MemoryObject getVisionMO() {
        return visionMO;
    }

    public void setVisionMO(MemoryObject visionMO) {
        this.visionMO = visionMO;
    }

    public String getPreviousHandsAction() {
        return previousHandsAction;
    }

    public void setPreviousHandsAction(String previousHandsAction) {
        this.previousHandsAction = previousHandsAction;
    }

    public Creature getC() {
        return c;
    }

    public void setC(Creature c) {
        this.c = c;
    }

    public Random getR() {
        return r;
    }

    public void setR(Random r) {
        this.r = r;
    }

    public MemoryObject getJewelsCollectedMO() {
        return jewelsCollectedMO;
    }

    public void setJewelsCollectedMO(MemoryObject jewelsCollectedMO) {
        this.jewelsCollectedMO = jewelsCollectedMO;
    }

    public List<String> getJewelsCollected() {
        return jewelsCollected;
    }

    public void setJewelsCollected(List<String> jewelsCollected) {
        this.jewelsCollected = jewelsCollected;
    }
}
