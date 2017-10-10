package br.unicamp.mtwsapp.codelets.sensors;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.representation.owrl.AbstractObject;
import br.unicamp.cst.representation.owrl.Property;
import br.unicamp.cst.representation.owrl.QualityDimension;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import ws3dproxy.CommandExecException;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Leaflet;
import ws3dproxy.model.World;
import ws3dproxy.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author Du
 */

public class InnerSense extends Codelet {

    private MemoryObject innerSenseMO;
    private MemoryObject innerSenseAOMO;
    private Creature c;
    private CreatureInnerSense cis;

    private String[] colors;

    public InnerSense(String name, Creature nc) {
        setC(nc);
        this.setName(name);
        setCis(new CreatureInnerSense());

        setColors(new String[]{Constants.colorRED, Constants.colorYELLOW, Constants.colorGREEN, Constants.colorWHITE, Constants.colorMAGENTA, Constants.colorBLUE});
    }

    @Override
    public void accessMemoryObjects() {
        if (getInnerSenseMO() == null) {
            setInnerSenseMO((MemoryObject) this.getOutput("INNER"));
        }

        if (getInnerSenseAOMO() == null) {
            setInnerSenseAOMO((MemoryObject) this.getOutput("INNER_AO"));
        }
    }

    public void proc() {

        getCis().setPosition(getC().getPosition());
        getCis().setPitch(getC().getPitch());
        getCis().setFov(getC().getFOV());
        getCis().setFuel(getC().getFuel());
        getCis().setLeafletList(getC().getLeaflets());
        getCis().setScore(getC().s.score);
        getCis().setDeliverySpotPosition(World.getDeliverySpot());

        try {
            getCis().setThingsInWorld(World.getWorldEntities());
        } catch (CommandExecException e) {
            e.printStackTrace();
        }

        double collectedNumberLeaflet = getCollectedNumberOfJewelsInLeaflet(getCis().getLeafletList());

        double fullNumberLeaflet = getTotalNumberOfJewelsInLeaflet(getCis().getLeafletList());

        getCis().setLeafletCompleteRate((collectedNumberLeaflet / fullNumberLeaflet) * 100);

        getInnerSenseMO().setI(getCis());

        getInnerSenseAOMO().setI(innerSenseToAbstractObject(getCis()));

    }

    @Override
    public void calculateActivation() {
        try {
            setActivation(0);
        } catch (CodeletActivationBoundsException e) {
            e.printStackTrace();
        }
    }


    public AbstractObject innerSenseToAbstractObject(CreatureInnerSense innerSense) {

        AbstractObject innerSenseAO = new AbstractObject("INNERSENSE");

        innerSenseAO.addProperty(new Property("SENSE", Arrays.asList(
                new QualityDimension("FUEL", innerSense.getFuel()),
                new QualityDimension("LEAFLETCOMPLETERATE", innerSense.getLeafletCompleteRate()),
                new QualityDimension("POSITION", innerSense.getPosition())
        )));



        List<Leaflet> leaflets = getCis().getLeafletList();

        for (int i = 0; i < leaflets.size(); i++) {
            HashMap<String, Object> hashMapJewels = new HashMap<>();
            HashMap<String, Double> differenceBetweenCollectedAndLeaflets = getDifferenceBetweenCollectedAndLeaflets(leaflets.get(i));

            Property leafletProp = new Property("LEAFLET", new QualityDimension("SCORE", (double)leaflets.get(i).getPayment()));
            leafletProp.addQualityDimension(new QualityDimension("INDEX", (double)i));

            for (HashMap.Entry<String, Double> colorJewel : differenceBetweenCollectedAndLeaflets.entrySet()) {
                hashMapJewels.put(colorJewel.getKey().toUpperCase(), colorJewel.getValue());
            }

            leafletProp.addQualityDimension(new QualityDimension("JEWELSINLEAFLET", hashMapJewels));

            innerSenseAO.addProperty(leafletProp);

        }

        return innerSenseAO;
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


    public HashMap<String, Double> getDifferenceBetweenCollectedAndLeaflets(Leaflet leaflet) {

        HashMap<String, Double> diffJewels = new HashMap<>();
        diffJewels.put(Constants.colorRED, 0d);
        diffJewels.put(Constants.colorYELLOW, 0d);
        diffJewels.put(Constants.colorGREEN, 0d);
        diffJewels.put(Constants.colorWHITE, 0d);
        diffJewels.put(Constants.colorMAGENTA, 0d);
        diffJewels.put(Constants.colorBLUE, 0d);


        for (String color : getColors()) {
            if (leaflet.getTotalNumberOfType(color) != -1) {
                int totalNumberOfType = leaflet.getTotalNumberOfType(color);
                if (totalNumberOfType > 0) {

                    //double value = leaflet.getMissingNumberOfType(color) - leaflet.getCollectedNumberOfType(color);


                    //diffJewels.put(color, diffJewels.get(color) + value);

                    diffJewels.put(color, (double)totalNumberOfType);
                }

            }
        }


        return diffJewels;

    }


    public double getTotalNumberOfJewelsInLeaflet(List<Leaflet> leaflets) {

        double totalJewels = 0;

        double totalScore = 0;

        for (Leaflet leaflet : leaflets) {

            int countLeaflet = 0;

            for (String color : getColors()) {
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

    public double getCollectedNumberOfJewelsInLeaflet(List<Leaflet> leaflets) {

        double totalCollectedJewels = 0;

        for (Leaflet leaflet : leaflets) {

            for (String color : getColors()) {
                if (leaflet.getTotalNumberOfType(color) != -1)
                    totalCollectedJewels += leaflet.getCollectedNumberOfType(color);
            }

        }

        return totalCollectedJewels;
    }

    public String[] getColors() {
        return colors;
    }

    public void setColors(String[] colors) {
        this.colors = colors;
    }

    public MemoryObject getInnerSenseAOMO() {
        return innerSenseAOMO;
    }

    public void setInnerSenseAOMO(MemoryObject innerSenseAOMO) {
        this.innerSenseAOMO = innerSenseAOMO;
    }
}
