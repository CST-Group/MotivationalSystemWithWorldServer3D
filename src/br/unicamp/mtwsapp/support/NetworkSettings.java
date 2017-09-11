package br.unicamp.mtwsapp.support;

import org.numenta.nupic.network.Layer;
import org.numenta.nupic.network.Network;
import org.numenta.nupic.network.PublisherSupplier;

import java.util.ArrayList;
import java.util.List;

public class NetworkSettings {

    private String networkName;
    private Network network;
    private List<String> inputs;
    private List<String> fields;
    private String fieldTypes;
    private String header;
    private String output;
    private int dimension;
    private int nextIndexToTrainning = 0;
    private double totalError = 0;
    private PublisherSupplier manual;

    private Object lastInputValue;
    private Object lastPredictedValue;

    private boolean showLog = false;

    public NetworkSettings(String networkName, List<String> fields, String fieldTypes, String header, int dimension, String output, boolean showLog){
        this.setNetworkName(networkName);
        this.setFields(fields);
        this.setDimension(dimension);
        this.setFieldTypes(fieldTypes);
        this.setHeader(header);
        this.setOutput(output);
        this.setInputs(new ArrayList<>());
        this.setShowLog(showLog);
    }


    public double[] getMostProbableValue(int count){

        Layer<?> layer = getNetwork().lookup("Region 1").lookup("Layer 2/3");

        double[] output = new double[count];

        for (int i = 0; i < count ; i++) {
            output[i] = (double) layer.getInference().getClassification("CurrentAppraisal").getMostProbableValue(1);
        }

        return output;
    }

    public void computeInput(String input){
        getManual().get().onNext(input);
    }

    public double getMeanError(){
        return getTotalError() / getInputs().size();
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public String getFieldTypes() {
        return fieldTypes;
    }

    public void setFieldTypes(String fieldTypes) {
        this.fieldTypes = fieldTypes;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public List<String> getInputs() {
        return inputs;
    }

    public void setInputs(List<String> inputs) {
        this.inputs = inputs;
    }

    public int getNextIndexToTrainning() {
        return nextIndexToTrainning;
    }

    public void setNextIndexToTrainning(int nextIndexToTrainning) {
        this.nextIndexToTrainning = nextIndexToTrainning;
    }

    public double getTotalError() {
        return totalError;
    }

    public void setTotalError(double totalError) {
        this.totalError = totalError;
    }


    public PublisherSupplier getManual() {
        return manual;
    }

    public void setManual(PublisherSupplier manual) {
        this.manual = manual;
    }

    public Object getLastInputValue() {
        return lastInputValue;
    }

    public void setLastInputValue(Object lastInputValue) {
        this.lastInputValue = lastInputValue;
    }

    public Object getLastPredictedValue() {
        return lastPredictedValue;
    }

    public void setLastPredictedValue(Object lastPredictedValue) {
        this.lastPredictedValue = lastPredictedValue;
    }

    public boolean isShowLog() {
        return showLog;
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }
}
