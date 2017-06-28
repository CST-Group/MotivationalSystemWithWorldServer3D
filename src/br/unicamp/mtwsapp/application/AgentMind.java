package br.unicamp.mtwsapp.application;

import br.unicamp.cst.core.entities.*;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.motivational.*;
import br.unicamp.cst.representation.owrl.AbstractObject;
import br.unicamp.cst.representation.owrl.Property;
import br.unicamp.cst.representation.owrl.QualityDimension;
import br.unicamp.mtwsapp.codelets.appraisal.CurrentAppraisal;
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

import java.lang.reflect.Array;
import java.util.*;

import br.unicamp.mtwsapp.memory.CreatureInnerSense;
import br.unicamp.mtwsapp.support.MindView;
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
        MemoryObject legsMO;
        MemoryObject handsMO;
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

        //Initialize Memory Objects
        legsMO = createMemoryObject("LEGS", "");
        handsMO = createMemoryObject("HANDS", "");

        List<Thing> vision_list = Collections.synchronizedList(new ArrayList<Thing>());
        visionMO = createMemoryObject("VISION", vision_list);

        CreatureInnerSense cis = new CreatureInnerSense();
        innerSenseMO = createMemoryObject("INNER", cis);

        Thing closestApple = null;
        closestAppleMO = createMemoryObject("CLOSEST_APPLE", closestApple);

        List<Thing> knownApples = Collections.synchronizedList(new ArrayList<Thing>());
        knownApplesMO = createMemoryObject("KNOWN_APPLES", knownApples);

        Thing closestJewel = null;
        closestJewelMO = createMemoryObject("CLOSEST_JEWEL", closestJewel);

        Thing closestObstacle = null;
        closestObstacleMO = createMemoryObject("CLOSEST_OBSTACLE", closestObstacle);

        List<Thing> knownJewels = Collections.synchronizedList(new ArrayList<Thing>());
        knownJewelsMO = createMemoryObject("KNOWN_JEWELS", knownJewels);

        hiddenObjetecsMO = createMemoryObject("HIDDEN_THINGS");
        hiddenObjetecsMO.setI(Collections.synchronizedList(new ArrayList<Thing>()));
        //==============================

        // Create Sensor Codelets
        Codelet vision = new Vision(env.c);
        vision.addOutput(visionMO);
        insertCodelet(vision); //Creates a vision sensor

        Codelet innerSense = new InnerSense(env.c);
        innerSense.addOutput(innerSenseMO);
        insertCodelet(innerSense); //A sensor for the inner state of the creature
        //==============================

        // Create Motivational Codelets
        try {
            hungerMotivationalCodelet = new HungerMotivationalCodelet("HungerDrive", 0, 0.3, 0.8);
            avoidDangerMotivationalCodelet = new AvoidDangerMotivationalCodelet("AvoidDangerDrive", 0, 0.45, 0.8);
            ambitionMotivationalCodelet = new AmbitionMotivationalCodelet("AmbitionDrive", 0, 0.2, 0.9);
            boredomMotivationalCodelet = new BoredomMotivationalCodelet("BoredomDrive", 0, 0.4, 0.8);
        } catch (CodeletActivationBoundsException e) {
            e.printStackTrace();
        }
        //==============================

        // Create Perception Codelets
        Codelet ad = new AppleDetector(env.c);
        ad.addInput(visionMO);
        ad.addInput(hiddenObjetecsMO);
        ad.addOutput(knownApplesMO);
        insertCodelet(ad);

        Codelet closestAppleDetector = new ClosestAppleDetector(env.c, reachDistance);
        closestAppleDetector.addInput(knownApplesMO);
        closestAppleDetector.addInput(innerSenseMO);
        closestAppleDetector.addOutput(closestAppleMO);

        insertCodelet(closestAppleDetector);

        Codelet aj = new JewelDetector(env.c);
        aj.addInput(visionMO);
        aj.addOutput(knownJewelsMO);
        insertCodelet(aj);

        Codelet closestJewelDetector = new ClosestJewelDetector(env.c, reachDistance);
        closestJewelDetector.addInput(knownJewelsMO);
        closestJewelDetector.addInput(innerSenseMO);
        closestJewelDetector.addOutput(closestJewelMO);
        insertCodelet(closestJewelDetector);


        Codelet closestObstacleDetector = new ClosestObstacleDetector(env.c, brickDistance);
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
        Codelet goToClosestJewel = new GoToJewel(creatureBasicSpeed);
        MemoryObject legsGoJewelMO = createMemoryObject("LEGS_GO_JEWEL");
        goToClosestJewel.addInput(outputAmbitionDriveMO);
        goToClosestJewel.addInput(knownJewelsMO);
        goToClosestJewel.addInput(innerSenseMO);
        goToClosestJewel.addOutput(legsGoJewelMO);
        insertCodelet(goToClosestJewel);

        Codelet getJewel = new GetClosestJewel(reachDistance);
        MemoryObject handsGetJewelMO = createMemoryObject("HANDS_GET_JEWEL");
        getJewel.addInput(outputAmbitionDriveMO);
        getJewel.addInput(closestJewelMO);
        getJewel.addInput(innerSenseMO);
        getJewel.addOutput(handsGetJewelMO);
        insertCodelet(getJewel);

        Codelet avoidColisionObstacle = new AvoidColisionObstacle();
        MemoryObject legsAvoidColisionMO = createMemoryObject("LEGS_AVOID_DANGER");
        MemoryObject handsAvoidColisionMO = createMemoryObject("HANDS_AVOID_DANGER");

        avoidColisionObstacle.addInput(knownJewelsMO);
        avoidColisionObstacle.addInput(outputAvoidDangerDriveMO);
        avoidColisionObstacle.addInput(closestObstacleMO);
        avoidColisionObstacle.addInput(innerSenseMO);
        avoidColisionObstacle.addOutput(legsAvoidColisionMO);
        avoidColisionObstacle.addOutput(handsAvoidColisionMO);
        insertCodelet(avoidColisionObstacle);

        Codelet eatApple = new EatClosestApple(reachDistance);
        MemoryObject handsEatAppleMO = createMemoryObject("HANDS_EAT_APPLE");
        eatApple.addInput(outputHungryDriveMO);
        eatApple.addInput(closestAppleMO);
        eatApple.addInput(innerSenseMO);
        eatApple.addInput(hiddenObjetecsMO);
        eatApple.addOutput(handsEatAppleMO);
        insertCodelet(eatApple);

        Codelet goToClosestApple = new GoToApple(creatureBasicSpeed, env.c);
        MemoryObject legsGoAppleMO = createMemoryObject("LEGS_GO_APPLE");
        goToClosestApple.addInput(outputHungryDriveMO);
        goToClosestApple.addInput(knownApplesMO);
        goToClosestApple.addOutput(legsGoAppleMO);
        insertCodelet(goToClosestApple);

        Codelet randomMove = new RandomMove();
        MemoryObject legsRandomMoveMO = createMemoryObject("LEGS_RANDOM_MOVE");
        randomMove.addInput(outputBoredomDriveMO);
        randomMove.addOutput(legsRandomMoveMO);
        insertCodelet(randomMove);
        //=======================================


        // Create Actuator Codelets
        Codelet legs = new LegsActionCodelet(env.c);
        MemoryContainer legsBehaviorMC = createMemoryContainer("BEHAVIORS_MC");
        legsBehaviorMC.add(legsGoAppleMO);
        legsBehaviorMC.add(legsGoJewelMO);
        legsBehaviorMC.add(legsAvoidColisionMO);
        legsBehaviorMC.add(legsRandomMoveMO);
        legs.addInput(legsBehaviorMC);

        insertCodelet(legs);

        Codelet hands = new HandsActionCodelet(env.c);
        MemoryContainer handsBehaviorMC = createMemoryContainer("BEHAVIORS_MC");
        handsBehaviorMC.add(handsEatAppleMO);
        handsBehaviorMC.add(handsGetJewelMO);
        handsBehaviorMC.add(handsAvoidColisionMO);
        hands.addInput(handsBehaviorMC);
        hands.addInput(hiddenObjetecsMO);
        hands.addInput(visionMO);

        insertCodelet(hands);
        //============================


        // Appraisal Codelet
        CurrentAppraisal currentAppraisal = new CurrentAppraisal("CurrentAppraisal");

        AbstractObject perceptionAO = new AbstractObject("CurrentPerception");
        Property innerSenseProperty = new Property("InnerSense");
        innerSenseProperty.addQualityDimension(new QualityDimension("cis", cis));
        innerSenseProperty.addQualityDimension(new QualityDimension("drives", Arrays.asList(outputAvoidDangerDriveMO,
                outputAmbitionDriveMO,
                outputBoredomDriveMO,
                outputHungryDriveMO)));

        perceptionAO.addProperty(innerSenseProperty);

        MemoryObject inputPerceptionMO = createMemoryObject(AppraisalCodelet.INPUT_ABSTRACT_OBJECT_MEMORY, perceptionAO);
        currentAppraisal.addInput(inputPerceptionMO);

        MemoryObject outputPerceptionMO = createMemoryObject(AppraisalCodelet.OUTPUT_ABSTRACT_OBJECT_MEMORY);
        currentAppraisal.addOutput(outputPerceptionMO);

        MemoryObject outputAppraisalMO = createMemoryObject(AppraisalCodelet.OUTPUT_APPRAISAL_MEMORY);
        currentAppraisal.addOutput(outputAppraisalMO);

        insertCodelet(currentAppraisal);
        //==================================

        // Mood Codelets
        HungerMoodCodelet hungerMoodCodelet = new HungerMoodCodelet("HungerMood");
        hungerMoodCodelet.addInput(createMemoryObject(MoodCodelet.INPUT_DRIVES_MEMORY, Arrays.asList(outputHungryDriveMO)));
        hungerMoodCodelet.addInput(createMemoryObject(MoodCodelet.INPUT_SENSORY_MEMORY, new ArrayList<>()));
        hungerMoodCodelet.addInput(createMemoryObject(MoodCodelet.INPUT_APPRAISAL_MEMORY, outputAppraisalMO));

        Memory hungerMoodMO = createMemoryObject(MoodCodelet.OUTPUT_MOOD_MEMORY);
        hungerMoodCodelet.addOutput(hungerMoodMO);

        insertCodelet(hungerMoodCodelet);

        AmbitionMoodCodelet ambitionMoodCodelet = new AmbitionMoodCodelet("AmbitionMood");
        ambitionMoodCodelet.addInput(createMemoryObject(MoodCodelet.INPUT_DRIVES_MEMORY, Arrays.asList(outputAmbitionDriveMO)));
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
        apCodelets.add(currentAppraisal);

        List<Codelet> mdCodelets = new ArrayList<>();
        mdCodelets.add(hungerMoodCodelet);
        mdCodelets.add(ambitionMoodCodelet);

        MotivationalSubsystemViewer motivationalSubsystemViewer =  new MotivationalSubsystemViewer(mtCodelets,
                emCodelets, new ArrayList<Codelet>(), apCodelets, mdCodelets, 50);

        motivationalSubsystemViewer.setVisible(true);
        //================================================

        // Create and Populate MindViewer
        MindView mv = new MindView("MindView");
        mv.addMO(knownApplesMO);
        mv.addMO(visionMO);
        mv.addMO(closestAppleMO);
        mv.addMO(innerSenseMO);
        mv.addMO(handsMO);
        mv.addMO(legsMO);
        mv.addMO(closestJewelMO);
        mv.addMO(knownJewelsMO);
        mv.addMO(closestObstacleMO);
        mv.addMO(outputAmbitionDriveMO);
        mv.addMO(outputAvoidDangerDriveMO);
        mv.addMO(outputBoredomDriveMO);
        mv.addMO(outputHungryDriveMO);

        mv.setCreatureInnerSense(cis);
        mv.setCreature(env.c);
        mv.setMind(this);

        mv.StartTimer();
        mv.setVisible(true);
        //=================================


        start();
    }


}
