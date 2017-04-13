package br.unicamp.mtwsapp.codelets.motor;


import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import org.json.JSONObject;

import br.unicamp.cst.core.entities.Codelet;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import ws3dproxy.CommandExecException;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

/**
 * @author Du
 */

public class LegsActionCodelet extends Codelet {

    private MemoryContainer behaviorsMC;


    private String previousLegsAction = "";
    private Creature c;
    private double rotation = 0.01;

    int k = 0;
    static Logger log = Logger.getLogger(LegsActionCodelet.class.getCanonicalName());

    public LegsActionCodelet(Creature nc) {
        c = nc;
    }

    @Override
    public void accessMemoryObjects() {
        if (behaviorsMC == null)
            behaviorsMC = (MemoryContainer) this.getInput("BEHAVIORS_MC");

    }

    @Override
    public void proc() {

        synchronized (behaviorsMC) {
            String comm = (String) behaviorsMC.getI();
            if (comm == null)
                comm = "";

            if (!comm.equals("")) {
                try {
                    JSONObject command = new JSONObject(comm);
                    if (command.has("ACTION")) {
                        int x = 0, y = 0;
                        String action = command.getString("ACTION");
                        if (action.equals("FORAGE")) {
                            //if (!comm.equals(previousLegsAction)) {
                            if (!comm.equals(previousLegsAction))
                                log.info("Sending FORAGE command to agent");
                            try {
                                c.rotate(rotation);
                                //c.move(0,0, rotation);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (action.equals("GOTO")) {
                            rotation = rotation == 0.01 ? -0.01 : 0.01;
                            if (!comm.equals(previousLegsAction)) {
                                double speed = command.getDouble("SPEED");
                                double targetx = command.getDouble("X");
                                double targety = command.getDouble("Y");
                                log.info("Sending MOVE command to agent: [" + targetx + "," + targety + "]");

                                try {
                                    c.moveto(speed, targetx, targety);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }  else if (action.equals("RANDOM")) {
                            if (!comm.equals(previousLegsAction)) {
                                rotation = rotation == 0.01 ? -0.01 : 0.01;
                                double speed = command.getDouble("SPEED");
                                double targetx = command.getDouble("X");
                                double targety = command.getDouble("Y");

                                log.info("Sending RANDOM command to agent: [" + targetx + "," + targety + "]");

                                c.moveto(speed, targetx, targety);
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                        } else if (action.equals("AVOID")) {

                            if (!comm.equals(previousLegsAction)) {
                                rotation = rotation == 0.01 ? -0.01 : 0.01;
                                Random rand = new Random();
                                int random = rand.nextInt(2);
                                double angle = randomAngle(random);

                                log.info("Sending AVOID command to agent: [" + angle + "]");

                                c.move(-3, -3, c.getPitch());

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                c.move(3, 3, angle);

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                        } else {
                            rotation = rotation == 0.01 ? -0.01 : 0.01;
                            log.info("Sending STOP command to agent");
                            try {
                                c.moveto(0, 0, 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    previousLegsAction = comm;
                    k++;
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (CommandExecException ex) {
                    Logger.getLogger(LegsActionCodelet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//end proc


    private double randomAngle(int random) {
        if (random == 1) {
            return (c.getPitch() + Math.toRadians(-90));
        } else  {
            return (c.getPitch() + Math.toRadians(90));
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


}
