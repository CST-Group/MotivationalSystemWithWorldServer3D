package br.unicamp.mtwsapp.codelets.motivational;

import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import ws3dproxy.model.Thing;

import java.util.Collections;
import java.util.List;

/**
 * Created by du on 22/03/17.
 */
public class HungerMotivationalCodelet extends MotivationalCodelet {
    public HungerMotivationalCodelet(String name, double level, double priority, double urgencyThreshold) throws CodeletActivationBoundsException {
        super(name, level, priority, urgencyThreshold);
    }

    @Override
    public double calculateSimpleActivation(List<Memory> sensorsMemory) {

        Memory cisMO = sensorsMemory.get(0);
        CreatureInnerSense cis = (CreatureInnerSense) cisMO.getI();

        Memory knownApples = sensorsMemory.get(1);
        double foodsStimulus = 0;

        synchronized (knownApples) {
            if (knownApples.getI() != null) {
                List<Thing> foods = Collections.synchronizedList((List<Thing>) knownApples.getI());
                if (foods.size() > 0)
                    foodsStimulus = 0.2;
            } else {
                foodsStimulus = 0;
            }

        }
        double foodDeficit = 1 - (cis.getFuel() / 1000);

        double activation = 0;

        if(foodsStimulus > 0)
            activation = 0.95 * Math.max(foodDeficit, foodDeficit * (1 + foodsStimulus));

        if(activation > 1)
            activation = 1;

        return activation;
    }

    @Override
    public double calculateSecundaryDriveActivation(List<Memory> sensorsMemory, List<Drive> drives) {
        return 0;
    }
}
