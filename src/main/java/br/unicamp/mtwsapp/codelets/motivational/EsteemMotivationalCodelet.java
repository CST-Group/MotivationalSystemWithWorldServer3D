package br.unicamp.mtwsapp.codelets.motivational;

import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import ws3dproxy.model.Leaflet;

import java.util.List;

public class EsteemMotivationalCodelet extends MotivationalCodelet {

    public EsteemMotivationalCodelet(String id, double level, double priority, double urgencyThreshold) {
        super(id, level, priority, urgencyThreshold);
    }

    @Override
    public double calculateSimpleActivation(List<Memory> sensors) {

        MemoryObject innerSense = (MemoryObject) sensors.get(0);

        CreatureInnerSense creatureInnerSense = (CreatureInnerSense) innerSense.getI();

        if (creatureInnerSense != null) {
            List<Leaflet> leafletList = creatureInnerSense.getLeafletList();
            if (leafletList != null) {
                for (Leaflet leaflet : leafletList) {
                    if (leaflet.getSituation() == 1) {
                        return 1;
                    }
                }
            }
        }

        return 0;
    }

    @Override
    public double calculateSecundaryDriveActivation(List<Memory> sensors, List<Drive> listOfDrives) {
        return 0;
    }
}
