package ch.mbuehler.eth.mgis.treasurego;

import android.location.Location;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by marcello on 23/04/18.
 */

public class ARViewUpdater extends ViewUpdater {
    /**
     * Activity whose View will be updated
     */
    private ARActivity activity;
    /**
     * Keep track of the time since we started the Quest
     */
    private long startTime;

    private TextView timerTextView;

    private TextView tvCurrentLocation;

    private TextView arGemsNotFoundTextView;



    RelativeLayout imgView;
    RelativeLayout arActivityView;

    RelativeLayout.LayoutParams params;

    /**
     * @param activity Activity whose View will be updated
     */
    ARViewUpdater(ARActivity activity) {
        this.activity = activity;

        // We keep track of when we started
        this.startTime = System.currentTimeMillis();

        timerTextView = activity.findViewById(R.id.timePassedValue);
        tvCurrentLocation = activity.findViewById(R.id.tv_current_location);
        arGemsNotFoundTextView = activity.findViewById(R.id.arGemsNotFound);
        arActivityView = activity.findViewById(R.id.activity_ar);

        imgView = (RelativeLayout) View.inflate(activity, R.layout.image_gemview, null);




        params = new RelativeLayout.LayoutParams(30, 40);
        params.leftMargin = 50;
        params.topMargin = 60;

        arActivityView.addView(imgView, params);

    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * Updates the field for time
     */
    void updateTime() {
        String formattedTimeDifference = this.getFormattedTimeDifference(getDeltaTimeMillis(startTime));
        timerTextView.setText(formattedTimeDifference);
    }

    void updateTVCurrentLocation(Location location){

    tvCurrentLocation.setText(String.format("lat: %s \nlon: %s \naltitude: %s \n",
            location.getLatitude(), location.getLongitude(), location.getAltitude()));

    }

    /**
     * Updates TextView that shows the user how many Gems are left to find.
     * @param numberARGemsNotFound number of ARGems that have not been collected
     */
    void updateARGemsNotFound(int numberARGemsNotFound){
        String text = String.format("%d %s", numberARGemsNotFound, activity.getString(R.string.arGemsLeft));
        arGemsNotFoundTextView.setText(text);
    }

    void updateGemParams(int leftMargin, int topMargin){
        params.leftMargin = leftMargin;
        params.topMargin = topMargin;
    }
}

