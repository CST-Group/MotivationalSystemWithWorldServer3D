package memory;

import java.awt.Polygon;
import java.util.List;

import ws3dproxy.model.Leaflet;
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
    
    public String toString() {
        if (getPosition() != null)
            return("Position: ["+(int) getPosition().getX()+","+(int) getPosition().getY()+"] Pitch: ["+ getPitch() +"] Fuel: ["+ getFuel()+"]");
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
}

