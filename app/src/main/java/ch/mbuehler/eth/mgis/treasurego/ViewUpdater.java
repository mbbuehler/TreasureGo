package ch.mbuehler.eth.mgis.treasurego;

/**
 * Abstract base class for classes that update Views.
 */
abstract class ViewUpdater {

    /**
     * Returns the time difference between start time and current time
     *
     * @param startTime starting time in milliseconds
     * @return time difference in milliseconds
     */
    long getDeltaTimeMillis(long startTime) {
        long deltaTimeMillis = System.currentTimeMillis() - startTime;
        return deltaTimeMillis;
    }

    /**
     * Reformats deltaTimeMillis to a String representation 00:00:00 (hours:minutes:seconds).
     *
     * @param deltaTimeMillis time to be formatted in milliseconds
     * @return formatted String
     */
    protected String getFormattedTimeDifference(long deltaTimeMillis) {
        int seconds = (int) (deltaTimeMillis / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;

        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

}
