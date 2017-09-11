package br.unicamp.mtwsapp.codelets.motor;


import br.unicamp.cst.core.entities.MemoryContainer;
import org.json.JSONObject;

import br.unicamp.cst.core.entities.Codelet;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import ws3dproxy.CommandExecException;
import ws3dproxy.model.Creature;

/**
 * @author Du
 */

public class LegsActionCodelet extends Codelet {

    private MemoryContainer behaviorsMC;
    private String previousLegsAction = "";
    private double previousPositionX = 0d;
    private double previousPositionY = 0d;
    private double targetXSaved = 0d;
    private double targetYSaved = 0d;

    private Creature c;
    private double rotation = 3;

    private int k = 0;
    private static Logger log = Logger.getLogger(LegsActionCodelet.class.getCanonicalName());

    public LegsActionCodelet(String name, Creature nc) {
        setC(nc);
        this.setName(name);
    }

    public static Logger getLog() {
        return log;
    }

    public static void setLog(Logger log) {
        LegsActionCodelet.log = log;
    }

    @Override
    public void accessMemoryObjects() {
        if (getBehaviorsMC() == null)
            setBehaviorsMC((MemoryContainer) this.getInput("BEHAVIORS_MC"));

    }

    @Override
    public void proc() {



            String comm = (String) getBehaviorsMC().getI();
            if (comm == null)
                comm = "";

            if (!comm.equals("")) {
                try {
                    JSONObject command = new JSONObject(comm);
                    if (command.has("ACTION")) {
                        int x = 0, y = 0;
                        String action = command.getString("ACTION");

                        if (action.equals("RANDOM")
                                && getPreviousLegsAction().contains("RANDOM")
                                && getC().getPosition().getX() == previousPositionX
                                && getC().getPosition().getY() == previousPositionY) {
                            action = "AVOID";
                        }

                        if (action.equals("FORAGE")) {
                            if (!getPreviousLegsAction().contains(action)) {
                                getLog().info("Sending FORAGE command to agent");
                                try {
                                    //getC().move(0, 0, getRotation());
                                    getC().rotate(getRotation());

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        } else if (action.equals("GOTO")) {
                            setRotation(getRotation() == 3 ? -3 : 3);
                            if (!comm.equals(getPreviousLegsAction())) {
                                double speed = command.getDouble("SPEED");
                                double targetx = command.getDouble("X");
                                double targety = command.getDouble("Y");
                                getLog().info("Sending MOVE command to agent: [" + targetx + "," + targety + "]");

                                try {

                                    getC().moveto(speed, targetx, targety);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        } else if (action.equals("RANDOM")) {
                            if (!comm.equals(getPreviousLegsAction())) {
                                setRotation(getRotation() == 3 ? -3 : 3);
                                double speed = command.getDouble("SPEED");
                                double targetx = command.getDouble("X");
                                double targety = command.getDouble("Y");

                                getLog().info("Sending RANDOM command to agent: [" + targetx + "," + targety + "]");

                                previousPositionX = getC().getPosition().getX();
                                previousPositionY = getC().getPosition().getY();

                                getC().moveto(speed, targetx, targety);

                                try {
                                    Thread.sleep(400);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                getC().moveto(0, 0, 0);
                                getC().rotate(getRotation());
                            }


                        } else if (action.equals("AVOID")) {

                            setRotation(getRotation() == 3 ? -3 : 3);
                            Random rand = new Random();
                            int random = rand.nextInt(2);
                            double angle = randomAngle(random);

                            getLog().info("Sending AVOID command to agent: [" + angle + "]");

                            getC().move(-3, -3, getC().getPitch());

                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            getC().rotate(3);


                            try {
                                Thread.sleep(400);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            getC().move(3, 3, angle);

                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            getC().rotate(getRotation());

                        } else {
                            setRotation(getRotation() == 3 ? -3 : 3);
                            getLog().info("Sending STOP command to agent");
                            try {
                                getC().moveto(0, 0, 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    setPreviousLegsAction(comm);
                    setK(getK() + 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (CommandExecException ex) {
                    Logger.getLogger(LegsActionCodelet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }



    }//end proc


    private double randomAngle(int random) {
        if (random == 1) {
            return (getC().getPitch() + Math.toRadians(-90));
        } else {
            return (getC().getPitch() + Math.toRadians(90));
        }
    }

    public boolean isNear(double targetx, double targety, Creature creature, double gap) {
        boolean result = false;

        if (((targetx - gap) <= creature.getPosition().getX() && (targetx + gap) >= creature.getPosition().getX())
                && ((targety - gap) <= creature.getPosition().getY() && (targety + gap) >= creature.getPosition().getY())) {
            result = true;
        }
        return result;
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

    public String getPreviousLegsAction() {
        return previousLegsAction;
    }

    public void setPreviousLegsAction(String previousLegsAction) {
        this.previousLegsAction = previousLegsAction;
    }

    public Creature getC() {
        return c;
    }

    public void setC(Creature c) {
        this.c = c;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }
}
