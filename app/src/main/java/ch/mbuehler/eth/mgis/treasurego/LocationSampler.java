package ch.mbuehler.eth.mgis.treasurego;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class for sampling Locations.
 * Based on
 * https://stackoverflow.com/questions/12448629/create-a-bounding-box-around-the-geo-point
 */
class LocationSampler {
    /**
     * Conversion rate for KM / Mile
     */
    private static final float KM_PER_MILE = 1.609344f;
    /**
     * Approximate distance between degrees of latitude
     */
    private static final float DISTANCE_BETWEEN_LATITUDE = 69 * KM_PER_MILE;
    /**
     * Radius of the earth in KM
     */
    private static final float EARTH_RADIUS = 6371;  // km

    /**
     * The Location around which we should sample other Locations
     */
    private Location center;
    /**
     * Distance bounds to center Location and altitude
     */
    private double distanceMin;
    private double distanceMax;
    private double altitude;

    /**
     *
     * @param center Location around which we should sample other Locations
     * @param distanceMin Only sample Locations that are further away that this (KM)
     * @param distanceMax Only sample Locations that are closer than this (KM)
     * @param altitude Set the altitude of the Locations roughly to this value (meters).
     */
    LocationSampler(Location center, double distanceMin, double distanceMax, double altitude) {
        this.center = center;
        this.distanceMin = distanceMin;
        this.distanceMax = distanceMax;
        this.altitude = altitude;
    }

    /**
     * Class representing a bounding box
     */
    private class BoundingBox{
        /**
         * @param latitudeMin Bound for the bounding box
         * @param latitudeMax Bound for the bounding box
         * @param longitudeMin Bound for the bounding box
         * @param longitudeMax Bound for the bounding box
         */
        BoundingBox(double latitudeMin, double latitudeMax, double longitudeMin, double longitudeMax) {
            this.latitudeMin = latitudeMin;
            this.latitudeMax = latitudeMax;
            this.longitudeMin = longitudeMin;
            this.longitudeMax = longitudeMax;
        }

        double latitudeMin;
        double latitudeMax;
        double longitudeMin;
        double longitudeMax;

        /**
         * Sample one Location within this BoundingBox.
         * The minimum distance is not yet considered (only maximum distance).
         * @param altitude The altitude of the sampled Location will be sampled from a Gaussian
         *                 distribution with mean altitude.
         * @return a random Location within bounds
         */
        Location sampleLocation(double altitude){
            // Calculate the deltas for latitude and longitude
            double latitudeDifference = (latitudeMax - latitudeMin);
            double longitudeDifference = (longitudeMax - longitudeMin);

            // Sample random values within allowed range
            double sampleLatitude = latitudeMin + (ThreadLocalRandom.current().nextFloat() * latitudeDifference);
            double sampleLongitude = longitudeMin + (ThreadLocalRandom.current().nextFloat() * longitudeDifference);

            // Create new Location and set sampled values.
            Location sampledLocation = new Location("LocationSampler");
            sampledLocation.setLatitude(sampleLatitude);
            sampledLocation.setLongitude(sampleLongitude);
            // Add some Gaussian noise to altitude to make the placement of Gems more interesting.
            sampledLocation.setAltitude(altitude + ThreadLocalRandom.current().nextGaussian() * 10);

            return sampledLocation;
        }
    }

    /**
     * Creates a BoundingBox around center with maximum distance distanceMax
     * @param center Location
     * @param distanceMax in kilometers
     * @return BoundingBox
     */
    private BoundingBox calculateBoundingBox(Location center, double distanceMax){
        // Get the latitude bounds
        // This is a simple approximates because the distance between latitude degrees is
        // approximately constant.
        double lat = center.getLatitude();
        double relativeDistanceLatitude = distanceMax / DISTANCE_BETWEEN_LATITUDE / 2;
        double latitudeMin = lat - relativeDistanceLatitude;
        double latitudeMax = lat + relativeDistanceLatitude;

        // Get the longitude bounds
        // They depend on the current latitude so we need to do some calculations.
        // This code is based on
        // http://janmatuschek.de/LatitudeLongitudeBoundingCoordinates#SphereRadius
        double lng = center.getLongitude();
        double latRad = Math.toRadians(lat);
        double delta = distanceMax / EARTH_RADIUS;
        double deltaLngRad = Math.asin(Math.sin(delta) / Math.cos(latRad));
        double deltaLngDeg = Math.toDegrees(deltaLngRad) / 2;

        double longitudeMin = lng - deltaLngDeg;
        double longitudeMax = lng + deltaLngDeg;

        return new BoundingBox(latitudeMin, latitudeMax, longitudeMin, longitudeMax);
    }

    /**
     * Samples n Locations that are valid.
     * @param n number of Locations to be sampled
     * @return List of sampled Locations if valid values for distances have been provided and null otherwise.
     */
    List<Location> sampleLocations(int n){
        // This is a necessary requirement
        if(distanceMin < distanceMax){
            List<Location> sampledLocations = new ArrayList<>();
            // We first create a BoundingBox and sample Locations within it.
            BoundingBox boundingBox = calculateBoundingBox(center, distanceMax);
            for(int i=0; i<n; ++i){
                sampledLocations.add(sampleLocation(boundingBox));
            }
            return sampledLocations;
        }
        return null;
    }

    /**
     * A Location is valid if its distance is greater than the minimum distance and smaller
     * than the maximum distance to the center Location.
     * @param location sampled Location
     * @return true if the Location is accepted and false otherwise
     */
    private boolean isValidLocation(Location location){
        double distanceToCenter = location.distanceTo(center) / 1000;  // in km
        return distanceMin <= distanceToCenter && distanceToCenter <= distanceMax;
    }

    /**
     * Samples a single Location that satisfies both the minimum and maximum distance requirements.
     * @param boundingBox BoundingBox for maximum distance
     * @return one sampled Location within bounds
     */
    private Location sampleLocation(BoundingBox boundingBox){
        Location sampledLocation = boundingBox.sampleLocation(altitude);
        while(!isValidLocation(sampledLocation)){
            sampledLocation = boundingBox.sampleLocation(altitude);
        }
        return sampledLocation;
    }
}
