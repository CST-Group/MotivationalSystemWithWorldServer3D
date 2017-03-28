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
import java.util.List;

/**
 * Created by du on 22/03/17.
 */
public class CuriosityMotivationalCodelet extends MotivationalCodelet {
    public CuriosityMotivationalCodelet(String name, double level, double priority, double urgencyThreshold) throws CodeletActivationBoundsException {
        super(name, level, priority, urgencyThreshold);
    }

    @Override
    public double calculateSimpleActivation(List<Memory> sensorsMemory) {

        Memory cisMO = sensorsMemory.get(0);

        Memory knownJewels = sensorsMemory.get(1);

        double jewelsStimulus = 0;

        synchronized (knownJewels) {

            if (knownJewels.getI() != null) {

                List<Thing> jewels = (List<Thing>) knownJewels.getI();
                if (jewels.size() > 0)
                    jewelsStimulus = 0.2;
            } else {
                jewelsStimulus = 0;
            }
        }

        double curiosityDeficit = 0;

        synchronized (cisMO) {
            CreatureInnerSense cis = (CreatureInnerSense) cisMO.getI();
            if (cis.getLeafletList() != null) {
                curiosityDeficit = 1 - getCollectedNumberLeaflet(cis.getLeafletList())/getFullNumberLeaflet(cis.getLeafletList());
            }
        }

        double activation = 0.9 * Math.max(curiosityDeficit, curiosityDeficit * (1 + jewelsStimulus));

        if (activation > 1)
            activation = 1;

        return activation;
    }

    @Override
    public double calculateSecundaryDriveActivation(List<Memory> sensorsMemory, List<Drive> drives) {
        return 0;
    }

    public int getFullNumberLeaflet(List<Leaflet> leaflets) {

        //HashMap<String, Integer> mapOfJewels = new HashMap<>();

        ArrayList<String> colors = new ArrayList<String>();
        colors.add(Constants.colorRED);
        colors.add(Constants.colorYELLOW);
        colors.add(Constants.colorGREEN);
        colors.add(Constants.colorWHITE);
        colors.add(Constants.colorORANGE);
        colors.add(Constants.colorMAGENTA);
        colors.add(Constants.colorBLUE);


        /*mapOfJewels.put(Constants.colorRED, 0);
        mapOfJewels.put(Constants.colorYELLOW, 0);
        mapOfJewels.put(Constants.colorGREEN, 0);
        mapOfJewels.put(Constants.colorWHITE, 0);
        mapOfJewels.put(Constants.colorORANGE, 0);
        mapOfJewels.put(Constants.colorMAGENTA, 0);
        mapOfJewels.put(Constants.colorBLUE, 0);*/

        int totalJewels = 0;

        for (Leaflet leaflet : leaflets) {

            for (String color : colors) {
                if (leaflet.getTotalNumberOfType(color) != -1)
                    totalJewels += leaflet.getTotalNumberOfType(color);

                    //mapOfJewels.put(color, mapOfJewels.get(color) + leaflet.getTotalNumberOfType(color));
            }

        }

        return totalJewels;
    }

    public int getCollectedNumberLeaflet(List<Leaflet> leaflets) {

        ArrayList<String> colors = new ArrayList<String>();
        colors.add(Constants.colorRED);
        colors.add(Constants.colorYELLOW);
        colors.add(Constants.colorGREEN);
        colors.add(Constants.colorWHITE);
        colors.add(Constants.colorORANGE);
        colors.add(Constants.colorMAGENTA);
        colors.add(Constants.colorBLUE);

        int totalCollectedJewels = 0;

        for (Leaflet leaflet : leaflets) {

            for (String color : colors) {
                if (leaflet.getTotalNumberOfType(color) != -1)
                    totalCollectedJewels += leaflet.getCollectedNumberOfType(color);
            }

        }

        return totalCollectedJewels;
    }
}
