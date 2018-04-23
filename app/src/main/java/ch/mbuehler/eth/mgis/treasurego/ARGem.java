package ch.mbuehler.eth.mgis.treasurego;

import android.location.Location;

/**
 * Created by vanagnos on 03.04.2017. Updated by goebelf on 18.04.2018.
 * A simple class to represent a point in the 3D space that we will use
 * for rendering in the screen
 */

public class ARGem {
    Location location;
    String name;
    double x;
    double y;

    /**
     * An ARGem represent a point in the 3D space for the AROverlayView.
     *
     * @param name     the name of the ARGem
     */
    public ARGem(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    /**
     * Returns the location of the ARGem
     *
     * @return the ARGem's location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Returns the Name of the ARGem
     *
     * @return the ARGem's name
     */
    public String getName() {
        return name;
    }

    public double euclideanDistanceTo(double x, double y){
        return Math.sqrt(Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2));
    }

    public String toString(){
        return String.format("ARGem(%f,%f)",x,y);
    }
}
