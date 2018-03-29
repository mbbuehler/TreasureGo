package ch.mbuehler.eth.mgis.treasurego;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class handles View updates for the CompassActivity
 */
class ViewUpdater {

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
     *
     * @param activity Activity whose View will be updated
     */
    ViewUpdater(CompassActivity activity) {
        this.activity = activity;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Updates the direction of the arrow pointing to the target Location
     */
    void updateArrow() {
        try {
            ImageView arrowView = activity.findViewById(R.id.arrow);

            // Multiply by (-1) in order to make the arrow face the correct way.
            float direction = -1 * activity.getHeading();

//                Log.v("loc", String.format("old Rot: %f / dir: %f", oldRotation, direction));
//                RotateAnimation rotate = new RotateAnimation(arrowRotation, direction, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, .5f );
//
//                arrowRotation = direction;
////                        new RotateAnimation(oldRotation, 90, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//                rotate.setDuration(ARROW_UPDATE_DELAY * 2 / 1000000); //Math.round(Math.abs(arrowRotation - direction)/360*500));
//                rotate.setInterpolator(new LinearInterpolator());
//                arrowView.startAnimation(rotate);
//
            arrowView.setRotation(direction);


        } catch (LocationNotFoundException e) {
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
        TextView avgSpeedView = (TextView) activity.findViewById(R.id.averageSpeedValue);
        avgSpeedView.setText(Formatter.formatDouble(averageSpeed, 1) + " m/s");
    }

    /**
     * Updates the field for current Speed
     */
    void updateCurrentSpeed() {
        TextView currentSpeedView = (TextView) activity.findViewById(R.id.currentSpeedValue);
        String text = "";

        try {
            float currentSpeed = activity.getCurrentLocation().getSpeed();
            text = Formatter.formatDouble(currentSpeed, 1)+ " m/s";
        } catch (LocationNotFoundException e) {
            text = "n.a.";
        } finally {
            currentSpeedView.setText(text);
        }
    }

    /**
     * Updates the field for distance
     * @param text pre-formatted text for distance
     */
    void updateDistance(String text) {
        TextView distanceView = (TextView) activity.findViewById(R.id.distanceValue);
        distanceView.setText(text);
    }

    /**
     * Updates the field for temperature.
     */
    void updateTemperature() {
        // TODO: handle case if temperature is not available.
        float currentTemperature = activity.getCurrentTemperature();
        TextView temperatureView = (TextView) activity.findViewById(R.id.currentTemperatureValue);
        String temperatureString = Formatter.formatDouble(currentTemperature, 1) + " \u2103";
        temperatureView.setText(temperatureString);
    }

    /**
     * Updates the field for time
     */
    void updateTime() {
        long millis = System.currentTimeMillis() - startTime;
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;

        TextView timerTextView = activity.findViewById(R.id.timePassedValue);
        timerTextView.setText(String.format("%d:%02d:%02d", hours, minutes, seconds));
    }

    /**
     * Updates the field for currently achievable reward
     */
    void updateCurrentReward() {
        int currentReward = RewardCalculator.calculateReward(
                activity.getTargetTreasure(),
                activity.getAverageSpeed(),
                activity.getCurrentTemperature()
        );
        TextView currentRewardView = activity.findViewById(R.id.CurrentRewardValue);
        String text = String.format("%d coins", currentReward);
        currentRewardView.setText(text);
    }

    /**
     * Show error message when Location could not be identified.
     * @param visibility View.* e.g. View.GONE
     */
    void updateLocationNotFoundVisibility(int visibility) {
        TextView locationNotFound = activity.findViewById(R.id.errorText);
        if (locationNotFound.getVisibility() != visibility) {
            locationNotFound.setVisibility(visibility);
        }
    }

}
