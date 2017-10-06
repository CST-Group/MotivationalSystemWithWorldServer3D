package br.unicamp.mtwsapp.memory;

import java.awt.Polygon;
import java.util.HashMap;
import java.util.List;

import ws3dproxy.model.Leaflet;
import ws3dproxy.model.Thing;
import ws3dproxy.model.WorldPoint;

/**
 *
 * @author Du
 */
public class CreatureInnerSense {
    private WorldPoint position;
    private double pitch;
    private double fuel;
    private Polygon fov;
    private List<Leaflet> leafletList;
    private double score;
    private double leafletCompleteRate;
    private HashMap<String,Double> diffJewels;
    private List<Thing> thingsInWorld;
    
    public String toString() {
        if (getPosition() != null)
            return("Position: ["+(int) getPosition().getX()+","+(int) getPosition().getY()+"] Pitch: ["+ getPitch() +"] Fuel: ["+ getFuel()+"] Leaflet Complete Rate: ["+ getLeafletCompleteRate() +"] Score: ["+getScore()+"]");
        else 
            return("Position: [null,null] "+" Pitch: ["+ getPitch() +"] Fuel: ["+ getFuel()+"]");


    }

    public WorldPoint getPosition() {
        return position;
    }

    public void setPosition(WorldPoint position) {
        this.position = position;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public double getFuel() {
        return fuel;
    }

    public void setFuel(double fuel) {
        this.fuel = fuel;
    }

    public Polygon getFov() {
        return fov;
    }

    public void setFov(Polygon fov) {
        this.fov = fov;
    }

    public List<Leaflet> getLeafletList() {
        return leafletList;
    }

    public void setLeafletList(List<Leaflet> leafletList) {
        this.leafletList = leafletList;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getLeafletCompleteRate() {
        return leafletCompleteRate;
    }

    public void setLeafletCompleteRate(double leafletCompleteRate) {
        this.leafletCompleteRate = leafletCompleteRate;
    }

    public void setDiffJewels(HashMap<String,Double> diffJewels) {
        this.diffJewels = diffJewels;
    }

    public HashMap<String, Double> getDiffJewels() {
        return diffJewels;
    }

    public List<Thing> getThingsInWorld() {
        return thingsInWorld;
    }

    public void setThingsInWorld(List<Thing> thingsInWorld) {
        this.thingsInWorld = thingsInWorld;
    }
}

