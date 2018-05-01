package ch.mbuehler.eth.mgis.treasurego;

/**
 * Formats numbers to Strings
 */
class Formatter {

    /**
     * Formats Double for given precision
     *
     * @param d         double value
     * @param precision digits after the comma, e.g. 3 for 0.123
     * @return String
     */
    static String formatDouble(double d, int precision) {
        return String.format("%1$,." + precision + "f", d);
    }
}
