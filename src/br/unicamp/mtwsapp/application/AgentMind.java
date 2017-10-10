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
import br.unicamp.mtwsapp.codelets.goal.GoalGeneratorCodelet;
import br.unicamp.mtwsapp.codelets.mood.AmbitionMoodCodelet;
import br.unicamp.mtwsapp.codelets.mood.HungerMoodCodelet;
import br.unicamp.mtwsapp.codelets.motivational.*;
import br.unicamp.mtwsapp.codelets.motivationalbehaviors.*;
import br.unicamp.mtwsapp.codelets.motor.HandsActionCodelet;
import br.unicamp.mtwsapp.codelets.motor.LegsActionCodelet;
import br.unicamp.mtwsapp.codelets.perception.AppleDetector;
import br.unicamp.mtwsapp.codelets.perception.ClosestAppleDetector;
import br.unicamp.mtwsapp.codelets.perception.ClosestJewelDetector;
import br.unicamp.mtwsapp.codelets.perception.ClosestObstacleDetector;
import br.unicamp.mtwsapp.codelets.perception.JewelDetector;
import br.unicamp.mtwsapp.codelets.sensors.InnerSense;
import br.unicamp.mtwsapp.codelets.sensors.Vision;

import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import br.unicamp.mtwsapp.codelets.soarplanning.PlanSelectionCodelet;
import br.unicamp.mtwsapp.codelets.soarplanning.SoarPlanningCodelet;
import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import br.unicamp.mtwsapp.support.SimulationController;
import ws3dproxy.model.Thing;

/**
 * @author Du
 */
public class AgentMind extends Mind {

    private static int creatureBasicSpeed = 3;

    private static String soarRulesPath = "soarRules/soarPlanning.soar";


    public AgentMind(Environment env) {
        super();

        // Declare Motivational Codelets
        MotivationalCodelet hungerMotivationalCodelet = null;
        MotivationalCodelet avoidDangerMotivationalCodelet = null;
        MotivationalCodelet ambitionMotivationalCodelet = null;
        MotivationalCodelet boredomMotivationalCodelet = null;
        MotivationalCodelet esteemMotivationalCodelet = null;
        EmotionalCodelet hungerEmotionalCodelet = null;
        EmotionalCodelet ambitionEmotionalCodelet = null;
        //==================================


        // Declare Memory Objects
        MemoryObject visionMO;
        MemoryObject innerSenseMO;
        MemoryObject innerSenseAOMO;
        MemoryObject closestAppleMO;
        MemoryObject knownApplesMO;
        MemoryObject closestJewelMO;
        MemoryObject knownJewelsMO;
        MemoryObject closestObstacleMO;
        MemoryObject hiddenObjetecsMO;
        MemoryObject jewelsCollectedMO = createMemoryObject("JEWELS_COLLECTED");
        MemoryObject outputCommandMO = createMemoryObject(SoarPlanningCodelet.OUTPUT_COMMAND_MO);
        MemoryObject outputSelectedPlan = createMemoryObject(PlanSelectionCodelet.OUPUT_SELECTED_PLAN_MO);

        int reachDistance = 65;
        int brickDistance = 48;
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
        innerSenseAOMO = createMemoryObject("INNER_AO");
        innerSense.addOutput(innerSenseMO);
        innerSense.addOutput(innerSenseAOMO);
        insertCodelet(innerSense); //A sensor for the inner state of the creature
        //==============================

        // Create Motivational Codelets
        try {
            hungerMotivationalCodelet = new HungerMotivationalCodelet("HungerDrive", 0, 0.3, 0.8);
            avoidDangerMotivationalCodelet = new AvoidDangerMotivationalCodelet("AvoidDangerDrive", 0, 0.45, 0.7);
            ambitionMotivationalCodelet = new AmbitionMotivationalCodelet("AmbitionDrive", 0, 0.2, 0.9);
            esteemMotivationalCodelet = new EsteemMotivationalCodelet("EsteemDrive", 1, 0.25, 1);
            boredomMotivationalCodelet = new BoredomMotivationalCodelet("BoredomDrive", 0, 0.4, 0.65);
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
        ambitionSensorsMemory.add(jewelsCollectedMO);

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

        //Esteem Motivational Codelet
        List<Memory> esteemSensorsMemory = new ArrayList<>();
        esteemSensorsMemory.add(innerSenseMO);

        Memory inputEsteemSensorsMO = createMemoryObject(MotivationalCodelet.INPUT_SENSORS_MEMORY);
        inputEsteemSensorsMO.setI(esteemSensorsMemory);
        esteemMotivationalCodelet.addInput(inputEsteemSensorsMO);

        Memory inputEsteemDrivesMO = createMemoryObject(MotivationalCodelet.INPUT_DRIVES_MEMORY);
        inputEsteemDrivesMO.setI(new HashMap<Memory, Double>());
        esteemMotivationalCodelet.addInput(inputEsteemDrivesMO);

        MemoryObject outputEsteemDriveMO = createMemoryObject(MotivationalCodelet.OUTPUT_DRIVE_MEMORY);

        esteemMotivationalCodelet.addOutput(outputEsteemDriveMO);

        insertCodelet(esteemMotivationalCodelet);
        //=================================

        // Create Motivational Behavior Codelets
        Codelet goToClosestJewel = new GoToJewel("GoToJewelMotivationalBehaviorCodelet", creatureBasicSpeed, env.c);
        MemoryObject legsGoJewelMO = createMemoryObject("LEGS_GO_JEWEL");
        goToClosestJewel.addInput(innerSenseMO);
        goToClosestJewel.addInput(outputAmbitionDriveMO);
        goToClosestJewel.addInput(knownJewelsMO);
        goToClosestJewel.addInput(outputSelectedPlan);
        goToClosestJewel.addOutput(legsGoJewelMO);
        insertCodelet(goToClosestJewel);

        Codelet goToDeliverySpot = new GoToDeliverySpot("GoToDeliverySpotMotivationalBehaviorCodelet", creatureBasicSpeed, env.c);
        MemoryObject legsGoDeliverySpotMO = createMemoryObject("LEGS_GO_DELIVERY_SPOT");
        goToDeliverySpot.addInput(innerSenseMO);
        goToDeliverySpot.addInput(outputEsteemDriveMO);
        goToDeliverySpot.addOutput(legsGoDeliverySpotMO);
        insertCodelet(goToDeliverySpot);

        Codelet getJewel = new GetClosestJewel("GetClosestJewelMotivationalBehaviorCodelet", reachDistance, env.c);
        MemoryObject handsGetJewelMO = createMemoryObject("HANDS_GET_JEWEL");
        getJewel.addInput(outputAmbitionDriveMO);
        getJewel.addInput(closestJewelMO);
        getJewel.addInput(innerSenseMO);
        getJewel.addInput(outputSelectedPlan);
        getJewel.addOutput(handsGetJewelMO);
        insertCodelet(getJewel);

        Codelet avoidColisionObstacle = new AvoidColisionObstacle("AvoidColisionObstacleMotivationalBehaviorCodelet", env.c);
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
        legsBehaviorMC.add(legsGoDeliverySpotMO);
        legs.addInput(legsBehaviorMC);

        insertCodelet(legs);

        Codelet hands = new HandsActionCodelet("HandsActionCodelet", env.c);
        MemoryContainer handsBehaviorMC = createMemoryContainer("BEHAVIORS_MC");
        handsBehaviorMC.add(handsEatAppleMO);
        handsBehaviorMC.add(handsGetJewelMO);
        handsBehaviorMC.add(handsAvoidColisionMO);
        hands.addInput(handsBehaviorMC);
        hands.addOutput(hiddenObjetecsMO);
        hands.addOutput(jewelsCollectedMO);
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

        // Episodic Codelets
        /*Codelet episodicMemoryGeneratorCodelet = new EpisodicMemoryGeneratorCodelet("EpisodicMemoryGeneratorCodelet", 1);
        episodicMemoryGeneratorCodelet.addInput(innerSenseMO);
        episodicMemoryGeneratorCodelet.addInput(visionMO);
        episodicMemoryGeneratorCodelet.addInput(knownJewelsMO);
        episodicMemoryGeneratorCodelet.addInput(outputAppraisalMO);

        MemoryContainer drivesMC = createMemoryContainer("INPUT_DRIVES_MEMORY");
        drivesMC.add(outputAmbitionDriveMO);
        drivesMC.add(outputHungryDriveMO);
        drivesMC.add(outputBoredomDriveMO);
        drivesMC.add(outputAvoidDangerDriveMO);

        episodicMemoryGeneratorCodelet.addInput(drivesMC);

        Memory outputEpisodicsMemoryMO = createMemoryObject(EpisodicMemoryGeneratorCodelet.OUTPUT_EPISODIC_MEMORY);
        episodicMemoryGeneratorCodelet.addOutput(outputEpisodicsMemoryMO);
        insertCodelet(episodicMemoryGeneratorCodelet);*/
        //===================================

        // Expectation Codelets
        /*ExpectationCodelet expectationCodelet = new ExpectationCodelet("ExpectationCodelet", 180);
        expectationCodelet.addInput(outputEpisodicsMemoryMO);

        Memory outputExpectationMemory = createMemoryObject(ExpectationCodelet.OUTPUT_EXPECTATION_MEMORY);
        expectationCodelet.addOutput(outputExpectationMemory);
        insertCodelet(expectationCodelet);*/
        //===================================

        // Goal Codelets
        GoalGeneratorCodelet goalCodelet = new GoalGeneratorCodelet("GoalGenerator");
        goalCodelet.addInput(createMemoryObject(GoalCodelet.INPUT_HYPOTHETICAL_SITUATIONS_MEMORY, innerSenseAOMO));
        MemoryObject outputGoalMO = createMemoryObject(GoalCodelet.OUTPUT_GOAL_MEMORY);
        goalCodelet.addOutput(outputGoalMO);
        insertCodelet(goalCodelet);
        //===================================

        // Soar Codelet
        SoarPlanningCodelet soarCodelet = new SoarPlanningCodelet("SoarPlanning",
                "br.unicamp.mtwsapp.codelets.soarplanning",
                "Creature",
                new File(soarRulesPath),
                false,
                env.c);

        MemoryObject inputFoodsMO = createMemoryObject(SoarPlanningCodelet.INPUT_FOODS_MO, knownApplesMO.getI());
        MemoryObject inputJewelsMO = createMemoryObject(SoarPlanningCodelet.INPUT_JEWELS_MO, knownJewelsMO.getI());

        soarCodelet.addInput(inputFoodsMO);
        soarCodelet.addInput(inputJewelsMO);
        soarCodelet.addInput(outputGoalMO);
        soarCodelet.addOutput(outputCommandMO);
        soarCodelet.addOutput(createMemoryObject(SoarPlanningCodelet.OUTPUT_CURRENT_SOAR_PLAN_MO));

        insertCodelet(soarCodelet);

        PlanSelectionCodelet planSelectionCodelet = new PlanSelectionCodelet("PlanSelection", env.c);
        planSelectionCodelet.addInput(innerSenseMO);
        planSelectionCodelet.addInput(outputCommandMO);
        planSelectionCodelet.addOutput(outputSelectedPlan);

        insertCodelet(planSelectionCodelet);

        //===================================

        //Create and Populate MotivationalSubsystemViewer
        List<MotivationalCodelet> mtCodelets = new ArrayList<>();
        mtCodelets.add(avoidDangerMotivationalCodelet);
        mtCodelets.add(ambitionMotivationalCodelet);
        mtCodelets.add(hungerMotivationalCodelet);
        mtCodelets.add(boredomMotivationalCodelet);
        mtCodelets.add(esteemMotivationalCodelet);

        List<EmotionalCodelet> emCodelets = new ArrayList<>();
        emCodelets.add(hungerEmotionalCodelet);
        emCodelets.add(ambitionEmotionalCodelet);

        List<AppraisalCodelet> apCodelets = new ArrayList<>();
        apCodelets.add(currentAppraisalCodelet);

        List<MoodCodelet> mdCodelets = new ArrayList<>();
        mdCodelets.add(hungerMoodCodelet);
        mdCodelets.add(ambitionMoodCodelet);

        List<GoalCodelet> goCodelets = new ArrayList<>();
        goCodelets.add(goalCodelet);

        List<Codelet> mtbCodelets = new ArrayList<>();
        mtbCodelets.add(avoidColisionObstacle);
        mtbCodelets.add(eatApple);
        mtbCodelets.add(getJewel);
        mtbCodelets.add(goToClosestApple);
        mtbCodelets.add(goToClosestJewel);
        mtbCodelets.add(randomMove);
        mtbCodelets.add(goToDeliverySpot);

        getMotivationalSubsystemModule().setMotivationalCodelets(mtCodelets);
        getMotivationalSubsystemModule().setEmotionalCodelets(emCodelets);
        getMotivationalSubsystemModule().setGoalCodelets(goCodelets);
        getMotivationalSubsystemModule().setAppraisalCodelets(apCodelets);
        getMotivationalSubsystemModule().setMoodCodelets(mdCodelets);

        getPlansSubsystemModule().setjSoarCodelet(soarCodelet);

        MindViewer mindViewer = new MindViewer(this, "MindViewer", mtbCodelets);

        mindViewer.setVisible(true);
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
