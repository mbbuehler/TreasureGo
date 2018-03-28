package ch.mbuehler.eth.mgis.treasurego;

import android.location.Location;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by marcello on 18/03/18.
 */

public class LocationTracker {

    private ArrayList<Location> locations = new ArrayList<>();

    private long millisecondsStart;
    private int nSamples = 0;
    private double distance = 0;

    private final int N_SMOOTHING_LOCATIONS = 5;

    public LocationTracker() {
        this.millisecondsStart = System.currentTimeMillis();
    }

    public void addSample(Location location) {
        this.distance = this.distance + this.calculateDistanceToLastSample(location);

        this.locations.add(location);
        this.nSamples += 1;
    }

    public int getNSamples() {
        return this.nSamples;
    }

    public void addSamples(ArrayList<Location> locations) {
        for (Location location : locations) {
            this.addSample(location);
        }
    }

    /**
     * @param newLocation Location
     * @return double Distance to last sample stored in this.locations in meters
     */
    private double calculateDistanceToLastSample(Location newLocation) {
        double distanceToLast = 0;
        if (locations.size() == 0) {
            distanceToLast = 0;
        } else {
            Location lastLocation = this.locations.get(this.locations.size() - 1);
            distanceToLast = newLocation.distanceTo(lastLocation);
        }
        return distanceToLast;
    }

    /**
     * @return average speed in meters / second
     */
    public double getAverageSpeed() {
        long currentMilliseconds = System.currentTimeMillis();
        long seconds = (currentMilliseconds - this.millisecondsStart) / 1000;
        return this.distance / seconds;
    }

    public double calculateSmoothedDistanceTo(Location targetLocation) {
        // Number of locations we have saved
        int nLocations = this.locations.size();
        double median = 999999999;
        if (nLocations > 0) {
            ArrayList<Double> lastNDistances = new ArrayList<>();
            int takeFromIndex = Math.max(0, nLocations - N_SMOOTHING_LOCATIONS);
            for (int i = takeFromIndex; i < nLocations; ++i) {
                double dist = Float.valueOf(this.locations.get(i).distanceTo(targetLocation)).doubleValue();
                lastNDistances.add(dist);
            }
            median = getMedian(lastNDistances);
        }
        return median;

    }

    public double getMedian(ArrayList arrayList) {
        // https://stackoverflow.com/questions/41117879/problems-finding-median-of-arraylist
        Collections.sort(arrayList);
        int middle = arrayList.size() / 2;
        middle = middle % 2 == 0 ? middle - 1 : middle;
        // If we only have one entry, middle will be -1 here.
        middle = Math.max(middle, 0);
        return (double) arrayList.get(middle);
    }
}
