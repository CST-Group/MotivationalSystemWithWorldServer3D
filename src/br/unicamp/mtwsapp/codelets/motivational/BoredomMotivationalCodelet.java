package br.unicamp.mtwsapp.codelets.motivational;

import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import sun.jvm.hotspot.utilities.Interval;
import ws3dproxy.model.WorldPoint;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Created by du on 31/03/17.
 */
public class BoredomMotivationalCodelet extends MotivationalCodelet {

    private WorldPoint creaturePositionSaved;
    private CreatureInnerSense cis;
    private Date timeCheckPoint;

    public BoredomMotivationalCodelet(String name, double level, double priority, double urgencyThreshold) throws CodeletActivationBoundsException {
        super(name, level, priority, urgencyThreshold);
    }

    @Override
    public synchronized double calculateSimpleActivation(List<Memory> sensorMOs) {

        double activation = 0;

        cis = (CreatureInnerSense) sensorMOs.get(0).getI();

        if(creaturePositionSaved == null)
            creaturePositionSaved =  cis.getPosition();
        else
        {
            WorldPoint actuallyPosition = cis.getPosition();

            if(creaturePositionSaved.getX() == actuallyPosition.getX() && creaturePositionSaved.getY() == actuallyPosition.getY()){
                if(timeCheckPoint == null)
                    timeCheckPoint = new Date();
                else{
                    double diff = (new Date()).getTime() - timeCheckPoint.getTime();
                    activation = diff/20000 > 1 ? 1 : diff/20000;
                }
            }
            else{
                timeCheckPoint = new Date();
                creaturePositionSaved = cis.getPosition();
            }
        }

        return activation;
    }

    @Override
    public double calculateSecundaryDriveActivation(List<Memory> sensorMOs, List<Drive> drives) {
        return 0;
    }
}
