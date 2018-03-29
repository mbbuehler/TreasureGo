package ch.mbuehler.eth.mgis.treasurego;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Formats temperature text
 */
public class TemperatureText {
    /**
     * Activity using this class
     */
    Context context;

    /**
     * Calling activity
     *
     * @param context
     */
    public TemperatureText(Context context) {
        this.context = context;
    }

    /**
     * @return true if the device has a temperature Sensor
     */
    private boolean hasAmbientTemperatureSensor() {
        // Get information from PackageManager
        PackageManager manager = context.getPackageManager();
        return manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_AMBIENT_TEMPERATURE);
    }

    /**
     * Defines and formats text for temperature Views
     * If there is no temperature Sensor available,
     * the returned string will contain this information.
     *
     * @param temperature temperature in degrees Celsius
     * @return formatted text
     */
    String getText(float temperature) {
        String text;
        if (hasAmbientTemperatureSensor()) {
            text = Formatter.formatDouble(temperature, 1) + context.getString(R.string.degreeC);
        } else {
            text = context.getString(R.string.NAnoSensor);
        }
        return text;
    }
}
