package br.unicamp.mtwsapp.codelets.motor;


import br.unicamp.cst.core.entities.MemoryContainer;
import org.json.JSONObject;

import br.unicamp.cst.core.entities.Codelet;

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
    private double previousTargetx = 0;
    private double previousTargety = 0;
    private String previousLegsAction = "";
    private Creature c;
    int k = 0;
    static Logger log = Logger.getLogger(LegsActionCodelet.class.getCanonicalName());

    public LegsActionCodelet(Creature nc) {
        c = nc;
    }

    @Override
    public void accessMemoryObjects() {
        if(behaviorsMC == null)
            behaviorsMC = (MemoryContainer) this.getInput("BEHAVIORS_MC");
    }

    @Override
    public void proc() {

        String comm = (String) behaviorsMC.getI();
        if (comm == null) comm = "";
        Random r = new Random();

        if (!comm.equals("")) {

            try {
                JSONObject command = new JSONObject(comm);
                if (command.has("ACTION")) {
                    int x = 0, y = 0;
                    String action = command.getString("ACTION");
                    if (action.equals("FORAGE")) {
                        //if (!comm.equals(previousLegsAction)) {
                        if (!comm.equals(previousLegsAction))
                            log.info("Sending Forage command to agent");
                        try {
                            c.rotate(0.01);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (action.equals("GOTO")) {
                        if (!comm.equals(previousLegsAction)) {
                            double speed = command.getDouble("SPEED");
                            double targetx = command.getDouble("X");
                            double targety = command.getDouble("Y");
                            if (!comm.equals(previousLegsAction))
                                log.info("Sending move command to agent: [" + targetx + "," + targety + "]");
                            try {
                                c.moveto(speed, targetx, targety);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            previousTargetx = targetx;
                            previousTargety = targety;
                        }

                    } else if (action.equals("AVOID")) {
                        Random rand = new Random();
                        int random = rand.nextInt(3);

                        if (random == 2) {
                            c.move(0, 0, c.getPitch() + Math.toRadians(-90));
                            Thread.sleep(100);
                            c.move(3, 3, c.getPitch() + Math.toRadians(0));

                        } else if (random == 1) {
                            c.move(0, 0, c.getPitch() + Math.toRadians(90));
                            Thread.sleep(100);
                            c.move(3, 3, c.getPitch() + Math.toRadians(0));
                        } else {
                            c.move(0, 0, c.getPitch() + Math.toRadians(180));
                            Thread.sleep(100);
                            c.move(3, 3, c.getPitch() + Math.toRadians(0));
                        }
                        Thread.sleep(500);

                    } else {
                        log.info("Sending stop command to agent");
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
            } catch (InterruptedException ex) {
                Logger.getLogger(LegsActionCodelet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//end proc

    @Override
    public void calculateActivation() {

    }


}
