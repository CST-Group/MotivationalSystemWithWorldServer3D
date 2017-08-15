package br.unicamp.mtwsapp.codelets.episodic;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.motivational.Appraisal;
import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.representation.owrl.AbstractObject;
import br.unicamp.cst.representation.owrl.Property;
import br.unicamp.cst.representation.owrl.QualityDimension;
import br.unicamp.cst.util.Pair;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import ws3dproxy.model.Thing;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by du on 31/07/17.
 */
public class EpisodicMemoryGeneratorCodelet extends Codelet {

    public final static String OUTPUT_EPISODIC_MEMORY = "OUTPUT_EPISODIC_MEMORY";


    private MemoryObject inputVisionMO;
    private MemoryObject inputKnownJewelsMO;
    private MemoryObject inputInnerSenseMO;
    private MemoryObject inputAppraisalMO;
    private MemoryContainer inputDrivesMO;
    private MemoryObject outputEpisodicsMemoryMO;

    private AbstractObject previousAO = null;
    private AbstractObject afterAO = null;

    private HashMap<Long, Pair> episodics;
    private int timeCounter = 0;
    private int time = 1;


    private Date initDate;

    public EpisodicMemoryGeneratorCodelet(String name, int time) {
        setName(name);
        setInitDate(Calendar.getInstance().getTime());
        setEpisodics(new HashMap<>());
        setPreviousAO(null);
        setTime(time);
        setTimeStep(time * 1000);
    }

    @Override
    public void accessMemoryObjects() {

        if(getInputDrivesMO() == null){
            setInputDrivesMO((MemoryContainer) getInput("INPUT_DRIVES_MEMORY"));
        }

        if (getInputVisionMO() == null) {
            setInputVisionMO((MemoryObject) getInput("VISION"));
        }

        if (getInputKnownJewelsMO() == null) {
            setInputKnownJewelsMO((MemoryObject) getInput("KNOWN_JEWELS"));
        }

        if (getInputInnerSenseMO() == null) {
            setInputInnerSenseMO((MemoryObject) getInput("INNER"));
        }

        if (getInputAppraisalMO() == null) {
            setInputAppraisalMO((MemoryObject) getInput("OUTPUT_APPRAISAL_MEMORY"));
        }

        if (getOutputEpisodicsMemoryMO() == null) {
            setOutputEpisodicsMemoryMO((MemoryObject) getOutput(OUTPUT_EPISODIC_MEMORY));
        }

    }

    @Override
    public void calculateActivation() {
        try {
            setActivation(0d);
        } catch (CodeletActivationBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void proc() {

        CreatureInnerSense innerSense = (CreatureInnerSense) getInputInnerSenseMO().getI();

        List<Thing> knownThings = (List<Thing>) getInputVisionMO().getI();
        List<Thing> knownJewels = (List<Thing>) getInputKnownJewelsMO().getI();
        Appraisal currentAppraisal = (Appraisal) getInputAppraisalMO().getI();


        if (innerSense != null && knownThings != null && knownJewels != null && currentAppraisal != null && getInputDrivesMO().getI() != null) {

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/YY HH:mm:ss.SSS");

            //long time = Calendar.getInstance().getTime().getTime() - getInitDate().getTime();

            AbstractObject sceneAO = new AbstractObject("Scene - " + getTime());

            AbstractObject creatureAO = new AbstractObject("Creature");
            AbstractObject innerSenseAO = innerSenseToAbstractObject(innerSense);

            creatureAO.addCompositePart(innerSenseAO);
            creatureAO.addCompositePart(appraisalToAbstractObject(currentAppraisal));

            AbstractObject thingsAO = new AbstractObject("Things");
            thingsAO.addCompositePart(thingsToAbstractObject(knownThings.stream().filter(ap -> ap.getName().toUpperCase().contains("FOOD")).collect(Collectors.toList()), "Foods"));
            thingsAO.addCompositePart(thingsToAbstractObject(knownThings, "Jewels"));
            thingsAO.addCompositePart(thingsToAbstractObject(knownThings.stream().filter(ap -> ap.getName().toUpperCase().contains("BRICK")).collect(Collectors.toList()), "Bricks"));

            sceneAO.addCompositePart(creatureAO);
            sceneAO.addCompositePart(thingsAO);


            AbstractObject drives = new AbstractObject("Drives");

            for (Memory driveMO: (List<Memory>)getInputDrivesMO().getAllMemories().clone()) {
                Drive drive = (Drive) driveMO.getI();
                Property propDrive = new Property(drive.getName());
                propDrive.addQualityDimension(new QualityDimension("Activation", drive.getActivation()));
                propDrive.addQualityDimension(new QualityDimension("UrgencyThreshold", drive.getUrgencyThreshold()));
                propDrive.addQualityDimension(new QualityDimension("EmotionalDistortion", drive.getEmotionalDistortion()));
                propDrive.addQualityDimension(new QualityDimension("Priority", drive.getPriority()));
                propDrive.addQualityDimension(new QualityDimension("Level", drive.getLevel()));
                drives.addProperty(propDrive);
            }
            creatureAO.addCompositePart(drives);


            AbstractObject collisionON = new AbstractObject("Collision");
            Property propColision = new Property("Flag");

            Drive wonDrive = (Drive) getInputDrivesMO().getI();

            if(wonDrive.getName().toUpperCase().contains("AvoidDangerDrive")){
                propColision.addQualityDimension(new QualityDimension("Value", true));
            }
            else{
                propColision.addQualityDimension(new QualityDimension("Value", false));
            }

            collisionON.addProperty(propColision);
            creatureAO.addCompositePart(collisionON);

            String dateString = sdf.format(Calendar.getInstance().getTime());

            Property propTime = new Property("CurrentTime");
            //propTime.addQualityDimension(new QualityDimension("Time", dateString));
            propTime.addQualityDimension(new QualityDimension("Time", getTime() *timeCounter));

            sceneAO.addProperty(propTime);

            Pair<AbstractObject, AbstractObject> episodic = new Pair(getPreviousAO(), sceneAO);
            getEpisodics().put((long)timeCounter, episodic);

            getOutputEpisodicsMemoryMO().setI(getEpisodics());

            setPreviousAO(sceneAO);

            timeCounter++;

        }
    }

    public AbstractObject appraisalToAbstractObject(Appraisal appraisal) {
        AbstractObject appraisalAO = new AbstractObject("Appraisal");
        appraisalAO.addProperty(new Property("CurrentAppraisal", Arrays.asList(new QualityDimension("Evaluation", appraisal.getEvaluation()),
                new QualityDimension("CurrentStateEvaluation", appraisal.getCurrentStateEvaluation()))));

        return appraisalAO;
    }

    public AbstractObject thingsToAbstractObject(List<Thing> things, String name) {

        AbstractObject thingsABS = new AbstractObject(name);

        for (Thing thing : things) {
            AbstractObject temp = thingToAbstractObject(thing, thing.getName());
            temp.addProperty(new Property(thing.getName()));
            thingsABS.addCompositePart(temp);
        }
        return thingsABS;
    }

    public AbstractObject thingToAbstractObject(Thing thing, String name) {

        AbstractObject abs = new AbstractObject(name);
        Property position = new Property("Position");

        position.addQualityDimension(new QualityDimension("X1", thing.getX1()));
        position.addQualityDimension(new QualityDimension("X2", thing.getX2()));
        position.addQualityDimension(new QualityDimension("Y1", thing.getY1()));
        position.addQualityDimension(new QualityDimension("Y2", thing.getY1()));
        abs.addProperty(position);

        Property size = new Property("Size");
        size.addQualityDimension(new QualityDimension("Width", thing.getWidth()));
        size.addQualityDimension(new QualityDimension("Height", thing.getHeight()));
        abs.addProperty(size);

        if (thing.getName().toUpperCase().contains("JEWEL")) {
            Property color = new Property("Material");
            color.addQualityDimension(new QualityDimension("Name", thing.getMaterial().getColorName()));
            abs.addProperty(color);
        }

        if (thing.getName().toUpperCase().contains("FOOD")) {
            Property color = new Property("Material");
            color.addQualityDimension(new QualityDimension("Name", thing.getMaterial().getColorName()));
            abs.addProperty(color);
        }

        abs.addCompositePart(abs);
        return abs;
    }

    public AbstractObject innerSenseToAbstractObject(CreatureInnerSense innerSense) {

        AbstractObject innerSenseAO = new AbstractObject("InnerSense");

        innerSenseAO.addProperty(new Property("Sense", Arrays.asList(new QualityDimension("Fuel", innerSense.getFuel()),
                new QualityDimension("LeafletCompleteRate", innerSense.getLeafletCompleteRate()),
                new QualityDimension("Position", innerSense.getPosition())
        )));

        return innerSenseAO;
    }

    public AbstractObject getPreviousAO() {
        return previousAO;
    }

    public void setPreviousAO(AbstractObject previousAO) {
        this.previousAO = previousAO;
    }

    public MemoryObject getInputInnerSenseMO() {
        return inputInnerSenseMO;
    }

    public void setInputInnerSenseMO(MemoryObject inputInnerSenseMO) {
        this.inputInnerSenseMO = inputInnerSenseMO;
    }

    public MemoryObject getOutputEpisodicsMemoryMO() {
        return outputEpisodicsMemoryMO;
    }

    public void setOutputEpisodicsMemoryMO(MemoryObject outputEpisodicsMemoryMO) {
        this.outputEpisodicsMemoryMO = outputEpisodicsMemoryMO;
    }

    public AbstractObject getAfterAO() {
        return afterAO;
    }

    public void setAfterAO(AbstractObject afterAO) {
        this.afterAO = afterAO;
    }

    public HashMap<Long, Pair> getEpisodics() {
        return episodics;
    }

    public void setEpisodics(HashMap<Long, Pair> episodics) {
        this.episodics = episodics;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public MemoryObject getInputAppraisalMO() {
        return inputAppraisalMO;
    }

    public void setInputAppraisalMO(MemoryObject inputAppraisalMO) {
        this.inputAppraisalMO = inputAppraisalMO;
    }

    public MemoryObject getInputVisionMO() {
        return inputVisionMO;
    }

    public void setInputVisionMO(MemoryObject inputVisionMO) {
        this.inputVisionMO = inputVisionMO;
    }

    public MemoryObject getInputKnownJewelsMO() {
        return inputKnownJewelsMO;
    }

    public void setInputKnownJewelsMO(MemoryObject inputKnownJewelsMO) {
        this.inputKnownJewelsMO = inputKnownJewelsMO;
    }

    public MemoryContainer getInputDrivesMO() {
        return inputDrivesMO;
    }

    public void setInputDrivesMO(MemoryContainer inputDrivesMO) {
        this.inputDrivesMO = inputDrivesMO;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}


