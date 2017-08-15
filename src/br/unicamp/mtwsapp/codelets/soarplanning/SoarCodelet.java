/**
 *
 */
package br.unicamp.mtwsapp.codelets.soarplanning;

import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.motivational.Appraisal;
import br.unicamp.cst.motivational.Goal;
import br.unicamp.cst.representation.owrl.AbstractObject;
import br.unicamp.cst.representation.owrl.Property;
import br.unicamp.cst.representation.owrl.QualityDimension;
import br.unicamp.mtwsapp.codelets.sensors.InnerSense;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import ws3dproxy.model.Thing;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Eduardo Froes
 */
public class SoarCodelet extends br.unicamp.cst.bindings.soar.JSoarCodelet {

    private String id;

    private String pathToCommands;

    private String agentName;

    private File productionPath;

    private boolean startSOARDebugger;

    private MemoryObject outputCommandMO;

    private MemoryObject inputCurrentAppraisalMO;

    private MemoryObject inputFoodsMO;

    private MemoryObject inputJewelsMO;

    private MemoryObject inputInnerSenseMO;

    public final String OUTPUT_COMMAND_MO = "OUTPUT_COMMAND_MO";

    public final String INPUT_CURRENT_APPRAISAL_MO = "INPUT_CURRENT_APPRAISAL_MO";

    public final String INPUT_FOODS_MO = "INPUT_FOODS_MO";

    public final String INPUT_JEWELS_MO = "INPUT_JEWELS_MO";

    public final String INPUT_INNER_SENSE_MO = "INPUT_INNER_SENSE_MO";

    public SoarCodelet(String id) {
        this.setId(id);
        setName(id);
        setPathToCommands("");
    }

    public SoarCodelet(String id, String path_to_commands, String _agentName, File _productionPath, Boolean startSOARDebugger) {

        this.setId(id);
        setName(id);
        this.agentName = _agentName;
        this.productionPath = _productionPath;
        this.startSOARDebugger = startSOARDebugger;

        setPathToCommands(path_to_commands);
        initSoarPlugin(_agentName, _productionPath, startSOARDebugger);
    }


    @Override
    public void accessMemoryObjects() {

        if (outputCommandMO == null) {
            outputCommandMO = (MemoryObject) getOutput(OUTPUT_COMMAND_MO);
        }

        if (inputCurrentAppraisalMO == null) {
            inputCurrentAppraisalMO = (MemoryObject) getInput(INPUT_CURRENT_APPRAISAL_MO);
        }

        if (inputFoodsMO == null) {
            inputFoodsMO = (MemoryObject) getInput(INPUT_FOODS_MO);
        }


        if (inputJewelsMO == null) {
            inputJewelsMO = (MemoryObject) getInput(INPUT_JEWELS_MO);
        }

        if (inputInnerSenseMO == null) {
            inputInnerSenseMO = (MemoryObject) getInput(INPUT_INNER_SENSE_MO);
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

        AbstractObject il = processCreatureEntities();
        setInputLink(il);

        jsoar.runSOAR();

        ArrayList<Object> commandList = getCommandsOWRL(pathToCommands);

        outputCommandMO.setI(commandList);

        //	fromPlanToAction();

        jsoar.resetSOAR();
    }


    public AbstractObject processCreatureEntities() {

        AbstractObject il = new AbstractObject("InputLink");
        AbstractObject creatureAO = new AbstractObject("Creature");
        AbstractObject perceptionAO = new AbstractObject("Perception");
        ;
        AbstractObject jewelsAO = null;
        AbstractObject foodsAO = null;

        List<Thing> jewelThings = (List<Thing>) inputJewelsMO.getI();

        if (jewelThings != null) {
            jewelsAO = thingsToAbstractObject(jewelThings, "Jewels");
            perceptionAO.addCompositePart(jewelsAO);
        }

        List<Thing> foodThings = (List<Thing>) inputFoodsMO.getI();

        if (foodThings != null) {
            foodsAO = thingsToAbstractObject(foodThings, "Foods");
            perceptionAO.addCompositePart(foodsAO);
        }

        creatureAO.addAggregatePart(perceptionAO);

        Appraisal currentAppraisal = (Appraisal) inputCurrentAppraisalMO.getI();

        if (currentAppraisal != null) {
            creatureAO.addCompositePart(appraisalToAbstractObject(currentAppraisal));
        }

        CreatureInnerSense innerSense = (CreatureInnerSense) inputInnerSenseMO.getI();

        if (innerSense != null) {
            creatureAO.addCompositePart(innerSenseToAbstractObject(innerSense));
        }

        il.addCompositePart(creatureAO);

        return il;
    }


    public AbstractObject innerSenseToAbstractObject(CreatureInnerSense innerSense) {

        AbstractObject innerSenseAO = new AbstractObject("InnerSense");

        innerSenseAO.addProperty(new Property("InnerSense", Arrays.asList(new QualityDimension("Fuel", innerSense.getFuel()),
                new QualityDimension("LeafletCompleteRate", innerSense.getLeafletCompleteRate())
        )));

        return innerSenseAO;
    }

    public AbstractObject appraisalToAbstractObject(Appraisal appraisal) {
        AbstractObject appraisalAO = new AbstractObject(appraisal.getName());
        appraisalAO.addProperty(new Property("Appraisal", Arrays.asList(new QualityDimension("Evaluation", appraisal.getEvaluation()),
                new QualityDimension("CurrentStateEvaluation", appraisal.getCurrentStateEvaluation()))));

        return appraisalAO;
    }

    public AbstractObject thingsToAbstractObject(List<Thing> things, String name) {

        AbstractObject thingsABS = new AbstractObject(name);

        for (Thing thing : things) {
            AbstractObject temp = thingToAbstractObject(thing, thing.getName());
            temp.addProperty(new Property(thing.getName()));
            thingsABS.addAggregatePart(temp);
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
            /*Property color = new Property("MATERIAL");
            color.addQualityDimension(new QualityDimension("NAME", thing.getMaterial().getColorName()));
            abs.addProperty(color);*/
        }

        abs.addCompositePart(abs);
        return abs;
    }

    public AbstractObject convertToAbstractObject(AbstractObject abstractObject, String nodeName) {
        AbstractObject abs = new AbstractObject(nodeName);
        abs.addCompositePart(abstractObject);
        return abs;
    }

    public AbstractObject convertToAbstractObject(List<AbstractObject> abstractObjects, String nodeNameTemplate) {

        AbstractObject configs = new AbstractObject(abstractObjects.toString());

        for (AbstractObject abs : abstractObjects) {
            configs.addAggregatePart(convertToAbstractObject(abs, nodeNameTemplate));
        }
        return configs;
    }

    public AbstractObject goalToAbstractObject(List<Goal> goals) {

        AbstractObject go = new AbstractObject("Goals");

        for (Goal goal : goals) {

            AbstractObject temp = convertToAbstractObject(goal.getGoalAbstractObjects(), "Goals");
            temp.addProperty(new Property(goal.getId()));
            go.addAggregatePart(temp);
        }
        return go;
    }

    public boolean isAbstractObject(Object obj) {

        if (obj.getClass() == AbstractObject.class)
            return true;
        else
            return false;
    }

    public boolean isString(Object obj) {

        if (obj.getClass() == String.class)
            return true;
        else
            return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPathToCommands() {
        return this.pathToCommands;
    }

    public void setPathToCommands(String path) {
        this.pathToCommands = path;
    }

    //public void setPathToRules(String path){
    //    this.pathToRules = path;
    //}
}
