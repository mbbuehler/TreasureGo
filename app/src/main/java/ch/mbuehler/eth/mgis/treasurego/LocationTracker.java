package ch.mbuehler.eth.mgis.treasurego;

import android.location.Location;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Keeps track of the location measurements during a Quest.
 */
class LocationTracker {

    /**
     * List of locations that we keep track of
     */
    private ArrayList<Location> locations = new ArrayList<>();

    /**
     * Milliseconds since the start of tracking. This variable is initialized in the constructor
     */
    private long millisecondsStart;
    /**
     * Accumulated distance. Read this for fast access.
     */
    private double distance = 0;

    /**
     * Number of samples used to calculate the smoothed distance to a point. This helps avoiding
     * "jump" due to noisy measurements.
     * Setting this variable to 1 calculates a non-smoothed distance
     * (i.e. calculates the distance to the last known Location).
     * This is a Beta feature and has not been tested thoroughly.
     */
    private final int N_SMOOTHING_LOCATIONS = 1;

    LocationTracker() {
        this.millisecondsStart = System.currentTimeMillis();
    }

    /**
     * Adds one Location sample to the tracked samples
     *
     * @param location
     */
    void addSample(Location location) {
        this.distance = this.distance + this.calculateDistanceToLastSample(location);

        this.locations.add(location);
    }

    /**
     * Distance to last sample location saved.
     *
     * @param newLocation Location
     * @return Distance in meters. 0 if we have no measurements.
     */
    private double calculateDistanceToLastSample(Location newLocation) {
        double distanceToLast;
        if (locations.size() == 0) {
            // We have no measurements yet
            distanceToLast = 0;
        } else {
            // Calculate distance from last location.
            Location lastLocation = this.locations.get(this.locations.size() - 1);
            distanceToLast = newLocation.distanceTo(lastLocation);
        }
        return distanceToLast;
    }

    /**
     * @return Average speed since start of measurments. In meters / second
     */
    double getAverageSpeed() {
        long currentMilliseconds = System.currentTimeMillis();
        // Seconds since start
        long seconds = (currentMilliseconds - this.millisecondsStart) / 1000;
        return this.distance / seconds;
    }

    /**
     * Calculates the smoothed distance of the last known location to the target location.
     * This is required because the noisy sensors might cause "jumps" in locations such that we
     * get a location that is very close to the target although we are still some distance away.
     * <p>
     * Smoothing means that we calculate the median of the last N_SMOOTHING_LOCATIONS.
     *
     * @param targetLocation
     * @return smoothed distance
     */
    double calculateSmoothedDistanceTo(Location targetLocation) {
        // Number of locations we have saved
        int nLocations = this.locations.size();
        // Initialize the median to a very large value.
        double median = 999999999;
        if (nLocations > 0) {
            // Collect the last measure distances
            ArrayList<Double> lastNDistances = new ArrayList<>();
            int takeFromIndex = Math.max(0, nLocations - N_SMOOTHING_LOCATIONS);
            for (int i = takeFromIndex; i < nLocations; ++i) {
                double dist = Float.valueOf(this.locations.get(i).distanceTo(targetLocation)).doubleValue();
                lastNDistances.add(dist);
            }
            // Calculate the median.
            median = getMedian(lastNDistances);
        }
        return median;

    }

    /**
     * Helper method to calculate the median from an ArrayList of doubles.
     * https://stackoverflow.com/questions/41117879/problems-finding-median-of-arraylist
     *
     * @param arrayList of doubles
     * @return median of arrayList
     */
    private double getMedian(ArrayList arrayList) {
        Collections.sort(arrayList);
        // Get the index for the center value
        int middle = arrayList.size() / 2;
        middle = middle % 2 == 0 ? middle - 1 : middle;
        // If we only have one entry, middle will be -1 here.
        // This line makes sure we don't access an entry that does not exist.
        middle = Math.max(middle, 0);
        return (double) arrayList.get(middle);
    }
}
