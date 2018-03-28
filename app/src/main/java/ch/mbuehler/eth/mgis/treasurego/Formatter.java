package ch.mbuehler.eth.mgis.treasurego;

/**
 * Created by marcello on 28/03/18.
 */

public class Formatter {

    /**
     * Formats Double for given precision
     * @param d
     * @param precision digits after the comma, e.g. 3 for 0.123
     * @return String
     */
    public static String formatDouble(double d, int precision) {
        return String.format("%1$,." + precision + "f", d);
    }
}
