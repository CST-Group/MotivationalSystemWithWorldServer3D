/**
 *
 */
package br.unicamp.mtwsapp.codelets.soarplanning;

import br.unicamp.cst.bindings.soar.JSoarCodelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.motivational.Appraisal;
import br.unicamp.cst.motivational.Goal;
import br.unicamp.cst.representation.owrl.AbstractObject;
import br.unicamp.cst.representation.owrl.Property;
import br.unicamp.cst.representation.owrl.QualityDimension;
import br.unicamp.mtwsapp.codelets.goal.GoalGeneratorCodelet;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author Eduardo Froes
 */
public class SoarPlanningCodelet extends br.unicamp.cst.bindings.soar.JSoarCodelet {

    private String id;

    private String pathToCommands;

    private String agentName;

    private File productionPath;

    private boolean startSOARDebugger;

    private MemoryObject outputCommandMO;

    private MemoryObject outputCurrentSoarPlanMO;

    private MemoryObject inputCurrentAppraisalMO;

    private MemoryObject inputFoodsMO;

    private MemoryObject inputJewelsMO;

    private MemoryObject inputGoalMO;

    private Creature creature;

    private boolean init = false;

    public static final String OUTPUT_CURRENT_SOAR_PLAN_MO = "OUTPUT_CURRENT_SOAR_PLAN_MO";

    public static final String INPUT_FOODS_MO = "INPUT_FOODS_MO";

    public static final String INPUT_JEWELS_MO = "INPUT_JEWELS_MO";

    public SoarPlanningCodelet(String id) {
        this.setId(id);
        setName(id);
        setPathToCommands("");
    }

    public SoarPlanningCodelet(String id, String pathToCommands, String agentName, File productionPath, Boolean startSOARDebugger, Creature creature) {

        setId(id);
        setName(id);
        setAgentName(agentName);
        setProductionPath(productionPath);
        setStartSOARDebugger(startSOARDebugger);

        setPathToCommands(pathToCommands);
        initSoarPlugin(agentName, productionPath, startSOARDebugger);

        setCreature(creature);
    }


    @Override
    public void accessMemoryObjects() {

        if (getOutputCurrentSoarPlanMO() == null) {
            setOutputCurrentSoarPlanMO((MemoryObject) getOutput(OUTPUT_CURRENT_SOAR_PLAN_MO));
        }

        if (getOutputCommandMO() == null) {
            setOutputCommandMO((MemoryObject) getOutput(JSoarCodelet.OUTPUT_COMMAND_MO));
        }

        if (getInputJewelsMO() == null) {
            setInputJewelsMO((MemoryObject) getInput(INPUT_JEWELS_MO));
        }

        if (getInputGoalMO() == null) {
            setInputGoalMO((MemoryObject) getInput(GoalGeneratorCodelet.OUTPUT_GOAL_MEMORY));
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
        if (isInit()) {

            processDataToAbstractObject();

            if(getDebugState() == 0)
                getJsoar().step();

            ArrayList<Object> commandList = getOutputInObject(getPathToCommands());

            if(commandList != null) {
                getOutputCommandMO().setI(commandList);
                getOutputCurrentSoarPlanMO().setI(getOperatorsPathList());
            }

        } else {
            setInit(true);
        }
    }


    public void processDataToAbstractObject() {

        AbstractObject il = null;

        List<Thing> jewelThings = new CopyOnWriteArrayList(((List<Thing>) getInputJewelsMO().getI()));
        Goal goal = (Goal) getInputGoalMO().getI();

        if (jewelThings != null && goal != null) {

            AbstractObject goalAO = goal.getGoalAbstractObjects();

            il = processCreatureEntities(jewelThings, goalAO);

            setInputLinkAO(il);
        }

    }


    public AbstractObject processCreatureEntities(List<Thing> jewelThings, AbstractObject goalAO) {

        AbstractObject il = new AbstractObject("INPUTLINK");
        AbstractObject creatureAO = new AbstractObject("CREATURE");
        AbstractObject perceptionAO = new AbstractObject("PERCEPTION");

        AbstractObject jewelsAO = null;
        AbstractObject foodsAO = null;

        if (jewelThings != null) {
            jewelsAO = thingsToAbstractObject(jewelThings, "JEWELS");
            perceptionAO.addCompositePart(jewelsAO);
        }

        creatureAO.addCompositePart(perceptionAO);

        if (goalAO != null) {
            creatureAO.addCompositePart(goalAO);
        }

        il.addCompositePart(creatureAO);

        return il;
    }

    public AbstractObject appraisalToAbstractObject(Appraisal appraisal) {
        AbstractObject appraisalAO = new AbstractObject(appraisal.getName());
        appraisalAO.addProperty(new Property("APPRAISAL", Arrays.asList(new QualityDimension("EVALUATION", appraisal.getEvaluation()),
                new QualityDimension("CURRENTSTATEEVALUATION", appraisal.getCurrentStateEvaluation()))));

        return appraisalAO;
    }

    public AbstractObject thingsToAbstractObject(List<Thing> things, String name) {

        AbstractObject thingsABS = new AbstractObject(name);

        for (Thing thing : things) {
            AbstractObject temp = thingToAbstractObject(thing);
            thingsABS.addCompositePart(temp);
        }
        return thingsABS;
    }

    public AbstractObject thingToAbstractObject(Thing thing) {
        AbstractObject abs = new AbstractObject("THING");

        Property nameProp = new Property("NAME", new QualityDimension("VALUE", thing.getName()));
        abs.addProperty(nameProp);

        Property position = new Property("POSITION");
        position.addQualityDimension(new QualityDimension("X1", thing.getX1()));
        position.addQualityDimension(new QualityDimension("Y1", thing.getY1()));
        abs.addProperty(position);

        Property distance = new Property("DISTANCE");
        distance.addQualityDimension(new QualityDimension("VALUE", creature.calculateDistanceTo(thing)));
        abs.addProperty(distance);


        Property color = new Property("MATERIAL");
        color.addQualityDimension(new QualityDimension("TYPE", thing.getMaterial().getColorName().toUpperCase()));
        abs.addProperty(color);

        return abs;
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

    public MemoryObject getOutputCommandMO() {
        return outputCommandMO;
    }

    public void setOutputCommandMO(MemoryObject outputCommandMO) {
        this.outputCommandMO = outputCommandMO;
    }

    public MemoryObject getInputCurrentAppraisalMO() {
        return inputCurrentAppraisalMO;
    }

    public void setInputCurrentAppraisalMO(MemoryObject inputCurrentAppraisalMO) {
        this.inputCurrentAppraisalMO = inputCurrentAppraisalMO;
    }

    public MemoryObject getInputFoodsMO() {
        return inputFoodsMO;
    }

    public void setInputFoodsMO(MemoryObject inputFoodsMO) {
        this.inputFoodsMO = inputFoodsMO;
    }

    public MemoryObject getInputJewelsMO() {
        return inputJewelsMO;
    }

    public void setInputJewelsMO(MemoryObject inputJewelsMO) {
        this.inputJewelsMO = inputJewelsMO;
    }

    public MemoryObject getInputGoalMO() {
        return inputGoalMO;
    }

    public void setInputGoalMO(MemoryObject inputGoalMO) {
        this.inputGoalMO = inputGoalMO;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public File getProductionPath() {
        return productionPath;
    }

    public void setProductionPath(File productionPath) {
        this.productionPath = productionPath;
    }

    public boolean isStartSOARDebugger() {
        return startSOARDebugger;
    }

    public void setStartSOARDebugger(boolean startSOARDebugger) {
        this.startSOARDebugger = startSOARDebugger;
    }

    public Creature getCreature() {
        return creature;
    }

    public void setCreature(Creature creature) {
        this.creature = creature;
    }

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    public MemoryObject getOutputCurrentSoarPlanMO() {
        return outputCurrentSoarPlanMO;
    }

    public void setOutputCurrentSoarPlanMO(MemoryObject outputCurrentSoarPlanMO) {
        this.outputCurrentSoarPlanMO = outputCurrentSoarPlanMO;
    }
}
