package ch.mbuehler.eth.mgis.treasurego;

import android.location.Location;

/**
 * Simple class representing an ARGem that can be rendered on the screen.
 */
public class ARGem {
    private Location location;
    private String name;
    private int imageId;
    private double x;
    private double y;

    /**
     * An ARGem represent a point in the 3D space for the AROverlayView.
     *
     * @param name     the name of the ARGem
     */
    ARGem(String name, Location location, int imageId) {
        this.name = name;
        this.location = location;
        this.imageId = imageId;
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

    double euclideanDistanceTo(double x, double y){
        return Math.sqrt(Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2));
    }

    public String toString(){
        return String.format("ARGem(%f,%f)",x,y);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getImageId() {
        return imageId;
    }
}
