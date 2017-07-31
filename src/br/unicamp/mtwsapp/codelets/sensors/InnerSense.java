package br.unicamp.mtwsapp.codelets.sensors;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Leaflet;
import ws3dproxy.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Du
 */

public class InnerSense extends Codelet {

    private MemoryObject innerSenseMO;
    private Creature c;
    private CreatureInnerSense cis;
    private double agentScore;

    public InnerSense(String name, Creature nc) {
        setC(nc);
        this.setName(name);
        setCis(new CreatureInnerSense());
    }

    @Override
    public void accessMemoryObjects() {
        if (getInnerSenseMO() == null) {
            setInnerSenseMO((MemoryObject) this.getOutput("INNER"));
        }
    }

    public synchronized void proc() {

        getCis().setPosition(getC().getPosition());
        getCis().setPitch(getC().getPitch());
        getCis().setFov(getC().getFOV());
        getCis().setFuel(getC().getFuel());
        getCis().setLeafletList(getC().getLeaflets());

        double collectedNumberLeaflet = getCollectedNumberLeaflet(getCis().getLeafletList());
        double fullNumberLeaflet = getFullNumberLeaflet(getCis().getLeafletList());

        getCis().setLeafletCompleteRate((collectedNumberLeaflet / fullNumberLeaflet)*100);

        getInnerSenseMO().setI(getCis());

    }

    @Override
    public void calculateActivation() {
        try {
            setActivation(0);
        } catch (CodeletActivationBoundsException e) {
            e.printStackTrace();
        }
    }


    public MemoryObject getInnerSenseMO() {
        return innerSenseMO;
    }

    public void setInnerSenseMO(MemoryObject innerSenseMO) {
        this.innerSenseMO = innerSenseMO;
    }

    public Creature getC() {
        return c;
    }

    public void setC(Creature c) {
        this.c = c;
    }

    public CreatureInnerSense getCis() {
        return cis;
    }

    public void setCis(CreatureInnerSense cis) {
        this.cis = cis;
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

        this.agentScore = totalScore;

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
