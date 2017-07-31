package br.unicamp.mtwsapp.application;

import br.unicamp.cst.core.entities.*;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.motivational.*;
import br.unicamp.cst.representation.owrl.AbstractObject;
import br.unicamp.cst.representation.owrl.Property;
import br.unicamp.cst.representation.owrl.QualityDimension;
import br.unicamp.cst.util.MindViewer;
import br.unicamp.mtwsapp.codelets.appraisal.CurrentAppraisalCodelet;
import br.unicamp.mtwsapp.codelets.emotional.AmbitionEmotionalCodelet;
import br.unicamp.mtwsapp.codelets.emotional.HungerEmotionalCodelet;
import br.unicamp.mtwsapp.codelets.mood.AmbitionMoodCodelet;
import br.unicamp.mtwsapp.codelets.mood.HungerMoodCodelet;
import br.unicamp.mtwsapp.codelets.motivational.BoredomMotivationalCodelet;
import br.unicamp.mtwsapp.codelets.motivationalbehaviors.*;
import br.unicamp.mtwsapp.codelets.motivational.AvoidDangerMotivationalCodelet;
import br.unicamp.mtwsapp.codelets.motivational.AmbitionMotivationalCodelet;
import br.unicamp.mtwsapp.codelets.motivational.HungerMotivationalCodelet;
import br.unicamp.mtwsapp.codelets.motor.HandsActionCodelet;
import br.unicamp.mtwsapp.codelets.motor.LegsActionCodelet;
import br.unicamp.mtwsapp.codelets.perception.AppleDetector;
import br.unicamp.mtwsapp.codelets.perception.ClosestAppleDetector;
import br.unicamp.mtwsapp.codelets.perception.ClosestJewelDetector;
import br.unicamp.mtwsapp.codelets.perception.ClosestObstacleDetector;
import br.unicamp.mtwsapp.codelets.perception.JewelDetector;
import br.unicamp.mtwsapp.codelets.sensors.InnerSense;
import br.unicamp.mtwsapp.codelets.sensors.Vision;

import java.util.*;

import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import br.unicamp.mtwsapp.support.SimulationController;
import ws3dproxy.model.Thing;

/**
 * @author Du
 */
public class AgentMind extends Mind {

    private static int creatureBasicSpeed = 3;


    public AgentMind(Environment env) {
        super();

        // Declare Motivational Codelets
        Codelet hungerMotivationalCodelet = null;
        Codelet avoidDangerMotivationalCodelet = null;
        Codelet ambitionMotivationalCodelet = null;
        Codelet boredomMotivationalCodelet = null;
        Codelet hungerEmotionalCodelet = null;
        Codelet ambitionEmotionalCodelet = null;
        //==================================


        // Declare Memory Objects
        MemoryObject visionMO;
        MemoryObject innerSenseMO;
        MemoryObject closestAppleMO;
        MemoryObject knownApplesMO;
        MemoryObject closestJewelMO;
        MemoryObject knownJewelsMO;
        MemoryObject closestObstacleMO;
        MemoryObject hiddenObjetecsMO;
        int reachDistance = 65;
        int brickDistance = 30;
        //===============================

        closestAppleMO = createMemoryObject("CLOSEST_APPLE");
        knownApplesMO = createMemoryObject("KNOWN_APPLES", Collections.synchronizedList(new ArrayList<Thing>()));

        closestJewelMO = createMemoryObject("CLOSEST_JEWEL");

        closestObstacleMO = createMemoryObject("CLOSEST_OBSTACLE");

        List<Thing> knownJewels = Collections.synchronizedList(new ArrayList<Thing>());
        knownJewelsMO = createMemoryObject("KNOWN_JEWELS", knownJewels);

        hiddenObjetecsMO = createMemoryObject("HIDDEN_THINGS", Collections.synchronizedList(new ArrayList<Thing>()));
        //==============================

        // Create Sensor Codelets
        Codelet vision = new Vision("VisionCodelet", env.c);
        visionMO = createMemoryObject("VISION", Collections.synchronizedList(new ArrayList<Thing>()));
        vision.addOutput(visionMO);
        insertCodelet(vision); //Creates a vision sensor

        Codelet innerSense = new InnerSense("InnerSenseCodelet", env.c);
        innerSenseMO = createMemoryObject("INNER", new CreatureInnerSense());
        innerSense.addOutput(innerSenseMO);
        insertCodelet(innerSense); //A sensor for the inner state of the creature
        //==============================

        // Create Motivational Codelets
        try {
            hungerMotivationalCodelet = new HungerMotivationalCodelet("HungerDrive", 0, 0.3, 0.8);
            avoidDangerMotivationalCodelet = new AvoidDangerMotivationalCodelet("AvoidDangerDrive", 0, 0.45, 0.8);
            ambitionMotivationalCodelet = new AmbitionMotivationalCodelet("AmbitionDrive", 0, 0.2, 0.9);
            boredomMotivationalCodelet = new BoredomMotivationalCodelet("BoredomDrive", 0, 0.4, 0.5);
        } catch (CodeletActivationBoundsException e) {
            e.printStackTrace();
        }
        //==============================

        // Create Perception Codelets
        Codelet ad = new AppleDetector("AppleDetectorCodelet", env.c);
        ad.addInput(visionMO);
        ad.addInput(hiddenObjetecsMO);
        ad.addOutput(knownApplesMO);
        insertCodelet(ad);

        Codelet closestAppleDetector = new ClosestAppleDetector("ClosestAppleDetectorCodelet", env.c, reachDistance);
        closestAppleDetector.addInput(knownApplesMO);
        closestAppleDetector.addInput(innerSenseMO);
        closestAppleDetector.addOutput(closestAppleMO);

        insertCodelet(closestAppleDetector);

        Codelet aj = new JewelDetector("JewelDetectorCodelet", env.c);
        aj.addInput(visionMO);
        aj.addOutput(knownJewelsMO);
        insertCodelet(aj);

        Codelet closestJewelDetector = new ClosestJewelDetector("ClosestJewelDetectorCodelet", env.c, reachDistance);
        closestJewelDetector.addInput(knownJewelsMO);
        closestJewelDetector.addInput(innerSenseMO);
        closestJewelDetector.addOutput(closestJewelMO);
        insertCodelet(closestJewelDetector);


        Codelet closestObstacleDetector = new ClosestObstacleDetector("ClosestObstacleDetectorCodelet", env.c, brickDistance);
        closestObstacleDetector.addInput(visionMO);
        closestObstacleDetector.addInput(innerSenseMO);
        closestObstacleDetector.addOutput(closestObstacleMO);
        insertCodelet(closestObstacleDetector);
        //==================================

        //Boredom Motivational Codelet
        List<Memory> boredomSensorsMemory = new ArrayList<>();
        boredomSensorsMemory.add(innerSenseMO);

        Memory inputBoredomSensorsMO = createMemoryObject(MotivationalCodelet.INPUT_SENSORS_MEMORY);
        inputBoredomSensorsMO.setI(boredomSensorsMemory);

        boredomMotivationalCodelet.addInput(inputBoredomSensorsMO);

        Memory inputBoredomDrivesMO = createMemoryObject(MotivationalCodelet.INPUT_DRIVES_MEMORY);
        inputBoredomDrivesMO.setI(new HashMap<Memory, Double>());

        boredomMotivationalCodelet.addInput(inputBoredomDrivesMO);

        MemoryObject outputBoredomDriveMO = createMemoryObject(MotivationalCodelet.OUTPUT_DRIVE_MEMORY);

        boredomMotivationalCodelet.addOutput(outputBoredomDriveMO);

        insertCodelet(boredomMotivationalCodelet);
        //=============================

        //Hunger Motivational Codelet
        List<Memory> hungerSensorsMemory = new ArrayList<>();
        hungerSensorsMemory.add(innerSenseMO);
        hungerSensorsMemory.add(knownApplesMO);

        Memory inputHungerSensorsMO = createMemoryObject(MotivationalCodelet.INPUT_SENSORS_MEMORY);
        inputHungerSensorsMO.setI(hungerSensorsMemory);

        hungerMotivationalCodelet.addInput(inputHungerSensorsMO);

        Memory inputHungerDrivesMO = createMemoryObject(MotivationalCodelet.INPUT_DRIVES_MEMORY);
        inputHungerDrivesMO.setI(new HashMap<Memory, Double>());

        hungerMotivationalCodelet.addInput(inputHungerDrivesMO);

        MemoryObject outputHungryDriveMO = createMemoryObject(MotivationalCodelet.OUTPUT_DRIVE_MEMORY);

        hungerMotivationalCodelet.addOutput(outputHungryDriveMO);

        insertCodelet(hungerMotivationalCodelet);
        //=============================


        //Curiosity Motivational Codelet
        List<Memory> ambitionSensorsMemory = new ArrayList<>();
        ambitionSensorsMemory.add(innerSenseMO);
        ambitionSensorsMemory.add(knownJewelsMO);

        Memory inputAmbitionSensorsMO = createMemoryObject(MotivationalCodelet.INPUT_SENSORS_MEMORY);
        inputAmbitionSensorsMO.setI(ambitionSensorsMemory);
        ambitionMotivationalCodelet.addInput(inputAmbitionSensorsMO);

        Memory inputAmbitionDrivesMO = createMemoryObject(MotivationalCodelet.INPUT_DRIVES_MEMORY);
        inputAmbitionDrivesMO.setI(new HashMap<Memory, Double>());

        ambitionMotivationalCodelet.addInput(inputAmbitionDrivesMO);

        MemoryObject outputAmbitionDriveMO = createMemoryObject(MotivationalCodelet.OUTPUT_DRIVE_MEMORY);

        ambitionMotivationalCodelet.addOutput(outputAmbitionDriveMO);

        insertCodelet(ambitionMotivationalCodelet);
        //=================================

        //Avoid Danger Motivational Codelet
        List<Memory> avoidDangerSensorsMemory = new ArrayList<>();
        avoidDangerSensorsMemory.add(visionMO);
        avoidDangerSensorsMemory.add(closestObstacleMO);

        Memory inputAvoidDangerSensorsMO = createMemoryObject(MotivationalCodelet.INPUT_SENSORS_MEMORY);
        inputAvoidDangerSensorsMO.setI(avoidDangerSensorsMemory);
        avoidDangerMotivationalCodelet.addInput(inputAvoidDangerSensorsMO);

        Memory inputAvoidDangerDrivesMO = createMemoryObject(MotivationalCodelet.INPUT_DRIVES_MEMORY);
        inputAvoidDangerDrivesMO.setI(new HashMap<Memory, Double>());

        avoidDangerMotivationalCodelet.addInput(inputAvoidDangerDrivesMO);

        MemoryObject outputAvoidDangerDriveMO = createMemoryObject(MotivationalCodelet.OUTPUT_DRIVE_MEMORY);

        avoidDangerMotivationalCodelet.addOutput(outputAvoidDangerDriveMO);

        insertCodelet(avoidDangerMotivationalCodelet);
        //=================================

        // Create Motivational Behavior Codelets
        Codelet goToClosestJewel = new GoToJewel("GoToJewelMotivationalBehaviorCodelet", creatureBasicSpeed);
        MemoryObject legsGoJewelMO = createMemoryObject("LEGS_GO_JEWEL");
        goToClosestJewel.addInput(outputAmbitionDriveMO);
        goToClosestJewel.addInput(knownJewelsMO);
        goToClosestJewel.addOutput(legsGoJewelMO);
        insertCodelet(goToClosestJewel);

        Codelet getJewel = new GetClosestJewel("GetClosestJewelMotivationalBehaviorCodelet", reachDistance);
        MemoryObject handsGetJewelMO = createMemoryObject("HANDS_GET_JEWEL");
        getJewel.addInput(outputAmbitionDriveMO);
        getJewel.addInput(closestJewelMO);
        getJewel.addInput(innerSenseMO);
        getJewel.addOutput(handsGetJewelMO);
        insertCodelet(getJewel);

        Codelet avoidColisionObstacle = new AvoidColisionObstacle("AvoidColisionObstacleMotivationalBehaviorCodelet");
        MemoryObject legsAvoidColisionMO = createMemoryObject("LEGS_AVOID_DANGER");
        MemoryObject handsAvoidColisionMO = createMemoryObject("HANDS_AVOID_DANGER");

        avoidColisionObstacle.addInput(knownJewelsMO);
        avoidColisionObstacle.addInput(outputAvoidDangerDriveMO);
        avoidColisionObstacle.addInput(closestObstacleMO);
        avoidColisionObstacle.addOutput(legsAvoidColisionMO);
        avoidColisionObstacle.addOutput(handsAvoidColisionMO);
        insertCodelet(avoidColisionObstacle);

        Codelet eatApple = new EatClosestApple("EatClosestAppleMotivationalBehaviorCodelet", reachDistance);
        MemoryObject handsEatAppleMO = createMemoryObject("HANDS_EAT_APPLE");
        eatApple.addInput(outputHungryDriveMO);
        eatApple.addInput(closestAppleMO);
        eatApple.addInput(hiddenObjetecsMO);
        eatApple.addInput(innerSenseMO);
        eatApple.addOutput(handsEatAppleMO);
        insertCodelet(eatApple);

        Codelet goToClosestApple = new GoToApple("GoToAppleMotivationalBehaviorCodelet", creatureBasicSpeed, env.c);
        MemoryObject legsGoAppleMO = createMemoryObject("LEGS_GO_APPLE");
        goToClosestApple.addInput(outputHungryDriveMO);
        goToClosestApple.addInput(knownApplesMO);
        goToClosestApple.addOutput(legsGoAppleMO);
        insertCodelet(goToClosestApple);

        Codelet randomMove = new RandomMove("RandomMoveMotivationalBehaviorCodelet");
        MemoryObject legsRandomMoveMO = createMemoryObject("LEGS_RANDOM_MOVE");
        randomMove.addInput(outputBoredomDriveMO);
        randomMove.addOutput(legsRandomMoveMO);
        insertCodelet(randomMove);
        //=======================================


        // Create Actuator Codelets
        Codelet legs = new LegsActionCodelet("LegsActionCodelet", env.c);
        MemoryContainer legsBehaviorMC = createMemoryContainer("BEHAVIORS_MC");
        legsBehaviorMC.add(legsGoAppleMO);
        legsBehaviorMC.add(legsGoJewelMO);
        legsBehaviorMC.add(legsAvoidColisionMO);
        legsBehaviorMC.add(legsRandomMoveMO);
        legs.addInput(legsBehaviorMC);

        insertCodelet(legs);

        Codelet hands = new HandsActionCodelet("HandsActionCodelet", env.c);
        MemoryContainer handsBehaviorMC = createMemoryContainer("BEHAVIORS_MC");
        handsBehaviorMC.add(handsEatAppleMO);
        handsBehaviorMC.add(handsGetJewelMO);
        handsBehaviorMC.add(handsAvoidColisionMO);
        hands.addInput(handsBehaviorMC);
        hands.addOutput(hiddenObjetecsMO);
        hands.addInput(visionMO);

        insertCodelet(hands);
        //============================

        // Appraisal Codelet
        CurrentAppraisalCodelet currentAppraisalCodelet = new CurrentAppraisalCodelet("CurrentAppraisalCodelet");

        AbstractObject perceptionAO = new AbstractObject("CurrentPerception");
        Property innerSenseProperty = new Property("InnerSense");
        innerSenseProperty.addQualityDimension(new QualityDimension("cis", innerSenseMO));
        innerSenseProperty.addQualityDimension(new QualityDimension("drives", Arrays.asList(outputAvoidDangerDriveMO,
                outputAmbitionDriveMO,
                outputBoredomDriveMO,
                outputHungryDriveMO)));

        perceptionAO.addProperty(innerSenseProperty);

        MemoryObject inputPerceptionMO = createMemoryObject(AppraisalCodelet.INPUT_ABSTRACT_OBJECT_MEMORY, perceptionAO);
        currentAppraisalCodelet.addInput(inputPerceptionMO);

        MemoryObject outputPerceptionMO = createMemoryObject(AppraisalCodelet.OUTPUT_ABSTRACT_OBJECT_MEMORY);
        currentAppraisalCodelet.addOutput(outputPerceptionMO);

        MemoryObject outputAppraisalMO = createMemoryObject(AppraisalCodelet.OUTPUT_APPRAISAL_MEMORY);
        currentAppraisalCodelet.addOutput(outputAppraisalMO);

        insertCodelet(currentAppraisalCodelet);
        //==================================

        // Mood Codelets
        HungerMoodCodelet hungerMoodCodelet = new HungerMoodCodelet("HungerMood");
        hungerMoodCodelet.addInput(createMemoryObject(MoodCodelet.INPUT_DRIVES_MEMORY, new ArrayList<>()));
        hungerMoodCodelet.addInput(createMemoryObject(MoodCodelet.INPUT_SENSORY_MEMORY, new ArrayList<>()));
        hungerMoodCodelet.addInput(createMemoryObject(MoodCodelet.INPUT_APPRAISAL_MEMORY, outputAppraisalMO));

        Memory hungerMoodMO = createMemoryObject(MoodCodelet.OUTPUT_MOOD_MEMORY);
        hungerMoodCodelet.addOutput(hungerMoodMO);

        insertCodelet(hungerMoodCodelet);

        AmbitionMoodCodelet ambitionMoodCodelet = new AmbitionMoodCodelet("AmbitionMood");
        ambitionMoodCodelet.addInput(createMemoryObject(MoodCodelet.INPUT_DRIVES_MEMORY, new ArrayList<>()));
        ambitionMoodCodelet.addInput(createMemoryObject(MoodCodelet.INPUT_SENSORY_MEMORY, new ArrayList<>()));
        ambitionMoodCodelet.addInput(createMemoryObject(MoodCodelet.INPUT_APPRAISAL_MEMORY, outputAppraisalMO));

        Memory ambitionMoodMO = createMemoryObject(MoodCodelet.OUTPUT_MOOD_MEMORY);
        ambitionMoodCodelet.addOutput(ambitionMoodMO);

        insertCodelet(ambitionMoodCodelet);
        //===================================

        // Emotional Codelets
       try {
            hungerEmotionalCodelet = new HungerEmotionalCodelet("HungerEmotion");
            ambitionEmotionalCodelet = new AmbitionEmotionalCodelet("AmbitionEmotion");
        } catch (CodeletActivationBoundsException e) {
            e.printStackTrace();
        }

        Memory inputHungerMoodMO = createMemoryObject(EmotionalCodelet.INPUT_MOOD_MEMORY, hungerMoodMO);
        Memory inputDrivesHungerMO = createMemoryObject(EmotionalCodelet.INPUT_DRIVES_MEMORY, new HashMap<Memory, Double>());
        Memory inputAffectedHungerDriveMO = createMemoryObject(EmotionalCodelet.INPUT_AFFECTED_DRIVE_MEMORY, outputHungryDriveMO);
        Memory outputAffectedHungerDriveMO = createMemoryObject(EmotionalCodelet.OUTPUT_AFFECTED_DRIVE_MEMORY);

        hungerEmotionalCodelet.addInput(inputHungerMoodMO);
        hungerEmotionalCodelet.addInput(inputDrivesHungerMO);
        hungerEmotionalCodelet.addInput(inputAffectedHungerDriveMO);
        hungerEmotionalCodelet.addOutput(outputAffectedHungerDriveMO);

        insertCodelet(hungerEmotionalCodelet);

        Memory inputAmbitionMoodMO = createMemoryObject(EmotionalCodelet.INPUT_MOOD_MEMORY, ambitionMoodMO);
        Memory inputDrivesAmbitionMO = createMemoryObject(EmotionalCodelet.INPUT_DRIVES_MEMORY, new HashMap<Memory, Double>());
        Memory inputAffectAmbitionDriveMO = createMemoryObject(EmotionalCodelet.INPUT_AFFECTED_DRIVE_MEMORY, outputAmbitionDriveMO);
        Memory outputAffectedAmbitionDriveMO = createMemoryObject(EmotionalCodelet.OUTPUT_AFFECTED_DRIVE_MEMORY);

        ambitionEmotionalCodelet.addInput(inputAmbitionMoodMO);
        ambitionEmotionalCodelet.addInput(inputAffectAmbitionDriveMO);
        ambitionEmotionalCodelet.addInput(inputDrivesAmbitionMO);
        ambitionEmotionalCodelet.addOutput(outputAffectedAmbitionDriveMO);

        insertCodelet(ambitionEmotionalCodelet);

        //===================================

        //Create and Populate MotivationalSubsystemViewer
        List<Codelet> mtCodelets = new ArrayList<>();
        mtCodelets.add(avoidDangerMotivationalCodelet);
        mtCodelets.add(ambitionMotivationalCodelet);
        mtCodelets.add(hungerMotivationalCodelet);
        mtCodelets.add(boredomMotivationalCodelet);


        List<Codelet> emCodelets = new ArrayList<>();
        emCodelets.add(hungerEmotionalCodelet);
        emCodelets.add(ambitionEmotionalCodelet);

        List<Codelet> apCodelets = new ArrayList<>();
        apCodelets.add(currentAppraisalCodelet);

        List<Codelet> mdCodelets = new ArrayList<>();
        mdCodelets.add(hungerMoodCodelet);
        mdCodelets.add(ambitionMoodCodelet);

        List<Codelet> mtbCodelets = new ArrayList<>();
        mtbCodelets.add(avoidColisionObstacle);
        mtbCodelets.add(eatApple);
        mtbCodelets.add(getJewel);
        mtbCodelets.add(goToClosestApple);
        mtbCodelets.add(randomMove);

        //MindViewer mindViewer = new MindViewer(this, "MindViewer", mtbCodelets);
        //mindViewer.initMotivationalSubsystemViewer(mtCodelets, emCodelets, new ArrayList<>(), apCodelets, mdCodelets);

        //mindViewer.setVisible(true);
        //================================================

        // Create and Populate SimulationController
        SimulationController simulationController = new SimulationController("SimulationController");

        simulationController.addMO(outputAmbitionDriveMO);
        simulationController.addMO(outputAvoidDangerDriveMO);
        simulationController.addMO(outputBoredomDriveMO);
        simulationController.addMO(outputHungryDriveMO);

        simulationController.setCreatureInnerSenseMO(innerSenseMO);
        simulationController.setCreature(env.c);
        simulationController.setMind(this);
        simulationController.StartTimer();
        //=================================

        start();


    }


}
