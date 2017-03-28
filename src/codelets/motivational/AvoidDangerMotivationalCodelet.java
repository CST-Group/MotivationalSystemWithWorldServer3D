package codelets.motivational;

import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;

import java.util.List;

/**
 * Created by du on 22/03/17.
 */
public class AvoidDangerMotivationalCodelet extends MotivationalCodelet {
    public AvoidDangerMotivationalCodelet(String name, double level, double priority, double urgencyThreshold) throws CodeletActivationBoundsException {
        super(name, level, priority, urgencyThreshold);
    }

    @Override
    public double calculateSimpleActivation(List<Memory> sensorsMemory) {

        double activation = 0;

        Memory closestObstacle = sensorsMemory.get(0);

        synchronized (closestObstacle) {
            if (closestObstacle.getI() != null) {
                if (closestObstacle.getI() == "")
                    activation = 0;
                else
                    activation = 1;
            }
        }

        return activation;
    }

    @Override
    public double calculateSecundaryDriveActivation(List<Memory> sensorsMemory, List<Drive> drives) {
        return 0;
    }
}
