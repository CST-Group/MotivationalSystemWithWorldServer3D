package democst;

import br.unicamp.cst.behavior.subsumption.SubsumptionAction;
import br.unicamp.cst.behavior.subsumption.SubsumptionArchitecture;
import br.unicamp.cst.behavior.subsumption.SubsumptionBehaviourLayer;
import br.unicamp.cst.core.entities.*;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.motivational.MotivationalCodelet;
import br.unicamp.cst.motivational.MotivationalSubsystemViewer;
import br.unicamp.cst.motivational.MotivationalViewer;
import codelets.behaviors.AvoidColisionObstacle;
import codelets.behaviors.EatClosestApple;
import codelets.behaviors.Forage;
import codelets.behaviors.GetClosestJewel;
import codelets.behaviors.GoToApple;
import codelets.behaviors.GoToJewel;
import codelets.motivational.AvoidDangerMotivationalCodelet;
import codelets.motivational.CuriosityMotivationalCodelet;
import codelets.motivational.HungryMotivationalCodelet;
import codelets.motor.HandsActionCodelet;
import codelets.motor.LegsActionCodelet;
import codelets.perception.AppleDetector;
import codelets.perception.ClosestAppleDetector;
import codelets.perception.ClosestJewelDetector;
import codelets.perception.ClosestObstacleDetector;
import codelets.perception.JewelDetector;
import codelets.sensors.InnerSense;
import codelets.sensors.Vision;
import com.sun.corba.se.impl.orbutil.closure.Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import memory.CreatureInnerSense;
import support.MindView;
import ws3dproxy.model.Thing;
import ws3dproxy.util.Constants;

/**
 * @author Du
 */
public class AgentMind extends Mind {

    private static int creatureBasicSpeed = 3;


    public AgentMind(Environment env) {
        super();

        // Declare Motivational Codelets
        Codelet hungryMotivationalCodelet = null;
        Codelet avoidDangerMotivationalCodelet = null;
        Codelet curiosityMotivationalCodelet = null;


        // Declare Memory Objects
        MemoryObject legsMO;
        MemoryObject handsMO;
        MemoryObject visionMO;
        MemoryObject innerSenseMO;
        MemoryObject closestAppleMO;
        MemoryObject knownApplesMO;
        MemoryObject closestJewelMO;
        MemoryObject knownJewelsMO;
        MemoryObject leafletMO;
        MemoryObject closestObstacleMO;
        int reachDistance = 65;
        int brickDistance = 60;

        SubsumptionArchitecture subsumptionArchitecture = new SubsumptionArchitecture(this);

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

        mv.StartTimer();
        mv.setVisible(true);


        // Create Sensor Codelets
        Codelet vision = new Vision(env.c);
        vision.addOutput(visionMO);
        insertCodelet(vision); //Creates a vision sensor

        Codelet innerSense = new InnerSense(env.c);
        innerSense.addOutput(innerSenseMO);
        insertCodelet(innerSense); //A sensor for the inner state of the creature
        //=======================

        // Create Motivational Codelets
        try {
            hungryMotivationalCodelet = new HungryMotivationalCodelet("HungryDrive", 0, 0.4, 0.7);
            avoidDangerMotivationalCodelet = new AvoidDangerMotivationalCodelet("AvoidDangerDrive", 0, 0.3, 0.8);
            curiosityMotivationalCodelet = new CuriosityMotivationalCodelet("CuriosityDrive", 0, 0.2, 0.9);
        } catch (CodeletActivationBoundsException e) {
            e.printStackTrace();
        }
        //=============================



        // Create Perception Codelets
        Codelet ad = new AppleDetector(env.c);
        ad.addInput(visionMO);
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


        //Hungry Motivational Codelet
        List<Memory> hungrySensorsMemory = new ArrayList<>();
        hungrySensorsMemory.add(innerSenseMO);
        hungrySensorsMemory.add(knownApplesMO);

        Memory inputHungrySensorsMO = createMemoryObject(MotivationalCodelet.INPUT_SENSORS_MEMORY);
        inputHungrySensorsMO.setI(hungrySensorsMemory);

        hungryMotivationalCodelet.addInput(inputHungrySensorsMO);

        Memory inputHungryDrivesMO = createMemoryObject(MotivationalCodelet.INPUT_DRIVES_MEMORY);
        inputHungryDrivesMO.setI(new HashMap<Memory, Double>());

        hungryMotivationalCodelet.addInput(inputHungryDrivesMO);

        Memory outputHungryDriveMO = createMemoryObject(MotivationalCodelet.OUTPUT_DRIVE_MEMORY);

        hungryMotivationalCodelet.addOutput(outputHungryDriveMO);

        insertCodelet(hungryMotivationalCodelet);
        //=============================


        //Curiosity Motivational Codelet
        List<Memory> curiositySensorsMemory = new ArrayList<>();
        curiositySensorsMemory.add(innerSenseMO);
        curiositySensorsMemory.add(knownJewelsMO);

        Memory inputCuriositySensorsMO = createMemoryObject(MotivationalCodelet.INPUT_SENSORS_MEMORY);
        inputCuriositySensorsMO.setI(curiositySensorsMemory);
        curiosityMotivationalCodelet.addInput(inputCuriositySensorsMO);

        Memory inputCuriosityDrivesMO = createMemoryObject(MotivationalCodelet.INPUT_DRIVES_MEMORY);
        inputCuriosityDrivesMO.setI(new HashMap<Memory, Double>());

        curiosityMotivationalCodelet.addInput(inputCuriosityDrivesMO);

        Memory outputCuriosityDriveMO = createMemoryObject(MotivationalCodelet.OUTPUT_DRIVE_MEMORY);

        curiosityMotivationalCodelet.addOutput(outputCuriosityDriveMO);

        insertCodelet(curiosityMotivationalCodelet);
        //=================================

        //Avoid Danger Motivational Codelet
        List<Memory> avoidDangerSensorsMemory = new ArrayList<>();
        avoidDangerSensorsMemory.add(closestObstacleMO);

        Memory inputAvoidDangerSensorsMO = createMemoryObject(MotivationalCodelet.INPUT_SENSORS_MEMORY);
        inputAvoidDangerSensorsMO.setI(avoidDangerSensorsMemory);
        avoidDangerMotivationalCodelet.addInput(inputAvoidDangerSensorsMO);

        Memory inputAvoidDangerDrivesMO = createMemoryObject(MotivationalCodelet.INPUT_DRIVES_MEMORY);
        inputAvoidDangerDrivesMO.setI(new HashMap<Memory, Double>());

        avoidDangerMotivationalCodelet.addInput(inputAvoidDangerDrivesMO);

        Memory outputAvoidDangerDriveMO = createMemoryObject(MotivationalCodelet.OUTPUT_DRIVE_MEMORY);

        avoidDangerMotivationalCodelet.addOutput(outputAvoidDangerDriveMO);

        insertCodelet(avoidDangerMotivationalCodelet);
        //=================================


        // Create Behavior Codelets
        Codelet goToClosestApple = new GoToApple(creatureBasicSpeed);
        MemoryObject legsGoAppleMO = createMemoryObject("LEGS_GO_APPLE");
        goToClosestApple.addInput(outputHungryDriveMO);
        goToClosestApple.addInput(knownApplesMO);
        goToClosestApple.addOutput(legsGoAppleMO);
        insertCodelet(goToClosestApple);

        Codelet eatApple = new EatClosestApple(reachDistance);
        MemoryObject handsEatAppleMO = createMemoryObject("HANDS_EAT_APPLE");
        eatApple.addInput(outputHungryDriveMO);
        eatApple.addInput(closestAppleMO);
        eatApple.addInput(innerSenseMO);
        eatApple.addOutput(handsEatAppleMO);
        insertCodelet(eatApple);

        Codelet goToClosestJewel = new GoToJewel(creatureBasicSpeed);
        MemoryObject legsGoJewelMO = createMemoryObject("LEGS_GO_JEWEL");
        goToClosestJewel.addInput(outputCuriosityDriveMO);
        goToClosestJewel.addInput(knownJewelsMO);
        goToClosestJewel.addInput(innerSenseMO);
        goToClosestJewel.addOutput(legsGoJewelMO);
        insertCodelet(goToClosestJewel);

        Codelet getJewel = new GetClosestJewel(reachDistance);
        MemoryObject handsGetJewelMO = createMemoryObject("HANDS_GET_JEWEL");
        getJewel.addInput(outputCuriosityDriveMO);
        getJewel.addInput(closestJewelMO);
        getJewel.addInput(innerSenseMO);
        getJewel.addOutput(handsGetJewelMO);
        getJewel.addOutput(knownJewelsMO);
        insertCodelet(getJewel);

        Codelet avoidColisionObstacle = new AvoidColisionObstacle();
        MemoryObject legsAvoidColisionMO = createMemoryObject("LEGS_AVOID_DANGER");
        avoidColisionObstacle.addInput(outputAvoidDangerDriveMO);
        avoidColisionObstacle.addInput(closestObstacleMO);
        avoidColisionObstacle.addInput(innerSenseMO);
        avoidColisionObstacle.addOutput(legsAvoidColisionMO);
        insertCodelet(avoidColisionObstacle);

        /*Codelet forageLegs = new Forage();
        MemoryObject legsForageMO = createMemoryObject("LEGS_FORAGE");
        forageLegs.addInput(knownApplesMO);
        forageLegs.addInput(knownJewelsMO);
        forageLegs.addOutput(legsForageMO);
        insertCodelet(forageLegs);*/
        //=================================

        // Create Actuator Codelets
        Codelet legs = new LegsActionCodelet(env.c);
        MemoryContainer legsBehaviorMC = createMemoryContainer("BEHAVIORS_MC");
        legsBehaviorMC.add(legsGoAppleMO);
        legsBehaviorMC.add(legsGoJewelMO);
        legsBehaviorMC.add(legsAvoidColisionMO);
        //legsBehaviorMC.add(legsForageMO);
        legs.addInput(legsBehaviorMC);
        insertCodelet(legs);

        Codelet hands = new HandsActionCodelet(env.c);
        MemoryContainer handsBehaviorMC = createMemoryContainer("BEHAVIORS_MC");
        handsBehaviorMC.add(handsEatAppleMO);
        handsBehaviorMC.add(handsGetJewelMO);
        hands.addInput(handsBehaviorMC);
        insertCodelet(hands);

        List<Codelet> codelets = new ArrayList<>();
        codelets.add(hungryMotivationalCodelet);
        codelets.add(avoidDangerMotivationalCodelet);
        codelets.add(curiosityMotivationalCodelet);
        //codelets.add(forageLegs);

        //MotivationalViewer motivationalViewer = new MotivationalViewer(codelets, 500, "Motivational System", "Drives");
        //motivationalViewer.start();

        List<Codelet> mtCodelets = new ArrayList<>();
        mtCodelets.add(avoidDangerMotivationalCodelet);
        mtCodelets.add(curiosityMotivationalCodelet);
        mtCodelets.add(hungryMotivationalCodelet);

        MotivationalSubsystemViewer motivationalSubsystemViewer =  new MotivationalSubsystemViewer(mtCodelets,
                new ArrayList<>(), new ArrayList<>(),new ArrayList<>(), new ArrayList<>(), 500);

        motivationalSubsystemViewer.setVisible(true);


        //=========================

        /*SubsumptionBehaviourLayer layer = new SubsumptionBehaviourLayer();


        // Create Behavior Codelets
        SubsumptionAction goToClosestApple = new GoToApple(creatureBasicSpeed, reachDistance, env.c, subsumptionArchitecture);
        goToClosestApple.addInput(knownJewelsMO);
        goToClosestApple.addInput(knownApplesMO);
        goToClosestApple.addInput(innerSenseMO);

        goToClosestApple.addOutput(legsMO);


        SubsumptionAction eatApple = new EatClosestApple(reachDistance, env.c, subsumptionArchitecture);
        eatApple.addInput(closestAppleMO);
        eatApple.addInput(closestJewelMO);
        eatApple.addInput(innerSenseMO);
        eatApple.addOutput(handsMO);
        eatApple.addOutput(knownApplesMO);


        // Create Behavior Codelets
        SubsumptionAction goToClosestJewel = new GoToJewel(creatureBasicSpeed, reachDistance, env.c, subsumptionArchitecture);
        goToClosestJewel.addInput(knownJewelsMO);
        goToClosestJewel.addInput(innerSenseMO);

        goToClosestJewel.addOutput(legsMO);


        SubsumptionAction getJewel = new GetClosestJewel(reachDistance, env.c, subsumptionArchitecture);
        getJewel.addInput(closestJewelMO);
        getJewel.addInput(innerSenseMO);
        getJewel.addOutput(handsMO);
        getJewel.addOutput(knownJewelsMO);

        SubsumptionAction avoidColisionObstacle = new AvoidColisionObstacle(subsumptionArchitecture, brickDistance);
        avoidColisionObstacle.addInput(closestObstacleMO);
        avoidColisionObstacle.addInput(innerSenseMO);
        avoidColisionObstacle.addOutput(legsMO);


        SubsumptionAction forageLegs = new Forage(subsumptionArchitecture);
        forageLegs.addInput(knownApplesMO);
        forageLegs.addInput(knownJewelsMO);
        forageLegs.addOutput(legsMO);

        layer.addAction(forageLegs);
        layer.addAction(goToClosestJewel);
        layer.addAction(getJewel);
        layer.addAction(eatApple);
        layer.addAction(avoidColisionObstacle);

        subsumptionArchitecture.addSuppressedAction(forageLegs, goToClosestJewel);
        subsumptionArchitecture.addSuppressedAction(forageLegs, avoidColisionObstacle);
        subsumptionArchitecture.addSuppressedAction(avoidColisionObstacle, goToClosestJewel);
        subsumptionArchitecture.addSuppressedAction(avoidColisionObstacle, goToClosestApple);
        subsumptionArchitecture.addSuppressedAction(goToClosestApple, goToClosestJewel);


        subsumptionArchitecture.addLayer(layer)*/;


        // Start Cognitive Cycle
        start();
    }

}
