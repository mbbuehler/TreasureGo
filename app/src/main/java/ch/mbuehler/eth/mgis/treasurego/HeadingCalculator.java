package ch.mbuehler.eth.mgis.treasurego;

import android.hardware.GeomagneticField;
import android.location.Location;
import android.util.Log;

/**
 * Responsible for calculating the (degree) directions from one Location to another.
 */
class HeadingCalculator {

    /**
     * Calculates the heading to the target Location.
     * Adapted from
     * https://stackoverflow.com/questions/5479753/using-orientation-sensor-to-point-towards-a-specific-location
     * @param orientation  orientation vector from SensorManager.getOrientation(mRotationMatrix, orientation)
     * @param currentLocation current Location
     * @param targetLocation target Location
     * @return 0 <= angle <= 359 to target Location
     */
    static float calculateHeading(float[] orientation, Location currentLocation, Location targetLocation) {
        // Azimuth to North. Comes in radians
        double azimuthRadians = orientation[0];
        // We want degrees
        double azimuthDegrees = Math.toDegrees(azimuthRadians);

        // Converting the magnetic North to the true North
        float declination = calculateDeclination(currentLocation);
        azimuthDegrees += declination;

        // Get the bearing to the target location
        float bearing = currentLocation.bearingTo(targetLocation);

        // Bearing gives us the angle to the destination in Degrees East of true North
        double heading = (bearing - azimuthDegrees) * -1;
        double normalizedHeading = normalizeDegree(heading);

        // We want to return a float
        return Double.valueOf(normalizedHeading).floatValue();
    }

    /**
     * Calculates the declination for the current Location.
     * Needed for converting the magnetic North to the true North.
     * @param currentLocation Location
     * @return float declination
     */
    private static float calculateDeclination(Location currentLocation){
        GeomagneticField geoField = new GeomagneticField(
                Double.valueOf(currentLocation.getLatitude()).floatValue(),
                Double.valueOf(currentLocation.getLongitude()).floatValue(),
                Double.valueOf(currentLocation.getAltitude()).floatValue(),
                System.currentTimeMillis()
        );
        return geoField.getDeclination();
    }

    /**
     * Helper method.
     * Converts degrees from the range [~-180, ~+180] to the range [0, 359]
     * @param value degree value
     * @return normalized 0 <= degree <= 359
     */
    private static double normalizeDegree(double value) {
        double normalized;
        // https://stackoverflow.com/questions/4308262/calculate-compass-bearing-heading-to-location-in-android
        if (value >= 0.0d && value <= 180.0d) {
            normalized = value;
        } else {
            normalized = 180 + (180 + value);
        }
        normalized = Math.min(359, normalized);
        normalized = Math.max(0, normalized);
        return normalized;
    }
}
