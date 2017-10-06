package br.unicamp.mtwsapp.application;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Du
 */
public class ExperimentMain {




    public ExperimentMain() {
        //WS3DProxy.logger.setLevel(Level.SEVERE);
        //Logger.getLogger("br/unicamp/mtwsapp/codelets").setLevel(Level.SEVERE);
        Logger.getLogger("ac.biu.nlp.nlp.engineml").setLevel(Level.OFF);
        Logger.getLogger("org.BIU.utils.logging.ExperimentLogger").setLevel(Level.OFF);
        Logger.getLogger("java.awt").setLevel(Level.OFF);
        Logger.getLogger("sun.awt").setLevel(Level.OFF);
        Logger.getLogger("javax.swing").setLevel(Level.OFF);


        // Create Environment
        Environment env = new Environment(); //Creates only a creature and some apples
        AgentMind a = new AgentMind(env);  // Creates the Agent Mind and start it

    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        ExperimentMain em = new ExperimentMain();
    }

}
