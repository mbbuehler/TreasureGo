package ch.mbuehler.eth.mgis.treasurego;


import android.annotation.TargetApi;
import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

/**
 * This class holds methods for checking whether LocationServices are available.
 */
class LocationServiceChecker {

    /**
     * Gets the state of Airplane Mode.
     *
     * @param context
     * @return true if enabled.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

    /**
     * Checks if LocationServices are enabled.
     * This means we have GPS and the airplane mode is off.
     *
     * @param locationManager LocationManager
     * @param context         Activity or Context
     * @return true if LocationServices are enabled
     */
    static boolean areLocationServicesEnabled(LocationManager locationManager, Context context) {
        boolean gps_enabled;
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            gps_enabled = false;
        }
        boolean airplaneModeIsOff = !isAirplaneModeOn(context);
        return airplaneModeIsOff && gps_enabled;
    }
}
