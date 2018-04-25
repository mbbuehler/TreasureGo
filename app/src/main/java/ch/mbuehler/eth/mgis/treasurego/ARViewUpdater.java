package ch.mbuehler.eth.mgis.treasurego;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Set;

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

    long lastDrawUpdate = System.currentTimeMillis();
    long DELTA_DRAW_UPDATE = 1;



    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

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

    void updateOnDraw(Canvas canvas, Location currentLocation, Set<ARGem> arGems, float[] rotatedProjectionMatrix){
        if(System.currentTimeMillis() - lastDrawUpdate > DELTA_DRAW_UPDATE) {

            // variables for the point representation
            final int radius = 30;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            paint.setTextSize(60);

            int i = 0;
            // Transform the ARPoints coordinates from WGS84 to camera coordinates
            for (ARGem arGem : arGems) {

                // First we transform from GPS coordinates to ECEF coordinates and then to Navigation Coordinates
                float[] currentLocationInECEF = CoordinateTransformator.WSG84toECEF(currentLocation);
                float[] pointInECEF = CoordinateTransformator.WSG84toECEF(arGem.getLocation());
                float[] pointInENU = CoordinateTransformator.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

                // Afterwards we transform the Navigation coordinates (ENU) to Camera coordinates
                float[] cameraCoordinateVector = new float[4];

                // To convert ENU coordinate to Camera coordinate, we will multiply camera projection matrix
                // with ENU coordinate vector, the result is a vector [v0, v1, v2, v3].
                Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);


                // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
                // if z > 0, the point will display on the opposite
                if (cameraCoordinateVector[2] < 0) {

                    //Then x = (0.5 + v0 / v3) * widthOfCameraView and y = (0.5 - v1 / v3) * heightOfCameraView.
                    float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas.getWidth();
                    float y = (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight();

                    // We need to keep track of the position of the ARGems
                    arGem.setX(x);
                    arGem.setY(y);

                    canvas.drawCircle(x, y, radius, paint);
                    canvas.drawText(arGem.getName(), x - (30 * arGem.getName().length() / 2), y - 80, paint);

                    i += 1 % 5;
                    if(i==1)
                        this.updateGemParams((int) x, (int) y);
                }
            }
            lastDrawUpdate = System.currentTimeMillis();

            // Make sure the ARGems position gets updated.
            imgView.requestLayout();
        }
    }
}

