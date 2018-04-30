package ch.mbuehler.eth.mgis.treasurego;

/**
 * Abstract base class for classes that update Views.
 */
abstract class ViewUpdater {


    long getDeltaTimeMillis(long startTime) {
        long deltaTimeMillis = System.currentTimeMillis() - startTime;
        return deltaTimeMillis;
    }

    protected String getFormattedTimeDifference(long deltaTimeMillis) {
        int seconds = (int) (deltaTimeMillis / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;

        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

}
