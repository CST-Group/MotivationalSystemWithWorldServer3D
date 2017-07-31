package br.unicamp.mtwsapp.codelets.motivational;

import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import ws3dproxy.model.Thing;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by du on 22/03/17.
 */
public class AvoidDangerMotivationalCodelet extends MotivationalCodelet {
    public AvoidDangerMotivationalCodelet(String name, double level, double priority, double urgencyThreshold) throws CodeletActivationBoundsException {
        super(name, level, priority, urgencyThreshold);
    }

    @Override
    public synchronized double calculateSimpleActivation(List<Memory> sensorsMemory) {

        double closestActivation = 0;
        double obstacleStimulus = 0;

        Memory closestObstacle = sensorsMemory.get(1);
        Memory visionMO = sensorsMemory.get(0);

        synchronized (closestObstacle) {
            if (closestObstacle.getI() != null) {
                if (closestObstacle.getI() == "")
                    closestActivation = 0;
                else
                    closestActivation = 0.8;
            }
        }

        synchronized (visionMO){
            if(visionMO.getI() != null){
                List<Thing> vision = (List<Thing>) visionMO.getI();
                if(vision.stream().filter(thing -> thing.getName().contains("Brick")).collect(Collectors.toList()).size() > 0){
                    obstacleStimulus = 0.05;
                }
            }
        }

        double finalActivation = obstacleStimulus + closestActivation;

        if(finalActivation > 1)
            finalActivation = 1;

        return finalActivation;
    }

    @Override
    public double calculateSecundaryDriveActivation(List<Memory> sensorsMemory, List<Drive> drives) {
        return 0;
    }
}
