package ch.mbuehler.eth.mgis.treasurego;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by marcello on 21/04/18.
 */

// https://stackoverflow.com/questions/12448629/create-a-bounding-box-around-the-geo-point
public class LocationSampler {
    private static final float KM_PER_MILE = 1.609344f;
    private static final float DISTANCE_BETWEEN_LATITUDE = 69 * KM_PER_MILE;
    private static final float EARTH_RADIUS = 6371;  // km

    class BoundingBox{
        public BoundingBox(double latitudeMin, double latitudeMax, double longitudeMin, double longitudeMax) {
            this.latitudeMin = latitudeMin;
            this.latitudeMax = latitudeMax;
            this.longitudeMin = longitudeMin;
            this.longitudeMax = longitudeMax;
        }

        double latitudeMin;
        double latitudeMax;
        double longitudeMin;
        double longitudeMax;

        public Location sampleLocation(double altitude){
            double latitudeDifference = (latitudeMax - latitudeMin);
            double longitudeDifference = (longitudeMax - longitudeMin);

            double sampleLatitude = latitudeMin + (ThreadLocalRandom.current().nextFloat() * latitudeDifference);
            double sampleLongitude = longitudeMin + (ThreadLocalRandom.current().nextFloat() * longitudeDifference);

            Location sampledLocation = new Location("LocationSampler");
            sampledLocation.setLatitude(sampleLatitude);
            sampledLocation.setLongitude(sampleLongitude);
            sampledLocation.setAltitude(altitude);

            return sampledLocation;
        }
    }

    private BoundingBox calculateBoundingBox(Location center, double distanceMax){
        double lat = center.getLatitude();

        double relativeDistanceLatitude = distanceMax / DISTANCE_BETWEEN_LATITUDE / 2;
        // TODO divide by 2
        double latitudeMin = lat - relativeDistanceLatitude;
        double latitudeMax = lat + relativeDistanceLatitude;

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

    public List<Location> sampleLocations(int n, Location center, double distanceMin, double distanceMax){
        if(distanceMin < distanceMax){

            List<Location> sampledLocations = new ArrayList<>();

            BoundingBox boundingBox = calculateBoundingBox(center, distanceMax);
            for(int i=0; i<n; ++i){
                sampledLocations.add(sampleLocation(center, boundingBox, distanceMin, distanceMax));
            }

            return sampledLocations;
        }
        return null;

    }

    private boolean isValidLocation(Location location, Location center, double distanceMin, double distanceMax){
        double distanceToCenter = location.distanceTo(center) / 1000;  // in km
        return distanceMin <= distanceToCenter && distanceToCenter <= distanceMax;
    }

    private Location sampleLocation(Location center, BoundingBox boundingBox, double distanceMin, double distanceMax){
        Location sampledLocation = boundingBox.sampleLocation(center.getAltitude());
        while(!isValidLocation(sampledLocation, center, distanceMin, distanceMax)){
            sampledLocation = boundingBox.sampleLocation(center.getAltitude());
        }
        return sampledLocation;
    }
}
