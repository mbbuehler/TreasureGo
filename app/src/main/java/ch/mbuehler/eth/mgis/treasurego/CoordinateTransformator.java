package ch.mbuehler.eth.mgis.treasurego;

import android.location.Location;

/**
 * Created by vanagnos on 03.04.2017. Updated by goebelf on 18.04.2018.
 * A helper function for converting GPS coordinates to Navigation Coordinates
 * see also: https://stackoverflow.com/questions/17402723/function-that-converts-gps-coordinates-to-enu-coordinates
 */

public class CoordinateTransformator {

    private final static double WGS84_A = 6378137.0;                  // WGS 84 semi-major axis constant in meters
    private final static double WGS84_E2 = 0.00669437999014;          // square of WGS 84 eccentricity

    /**
     * A static helper function to translate GPS coordinates (WGS84) to ECEF (earth-centered, earth-fixed) coordinates.
     * The origin of the ECEF system is the center of mass of the earth, hence the name "earth-centered.". Its axes are aligned with the international reference pole (IRP)
     * and international reference meridian (IRM) that are fixed with respect to the surface of the earth,[3][4] hence the description "earth-fixed."
     * <p>
     * from https://en.wikipedia.org/wiki/ECEF
     *
     * @param location Location in WGS84
     * @return x, y and z in ECEF coordinates
     */
    public static float[] WSG84toECEF(Location location) {
        double radLat = Math.toRadians(location.getLatitude());
        double radLon = Math.toRadians(location.getLongitude());

        float clat = (float) Math.cos(radLat);
        float slat = (float) Math.sin(radLat);
        float clon = (float) Math.cos(radLon);
        float slon = (float) Math.sin(radLon);

        float N = (float) (WGS84_A / Math.sqrt(1.0 - WGS84_E2 * slat * slat));

        float x = (float) ((N + location.getAltitude()) * clat * clon);
        float y = (float) ((N + location.getAltitude()) * clat * slon);
        float z = (float) ((N * (1.0 - WGS84_E2) + location.getAltitude()) * slat);

        return new float[]{x, y, z};
    }

    /**
     * A static helper function to translate ECEF (earth-centered, earth-fixed) coordinates to ENU (East, North, Up) coordinates, a local earth based coordinate system.
     * The origin of the ENU system is the current gps location
     * <p>
     * See also http://www.navipedia.net/index.php/Transformations_between_ECEF_and_ENU_coordinates
     *
     * @param currentLocation     current location in WGS84 coordinates
     * @param ecefCurrentLocation current location in ECEF coordinates
     * @param ecefPOI             the ECEF coordinate of the point that needs to be translate to EDU coordinates
     * @return coordinates in the local east-north-up (ENU) system
     */
    public static float[] ECEFtoENU(Location currentLocation, float[] ecefCurrentLocation, float[] ecefPOI) {
        double radLat = Math.toRadians(currentLocation.getLatitude());
        double radLon = Math.toRadians(currentLocation.getLongitude());

        float clat = (float) Math.cos(radLat);
        float slat = (float) Math.sin(radLat);
        float clon = (float) Math.cos(radLon);
        float slon = (float) Math.sin(radLon);

        float dx = ecefCurrentLocation[0] - ecefPOI[0];
        float dy = ecefCurrentLocation[1] - ecefPOI[1];
        float dz = ecefCurrentLocation[2] - ecefPOI[2];

        float east = -slon * dx + clon * dy;

        float north = -slat * clon * dx - slat * slon * dy + clat * dz;

        float up = clat * clon * dx + clat * slon * dy + slat * dz;

        return new float[]{east, north, up, 1};
    }
}

