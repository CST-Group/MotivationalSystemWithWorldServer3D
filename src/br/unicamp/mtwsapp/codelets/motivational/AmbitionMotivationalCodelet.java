package br.unicamp.mtwsapp.codelets.motivational;

import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import ws3dproxy.model.Leaflet;
import ws3dproxy.model.Thing;
import ws3dproxy.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by du on 22/03/17.
 */
public class AmbitionMotivationalCodelet extends MotivationalCodelet {



    public AmbitionMotivationalCodelet(String name, double level, double priority, double urgencyThreshold) throws CodeletActivationBoundsException {
        super(name, level, priority, urgencyThreshold);
    }

    @Override
    public synchronized double calculateSimpleActivation(List<Memory> sensorsMemory) {

        Memory cisMO = sensorsMemory.get(0);
        Memory knownJewels = sensorsMemory.get(1);

        double jewelsStimulus = 0;
        double ambitionDeficit = 0.0d;

        if (knownJewels.getI() != null) {

            List<Thing> jewels = Collections.synchronizedList((List<Thing>) knownJewels.getI());
            if (jewels.size() > 0)
                jewelsStimulus = 0.2;
        } else {
            jewelsStimulus = 0;
        }

        CreatureInnerSense cis = (CreatureInnerSense) cisMO.getI();

        /*if (cis.getLeafletList() != null) {

            double collectedNumberLeaflet = getCollectedNumberOfJewelsInLeaflet(cis.getLeafletList());
            double fullNumberLeaflet = getTotalNumberOfJewelsInLeaflet(cis.getLeafletList());

            ambitionDeficit = (collectedNumberLeaflet / fullNumberLeaflet);
        }*/

        ambitionDeficit = cis.getLeafletCompleteRate()/100;

        double activation = Math.max(ambitionDeficit, ambitionDeficit * (1 + jewelsStimulus));

        if (activation > 1)
            activation = 1;

        if (activation == 0 && ambitionDeficit < 1) {
            activation = 0.05;
        } else if (ambitionDeficit == 1) {
            activation = 0;
        }

        return activation;
    }

    @Override
    public double calculateSecundaryDriveActivation(List<Memory> sensorsMemory, List<Drive> drives) {
        return 0;
    }

    public double getFullNumberLeaflet(List<Leaflet> leaflets) {

        ArrayList<String> colors = new ArrayList<String>();
        colors.add(Constants.colorRED);
        colors.add(Constants.colorYELLOW);
        colors.add(Constants.colorGREEN);
        colors.add(Constants.colorWHITE);
        colors.add(Constants.colorORANGE);
        colors.add(Constants.colorMAGENTA);
        colors.add(Constants.colorBLUE);

        double totalJewels = 0;

        double totalScore = 0;

        for (Leaflet leaflet : leaflets) {

            int countLeaflet = 0;

            for (String color : colors) {
                if (leaflet.getTotalNumberOfType(color) != -1) {
                    totalJewels += leaflet.getTotalNumberOfType(color);

                    if (leaflet.getMissingNumberOfType(color) > 0)
                        countLeaflet++;
                }
            }

            if (countLeaflet == 0)
                totalScore += leaflet.getSituation();

        }

        return totalJewels;
    }

    public double getCollectedNumberLeaflet(List<Leaflet> leaflets) {

        ArrayList<String> colors = new ArrayList<String>();
        colors.add(Constants.colorRED);
        colors.add(Constants.colorYELLOW);
        colors.add(Constants.colorGREEN);
        colors.add(Constants.colorWHITE);
        colors.add(Constants.colorORANGE);
        colors.add(Constants.colorMAGENTA);
        colors.add(Constants.colorBLUE);

        double totalCollectedJewels = 0;

        for (Leaflet leaflet : leaflets) {

            for (String color : colors) {
                if (leaflet.getTotalNumberOfType(color) != -1)
                    totalCollectedJewels += leaflet.getCollectedNumberOfType(color);
            }

        }

        return totalCollectedJewels;
    }
}
