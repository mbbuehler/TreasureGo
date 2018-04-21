package ch.mbuehler.eth.mgis.treasurego;

import android.location.Location;

/**
 * Created by vanagnos on 03.04.2017. Updated by goebelf on 18.04.2018.
 * A simple class to represent a point in the 3D space that we will use
 * for rendering in the screen
 */

public class ARPoint {
    Location location;
    String name;
    double x;
    double y;

    /**
     * An ARPoint represent a point in the 3D space for the AROverlayView.
     *
     * @param name     the name of the ARPoint
     * @param lat      latitude of the ARPoint
     * @param lon      longitude of the ARPoint
     * @param altitude altitude of the ARPoint
     */
    public ARPoint(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    /**
     * Returns the location of the ARPoint
     *
     * @return the ARPoint's location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Returns the Name of the ARPoint
     *
     * @return the ARPoint's name
     */
    public String getName() {
        return name;
    }

    public double euclideanDistanceTo(double x, double y){
        return Math.sqrt(Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2));
    }

    public String toString(){
        return String.format("ARPoint(%f,%f)",x,y);
    }
}
