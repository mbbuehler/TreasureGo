package ch.mbuehler.eth.mgis.treasurego;

import android.os.Handler;

/**
 * This class handles regular View updates. In particular it manages
 * updates that are not triggered by other Listeners.
 */
public class CompassActivityRunnable implements Runnable {

    /**
     * timeHandler posts the timerRunnable regularly
     */
    private Handler timerHandler;

    /**
     * Responsible for updating the View elements, e.g. distance, time, etc.
     */
    private ViewUpdater viewUpdater;
    /**
     * Time since we last updated the arrow. Keeping track of this and delaying udpates
     * helps avoid constantly doing extremely similar computations
     */
    private long lastMeasuredTime = 0;

    /**
     * Delay between sensor and View updates
     */
    private final int SENSOR_DELAY = 500; // 500ms

    CompassActivityRunnable(Handler timerHandler, ViewUpdater viewUpdater) {
        this.timerHandler = timerHandler;
        this.viewUpdater = viewUpdater;
    }

    /**
     * Updates View regularly.
     * Repeatedly calls itself to make sure the updates keep happening.
     * Adapted from
     * https://stackoverflow.com/questions/4597690/android-timer-how-to
     */
    @Override
    public void run() {
        // Update various information in View
        viewUpdater.updateAverageSpeed();
        viewUpdater.updateCurrentSpeed();
        viewUpdater.updateCurrentReward();
        viewUpdater.updateTime();

        // Only update arrow once in a while
        if (System.nanoTime() - lastMeasuredTime > 1000000000) {
            viewUpdater.updateArrow();
            lastMeasuredTime = System.nanoTime();
        }
        // Make sure this Runnable is run again
        timerHandler.postDelayed(this, SENSOR_DELAY);
    }
}
