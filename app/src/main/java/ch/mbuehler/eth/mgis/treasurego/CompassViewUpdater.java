package ch.mbuehler.eth.mgis.treasurego;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class handles View updates for the CompassActivity
 */
class CompassViewUpdater extends ViewUpdater {

    /**
     * Activity whose View will be updated
     */
    private CompassActivity activity;
    /**
     * Keep track of the time since we started the Quest
     */
    private long startTime;

    /**
     *
     */
    private final int ARROW_UPDATE_DELAY = 500 * 1000000;  // in nanoseconds. 500ms

    /**
     * @param activity Activity whose View will be updated
     */
    CompassViewUpdater(CompassActivity activity) {
        this.activity = activity;

        // We keep track of when we started
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Updates the direction of the arrow pointing to the target Location
     */
    void updateArrow() {
        try {
            final ImageView arrowView = activity.findViewById(R.id.arrow);

            // Multiply by (-1) in order to make the arrow face the correct way.
            final float direction = -1 * activity.getHeading();
            arrowView.setRotation(direction);
        } catch (LocationNotFoundException e) {
            // Show message to user
            updateLocationNotFoundVisibility(View.VISIBLE);
        }
    }

    /**
     * Updates the name of the Treasure we are searching for
     */
    void updateSearchingFor() {
        TextView searchingForView = activity.findViewById(R.id.searchingForValue);
        searchingForView.setText(activity.getTargetTreasure().toString());
    }

    /**
     * Updates the field for average Speed
     */
    void updateAverageSpeed() {
        double averageSpeed = activity.getAverageSpeed();
        TextView avgSpeedView = activity.findViewById(R.id.averageSpeedValue);
        String text = Formatter.formatDouble(averageSpeed, 1) + " " + activity.getString(R.string.speedUnit);
        avgSpeedView.setText(text);
    }

    /**
     * Updates the field for current Speed
     */
    void updateCurrentSpeed() {
        TextView currentSpeedView = activity.findViewById(R.id.currentSpeedValue);
        String text = "";

        try {
            float currentSpeed = activity.getCurrentLocation().getSpeed();
            text = Formatter.formatDouble(currentSpeed, 1) + " " + activity.getString(R.string.speedUnit);
        } catch (LocationNotFoundException e) {
            text = activity.getString(R.string.n_a_);
        } finally {
            currentSpeedView.setText(text);
        }
    }

    /**
     * Updates the field for distance
     *
     * @param text pre-formatted text for distance
     */
    void updateDistance(String text) {
        TextView distanceView = activity.findViewById(R.id.distanceValue);
        distanceView.setText(text);
    }

    /**
     * Show error message when Location could not be identified.
     *
     * @param visibility View.* e.g. View.GONE
     */
    void updateLocationNotFoundVisibility(int visibility) {
        TextView locationNotFound = activity.findViewById(R.id.errorText);
        if (locationNotFound.getVisibility() != visibility) {
            locationNotFound.setVisibility(visibility);
        }
    }

    /**
     * Updates the field for time
     */
    void updateTime() {
        TextView timerTextView = activity.findViewById(R.id.timePassedValue);
        String formattedTimeDifference = this.getFormattedTimeDifference(getDeltaTimeMillis(startTime));
        timerTextView.setText(formattedTimeDifference);
    }

}
