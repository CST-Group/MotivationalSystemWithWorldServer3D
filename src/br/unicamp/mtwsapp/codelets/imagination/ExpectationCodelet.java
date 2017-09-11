package br.unicamp.mtwsapp.codelets.imagination;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.representation.owrl.AbstractObject;
import br.unicamp.cst.representation.owrl.Property;
import br.unicamp.cst.representation.owrl.QualityDimension;
import br.unicamp.cst.util.Pair;
import br.unicamp.mtwsapp.codelets.appraisal.CurrentAppraisalCodelet;
import br.unicamp.mtwsapp.codelets.episodic.EpisodicMemoryGeneratorCodelet;
import br.unicamp.mtwsapp.support.NetworkSettings;
import org.numenta.nupic.Parameters;
import org.numenta.nupic.algorithms.*;
import org.numenta.nupic.network.Inference;
import org.numenta.nupic.network.Network;
import org.numenta.nupic.network.PublisherSupplier;
import org.numenta.nupic.network.sensor.ObservableSensor;
import org.numenta.nupic.network.sensor.Sensor;
import org.numenta.nupic.network.sensor.SensorParams;
import rx.Subscriber;

import java.util.*;
import java.util.stream.Collectors;

public class ExpectationCodelet extends Codelet {

    public static final String OUTPUT_EXPECTATION_MEMORY = "OUTPUT_EXPECTATION_MEMORY";

    private MemoryObject inputEpisodicMemory;
    private MemoryObject outputImaginationMemory;

    private HashMap<Long, Pair> inputEpisodics;

    private List<NetworkSettings> networks;
    private HashMap<String, List<String>> netNamesAndFields;
    private HashMap<String, Integer> netDimensions;

    private AbstractObject hypoteticalSituation;

    private Property propNet;

    private QualityDimension qdNet;

    private Property propUseFlag;

    private QualityDimension qdUseFlag;

    private boolean init = true;

    private int time;

    private double predictedValue = 0.0;


    public ExpectationCodelet(String name, int time) {
        setName(name);

        setInputEpisodics(new HashMap<>());

        setTime(time);

        setTimeStep(time * 1000);

        setNetworks(new ArrayList<>());

        initNetworksSetting();

        setHypoteticalSituation(new AbstractObject("HypoteticalSituation"));

        setPropNet(new Property("Network"));

        setPropUseFlag(new Property("UseFlag"));

        setQdNet(new QualityDimension("Value", null));

        getPropNet().addQualityDimension(getQdNet());

        setQdUseFlag(new QualityDimension("Value", false));

        getPropUseFlag().addQualityDimension(getQdUseFlag());

        getHypoteticalSituation().addProperty(getPropNet());

        getHypoteticalSituation().addProperty(getPropUseFlag());
    }

    private void initNetworksSetting() {

        getNetworks().add(new NetworkSettings("AppraisalNetWork",
                Arrays.asList("CurrentTime", "CurrentAppraisal"),
                "int,int",
                "C,C",
                896,
                "CurrentAppraisal", true));

        /*getNetworks().add(new NetworkSettings("HungerDriveNetWork",
                Arrays.asList("CurrentTime", "HungerDrive"),
                "int,float",
                "C,C",
                1024,
                "HungerDrive", false));

        getNetworks().add(new NetworkSettings("AmbitionDriveNetWork",
                Arrays.asList("CurrentTime", "AmbitionDrive"),
                "int,float",
                "C,C",
                1024,
                "AmbitionDrive", false));*/

        /*getNetworks().add(new NetworkSettings("AvoidDriveNetWork",
                Arrays.asList("CurrentTime", "AvoidDrive"),
                "int,float",
                "C,C",
                1024,
                "AvoidDrive"));

        getNetworks().add(new NetworkSettings("RandomDriveNetWork",
                Arrays.asList("CurrentTime", "RandomDrive"),
                "int,float",
                "C,C",
                1024,
                "RandomDrive"));*/

    }

    @Override
    public void accessMemoryObjects() {

        if (getInputEpisodicMemory() == null) {
            setInputEpisodicMemory((MemoryObject) getInput(EpisodicMemoryGeneratorCodelet.OUTPUT_EPISODIC_MEMORY));
        }

        if (getOutputImaginationMemory() == null) {
            setOutputImaginationMemory((MemoryObject) getOutput(OUTPUT_EXPECTATION_MEMORY));
        }

    }

    @Override
    public void calculateActivation() {
        try {
            setActivation(0);
        } catch (CodeletActivationBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void proc() {

        if (!isInit()) {
            setInputEpisodics((HashMap<Long, Pair>) ((HashMap<Long, Pair>) getInputEpisodicMemory().getI()).clone());

            if (getInputEpisodics() != null) {

                getQdUseFlag().setValue(false);

                List<HashMap<String, Object>> afterList = new ArrayList<>();

                HashMap<String, Object> afterMap = null;

                for (Map.Entry<Long, Pair> episodic : getInputEpisodics().entrySet()) {
                    long time = episodic.getKey();
                    Pair epdc = episodic.getValue();

                    AbstractObject afterEpisodic = (AbstractObject) epdc.getSecond();
                    afterMap = convertDataToMap(afterEpisodic);

                    afterList.add(afterMap);
                }

                for (NetworkSettings networkSetting : getNetworks()) {

                    networkSetting.setInputs(convertMapToString(afterList, networkSetting.getFields()));

                    if (networkSetting.getNetwork() == null) {
                        networkSetting.setNetwork(buildHTMClassifier(networkSetting.getNetworkName(),
                                networkSetting.getInputs(),
                                networkSetting.getFields(),
                                networkSetting.getFieldTypes(),
                                networkSetting.getHeader(),
                                networkSetting.getDimension(),
                                networkSetting.getOutput(),
                                networkSetting));
                    }

                    for (int i = networkSetting.getNextIndexToTrainning(); i < networkSetting.getInputs().size(); i++) {
                        networkSetting.computeInput(networkSetting.getInputs().get(i));
                    }

                    networkSetting.setNextIndexToTrainning(networkSetting.getInputs().size());

                }

                getQdNet().setValue(getNetworks());

                getQdUseFlag().setValue(true);

                getOutputImaginationMemory().setI(getHypoteticalSituation());
            }
        } else {
            setInit(false);
        }



    }

    public MemoryObject getInputEpisodicMemory() {
        return inputEpisodicMemory;
    }

    public void setInputEpisodicMemory(MemoryObject inputEpisodicMemory) {
        this.inputEpisodicMemory = inputEpisodicMemory;
    }

    public MemoryObject getOutputImaginationMemory() {
        return outputImaginationMemory;
    }

    public void setOutputImaginationMemory(MemoryObject outputImaginationMemory) {
        this.outputImaginationMemory = outputImaginationMemory;
    }

    public List<String> convertMapToString(List<HashMap<String, Object>> objects, List<String> fields) {

        ArrayList<String> inputs = new ArrayList<>();

        for (int i = 0; i < objects.size(); i++) {
            String input = "";

            for (String field : fields) {

                Map<String, Object> mapValues = objects.get(i).entrySet()
                        .stream()
                        .filter(entry -> entry.getKey().equals(field)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                input += mapValues.get(field) + ",";

            }

            inputs.add(input.substring(0, input.length() - 1));
        }


        return inputs;
    }

    public HashMap<String, Object> convertDataToMap(AbstractObject episodic) {

        HashMap<String, Object> values = new HashMap<>();

        AbstractObject creatureAO = episodic.getCompositeList().stream().filter(x -> x.getName().equals("Creature")).findFirst().get();

        //InnerSense AbstractObject
        AbstractObject innerSenseAO = creatureAO.getCompositeList().stream().filter(x -> x.getName().equals("InnerSense")).findFirst().get();

        Property senseAO = innerSenseAO.getProperties().stream().filter(i -> i.getName().equals("Sense")).findFirst().get();

        for (QualityDimension qd : senseAO.getQualityDimensions()) {

            values.put(qd.getName(), qd.getValue());
        }
        //=========================

        //Things AbstractObject
        AbstractObject thingsAO = episodic.getCompositeList().stream().filter(x -> x.getName().equals("Things")).findFirst().get();
        AbstractObject foodsAO = thingsAO.getCompositeList().stream().filter(t -> t.getName().equals("Foods")).findFirst().get();
        AbstractObject jewelsAO = thingsAO.getCompositeList().stream().filter(t -> t.getName().equals("Jewels")).findFirst().get();
        AbstractObject bricksAO = thingsAO.getCompositeList().stream().filter(t -> t.getName().equals("Bricks")).findFirst().get();

        values.put("NumberOfJewels", jewelsAO.getCompositeList().size());
        values.put("NumberOfFoods", foodsAO.getCompositeList().size());
        values.put("NumberOfBricks", bricksAO.getCompositeList().size());
        //=====================

        //Appraisal AbstractObject
        AbstractObject appraisalAO = creatureAO.getCompositeList().stream().filter(x -> x.getName().equals("Appraisal")).findFirst().get();

        Property currentAppraisalProp = appraisalAO.getProperties().stream().filter(i -> i.getName().equals("CurrentAppraisal")).findFirst().get();

        QualityDimension qdCurrentStateEvaluation = currentAppraisalProp.getQualityDimensions().stream().filter(p -> p.getName().equals("CurrentStateEvaluation")).findFirst().get();


        if (qdCurrentStateEvaluation.getValue().equals(CurrentAppraisalCodelet.STATE_BAD)) {
            values.put("CurrentAppraisal", 0);
        } else if (qdCurrentStateEvaluation.getValue().equals(CurrentAppraisalCodelet.STATE_NORMAL)) {
            values.put("CurrentAppraisal", 1);
        } else {
            values.put("CurrentAppraisal", 2);
        }
        //========================

        //Drives AbstractObject
        AbstractObject drivesAO = creatureAO.getCompositeList().stream().filter(x -> x.getName().equals("Drives")).findFirst().get();
        values.put("Drives", drivesAO);

        for (Property property : drivesAO.getProperties()) {
            values.put(property.getName(), property.getQualityDimensions().stream().filter(p -> p.getName().equals("Activation")).findFirst().get().getValue());
        }

        //========================

        //Current Time Property
        Property currentTimeProp = episodic.getProperties().stream().filter(p -> p.getName().equals("CurrentTime")).findFirst().get();
        QualityDimension qdCurrentTime = currentTimeProp.getQualityDimensions().stream().findFirst().get();

        values.put("CurrentTime", qdCurrentTime.getValue());

        //=====================

        //Collision ON Abstraction
        AbstractObject collisionAO = creatureAO.getCompositeList().stream().filter(x -> x.getName().equals("Collision")).findFirst().get();
        Property flagProp = collisionAO.getProperties().stream().filter(p -> p.getName().equals("Flag")).findFirst().get();
        QualityDimension qdCollision = flagProp.getQualityDimensions().stream().findFirst().get();

        if (qdCollision.getValue().equals(true)) {
            values.put("Collision", 1.0d);
        } else {
            values.put("Collision", 0.0d);
        }
        //========================

        return values;

    }

    public Parameters getParametersByNetwork(String networkName, int dimension, String output) {
        Map<String, Map<String, Object>> encoding = null;

        if (networkName.equals("AppraisalNetWork")) {
            encoding = getEncodingAppraisalNetwork();
        } else if (networkName.equals("HungerDriveNetWork")) {
            encoding = getEncodingHungerDriveNetwork();
        } else if (networkName.equals("AmbitionDriveNetWork")) {
            encoding = getEncodingAmbitionDriveNetwork();
        } else if (networkName.equals("AvoidDriveNetWork")) {
            encoding = getEncodingAvoidDriveNetwork();
        } else if (networkName.equals("RandomDriveNetWork")) {
            encoding = getEncodingAvoidDriveNetwork();
        }


        return getParameters(encoding, dimension, output);

    }

    public Network buildHTMClassifier(String networkName, List<String> inputs, List<String> fields, String fieldTypes, String header, int dimension, String output, NetworkSettings net) {

        Parameters p = getParametersByNetwork(networkName, dimension, output);

        PublisherSupplier manual = PublisherSupplier.builder()
                .addHeader(String.join(",", fields))
                .addHeader(fieldTypes)
                .addHeader(header) //see SensorFlags.java for more info
                .build();

        Sensor<Object> sensor = Sensor.create(ObservableSensor::create, SensorParams.create(
                SensorParams.Keys::obs, new Object[]{"", manual}));

        for (String input : inputs) {
            manual.get().onNext(input);
        }

        // This is how easy it is to create a full running Network!
        /*Network network = Network.create("Network API Demo", p)
                .add(Network.createRegion("Region 1")
                        .add(Network.createLayer("Layer 2/3", p)
                                .alterParameter(Parameters.KEY.AUTO_CLASSIFY, Boolean.TRUE)
                                .add(Anomaly.create())
                                .add(new TemporalMemory())
                                .add(new SpatialPooler())
                                .add(sensor)));*/

        Network network = Network.create(networkName, p)
                .add(Network.createRegion("Region 1")
                        .add(Network.createLayer("Layer 2/3", p)
                                .alterParameter(Parameters.KEY.AUTO_CLASSIFY, Boolean.TRUE)
                                .add(Anomaly.create())
                                .add(new TemporalMemory()))
                        .add(Network.createLayer("Layer 4", p)
                                .add(new SpatialPooler()))
                        .add(Network.createLayer("Layer 5", p)
                                .add(sensor))
                        .connect("Layer 2/3", "Layer 4")
                        .connect("Layer 4", "Layer 5"));

        net.setManual(manual);

        /*Network network = Network.create("Network API Demo", p)
                .add(Network.createRegion("Region 1")
                        .add(Network.createLayer("Layer 2/3", p)
                                .alterParameter(Parameters.KEY.AUTO_CLASSIFY, Boolean.TRUE)
                                .add(Anomaly.create())
                                .add(new TemporalMemory()))
                        .add(Network.createLayer("Layer 4", p)
                                .add(new SpatialPooler()))
                        .connect("Layer 2/3", "Layer 4"))
                .add(Network.createRegion("Region 2")
                        .add(Network.createLayer("Layer 2/3", p)
                                .alterParameter(Parameters.KEY.AUTO_CLASSIFY, Boolean.TRUE)
                                .add(Anomaly.create())
                                .add(new TemporalMemory())
                                .add(new SpatialPooler()))
                        .add(Network.createLayer("Layer 4", p)
                                .add(sensor))
                        .connect("Layer 2/3", "Layer 4"))
                .connect("Region 1", "Region 2");*/


        network.observe().subscribe(getSubscriber(output, net));



        network.start();

        return network;

    }

    public Subscriber<Inference> getSubscriber(String output, NetworkSettings networkSettings) {

        return new Subscriber<Inference>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Inference i) {
                try {
                    double newPrediction;
                    if (null != i.getClassification(output).getMostProbableValue(1)) {
                        newPrediction = (Double) i.getClassification(output).getMostProbableValue(1);
                    } else {
                        newPrediction = predictedValue;
                    }

                    if (i.getRecordNum() > 0) {
                        double actual = (Double) i.getClassifierInput()
                                .get(output).get("inputValue");
                        double error = Math.abs(predictedValue - actual);

                        networkSettings.setLastInputValue(actual);
                        networkSettings.setLastPredictedValue(newPrediction);
                        networkSettings.setTotalError(networkSettings.getTotalError() + error);

                        if (networkSettings.isShowLog()) {
                            StringBuilder sb = new StringBuilder()
                                    .append("Number:" + i.getRecordNum()).append(", ")
                                    //.append("classifier input=")
                                    .append("Actual:" + String.format("%3.2f", actual)).append(", ")
                                    //.append("prediction= ")
                                    .append("Predicted Value:" + String.format("%3.2f", predictedValue)).append(", ")
                                    .append("Error:" + String.format("%3.2f", error)).append(", ")
                                    //.append("anomaly score=")
                                    .append("Anomaly Score:" + i.getAnomalyScore()).append(", ")
                                    .append("Mean Error:" + networkSettings.getMeanError());

                            System.out.println(sb);
                        }



                    } else {

                    }
                    predictedValue = newPrediction;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public Map<String, Map<String, Object>> getEncodingAppraisalNetwork() {
        Map<String, Map<String, Object>> fieldEncodings = setupMap(
                null,
                512,
                39,
                0, 900, 0, 1, false, null, true,
                "CurrentTime", "int", "ScalarEncoder");

        fieldEncodings = setupMap(
                fieldEncodings,
                384,
                127,
                0, 2, 0, 1, false, null, true,
                "CurrentAppraisal", "int", "ScalarEncoder");

        return fieldEncodings;
    }

    public Map<String, Map<String, Object>> getEncodingHungerDriveNetwork() {
        Map<String, Map<String, Object>> fieldEncodings = setupMap(
                null,
                512,
                39,
                0, 900, 0, 1, false, null, true,
                "CurrentTime", "int", "ScalarEncoder");

        fieldEncodings = setupMap(
                fieldEncodings,
                512,
                39,
                0, 1, 0, 1, false, null, true,
                "HungerDrive", "float", "ScalarEncoder");

        return fieldEncodings;
    }

    public Map<String, Map<String, Object>> getEncodingAmbitionDriveNetwork() {

        Map<String, Map<String, Object>> fieldEncodings = setupMap(
                null,
                512,
                39,
                0, 900, 0, 1, false, null, true,
                "CurrentTime", "int", "ScalarEncoder");

        fieldEncodings = setupMap(
                fieldEncodings,
                512,
                39,
                0, 1, 0, 1, false, null, true,
                "AmbitionDrive", "float", "ScalarEncoder");

        return fieldEncodings;
    }

    public Map<String, Map<String, Object>> getEncodingAvoidDriveNetwork() {
        Map<String, Map<String, Object>> fieldEncodings = setupMap(
                null,
                512,
                39,
                0, 900, 0, 1, false, null, true,
                "CurrentTime", "int", "ScalarEncoder");

        fieldEncodings = setupMap(
                fieldEncodings,
                512,
                39,
                0, 1, 0, 1, false, null, true,
                "AvoidDrive", "float", "ScalarEncoder");

        return fieldEncodings;
    }

    public Map<String, Map<String, Object>> getEncodingRandomDriveNetwork() {
        Map<String, Map<String, Object>> fieldEncodings = setupMap(
                null,
                512,
                39,
                0, 900, 0, 1, false, null, true,
                "CurrentTime", "int", "ScalarEncoder");

        fieldEncodings = setupMap(
                fieldEncodings,
                512,
                39,
                0, 1, 0, 1, false, null, true,
                "RandomDrive", "float", "ScalarEncoder");

        return fieldEncodings;
    }


    public static Map<String, Map<String, Object>> setupMap(
            Map<String, Map<String, Object>> map,
            int n, int w, double min, double max, double radius, double resolution, Boolean periodic,
            Boolean clip, Boolean forced, String fieldName, String fieldType, String encoderType) {

        if (map == null) {
            map = new HashMap<String, Map<String, Object>>();
        }
        Map<String, Object> inner = null;
        if ((inner = map.get(fieldName)) == null) {
            map.put(fieldName, inner = new HashMap<String, Object>());
        }

        inner.put("n", n);
        inner.put("w", w);
        inner.put("minVal", min);
        inner.put("maxVal", max);
        inner.put("radius", radius);
        inner.put("resolution", resolution);

        if (periodic != null) inner.put("periodic", periodic);
        if (clip != null) inner.put("clipInput", clip);
        if (forced != null) inner.put("forced", forced);
        if (fieldName != null) inner.put("fieldName", fieldName);
        if (fieldType != null) inner.put("fieldType", fieldType);
        if (encoderType != null) inner.put("encoderType", encoderType);

        return map;
    }


    public static Parameters getParameters(Map<String, Map<String, Object>> encoding, int dimension, String output) {
        Parameters parameters = Parameters.getAllDefaultParameters();
        /*parameters.set(Parameters.KEY.INPUT_DIMENSIONS, new int[]{dimension});
        parameters.set(Parameters.KEY.COLUMN_DIMENSIONS, new int[]{32});
        parameters.set(Parameters.KEY.CELLS_PER_COLUMN, 10);

        //SpatialPooler specific
        parameters.set(Parameters.KEY.POTENTIAL_RADIUS, 3);//3
        parameters.set(Parameters.KEY.POTENTIAL_PCT, 0.5);//0.5
        parameters.set(Parameters.KEY.GLOBAL_INHIBITION, false);
        parameters.set(Parameters.KEY.LOCAL_AREA_DENSITY, -1.0);
        parameters.set(Parameters.KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, 5.0);
        parameters.set(Parameters.KEY.STIMULUS_THRESHOLD, 1.0);
        parameters.set(Parameters.KEY.SYN_PERM_INACTIVE_DEC, 0.0005);
        parameters.set(Parameters.KEY.SYN_PERM_ACTIVE_INC, 0.0015);
        parameters.set(Parameters.KEY.SYN_PERM_TRIM_THRESHOLD, 0.05);
        parameters.set(Parameters.KEY.SYN_PERM_CONNECTED, 0.1);
        parameters.set(Parameters.KEY.MIN_PCT_OVERLAP_DUTY_CYCLES, 0.1);
        parameters.set(Parameters.KEY.MIN_PCT_ACTIVE_DUTY_CYCLES, 0.1);
        parameters.set(Parameters.KEY.DUTY_CYCLE_PERIOD, 10);
        parameters.set(Parameters.KEY.MAX_BOOST, 10.0);
        parameters.set(Parameters.KEY.LEARN, true);

        parameters.set(Parameters.KEY.SEED, 42);
        parameters.set(Parameters.KEY.INFERRED_FIELDS, getInferredFieldsMap(output, SDRClassifier.class));

        //CLAClassifier.class

        //Temporal Memory specific
        parameters.set(Parameters.KEY.INITIAL_PERMANENCE, 0.2);
        parameters.set(Parameters.KEY.CONNECTED_PERMANENCE, 0.4);
        parameters.set(Parameters.KEY.MIN_THRESHOLD, 3);
        parameters.set(Parameters.KEY.MAX_NEW_SYNAPSE_COUNT, 6);
        parameters.set(Parameters.KEY.PERMANENCE_INCREMENT, 0.05);//0.05
        parameters.set(Parameters.KEY.PERMANENCE_DECREMENT, 0.05);//0.05
        parameters.set(Parameters.KEY.ACTIVATION_THRESHOLD, 4);
        parameters.set(Parameters.KEY.FIELD_ENCODING_MAP, encoding);

        parameters.set(Parameters.KEY.RANDOM, new FastRandom());*/

        parameters.set(Parameters.KEY.GLOBAL_INHIBITION, true);
        parameters.set(Parameters.KEY.COLUMN_DIMENSIONS, new int[]{dimension});
        parameters.set(Parameters.KEY.CELLS_PER_COLUMN, 64);
        parameters.set(Parameters.KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, 10.0);
        parameters.set(Parameters.KEY.POTENTIAL_PCT, 0.8);
        parameters.set(Parameters.KEY.SYN_PERM_CONNECTED, 0.1);
        parameters.set(Parameters.KEY.SYN_PERM_ACTIVE_INC, 0.0001);
        parameters.set(Parameters.KEY.SYN_PERM_INACTIVE_DEC, 0.0005);
        parameters.set(Parameters.KEY.MAX_BOOST, 1.0);
        parameters.set(Parameters.KEY.INFERRED_FIELDS, getInferredFieldsMap(output, SDRClassifier.class));

        parameters.set(Parameters.KEY.MAX_NEW_SYNAPSE_COUNT, 20);
        parameters.set(Parameters.KEY.INITIAL_PERMANENCE, 0.21);
        parameters.set(Parameters.KEY.PERMANENCE_INCREMENT, 0.1);
        parameters.set(Parameters.KEY.PERMANENCE_DECREMENT, 0.1);
        parameters.set(Parameters.KEY.MIN_THRESHOLD, 9);
        parameters.set(Parameters.KEY.ACTIVATION_THRESHOLD, 12);

        parameters.set(Parameters.KEY.CLIP_INPUT, true);
        parameters.set(Parameters.KEY.FIELD_ENCODING_MAP, encoding);

        return parameters;
    }

    public static Map<String, Class<? extends Classifier>> getInferredFieldsMap(String field, Class<? extends Classifier> classifier) {
        Map<String, Class<? extends Classifier>> inferredFieldsMap = new HashMap<>();
        inferredFieldsMap.put(field, classifier);
        return inferredFieldsMap;
    }

    public HashMap<Long, Pair> getInputEpisodics() {
        return inputEpisodics;
    }

    public void setInputEpisodics(HashMap<Long, Pair> inputEpisodics) {
        this.inputEpisodics = inputEpisodics;
    }

    public HashMap<String, List<String>> getNetNamesAndFields() {
        return netNamesAndFields;
    }

    public void setNetNamesAndFields(HashMap<String, List<String>> netNamesAndFields) {
        this.netNamesAndFields = netNamesAndFields;
    }

    public HashMap<String, Integer> getNetDimensions() {
        return netDimensions;
    }

    public void setNetDimensions(HashMap<String, Integer> netDimensions) {
        this.netDimensions = netDimensions;
    }

    public List<NetworkSettings> getNetworks() {
        return networks;
    }

    public void setNetworks(List<NetworkSettings> networks) {
        this.networks = networks;
    }

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public AbstractObject getHypoteticalSituation() {
        return hypoteticalSituation;
    }

    public void setHypoteticalSituation(AbstractObject hypoteticalSituation) {
        this.hypoteticalSituation = hypoteticalSituation;
    }

    private Property getPropNet() {
        return propNet;
    }

    private void setPropNet(Property propNet) {
        this.propNet = propNet;
    }

    private QualityDimension getQdNet() {
        return qdNet;
    }

    private void setQdNet(QualityDimension qdNet) {
        this.qdNet = qdNet;
    }

    private Property getPropUseFlag() {
        return propUseFlag;
    }

    private void setPropUseFlag(Property propUseFlag) {
        this.propUseFlag = propUseFlag;
    }

    private QualityDimension getQdUseFlag() {
        return qdUseFlag;
    }

    private void setQdUseFlag(QualityDimension qdUseFlag) {
        this.qdUseFlag = qdUseFlag;
    }
}
