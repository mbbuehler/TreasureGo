package ch.mbuehler.eth.mgis.treasurego;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
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


    long lastDrawUpdate = System.currentTimeMillis();
    long DELTA_DRAW_UPDATE = 1;

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    RelativeLayout arActivityView;

    RelativeLayout.LayoutParams params;

    class ARGemLayout{
        RelativeLayout layout;
        RelativeLayout.LayoutParams params;

        public ARGemLayout(RelativeLayout layout) {
            this.layout = layout;
            this.params = new RelativeLayout.LayoutParams(R.dimen.gem_width, R.dimen.gem_height);
            updatePosition(-100,-100);
        }

        void updatePosition(int x, int y){
            this.params.leftMargin = x - 60;
            this.params.topMargin = y - 60;
        }
    }

    /**
     * @param activity Activity whose View will be updated
     */
    ARViewUpdater(ARActivity activity) {
        this.activity = activity;

        // We keep track of when we started
        this.startTime = System.currentTimeMillis();

        timerTextView = activity.findViewById(R.id.timePassedValue);
        tvCurrentLocation = activity.findViewById(R.id.tv_current_location);
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


//    void updateOnDraw(Canvas canvas, Location currentLocation, float[] rotatedProjectionMatrix){
//        if(System.currentTimeMillis() - lastDrawUpdate > DELTA_DRAW_UPDATE) {
//
//            // variables for the point representation
//            final int radius = 30;
//            paint.setStyle(Paint.Style.FILL);
//            paint.setColor(Color.WHITE);
//            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
//            paint.setTextSize(60);
//
//            // Transform the ARPoints coordinates from WGS84 to camera coordinates
//            for (ARGem arGem : arGemLayouts.keySet()) {
//
//                // First we transform from GPS coordinates to ECEF coordinates and then to Navigation Coordinates
//                float[] currentLocationInECEF = CoordinateTransformator.WSG84toECEF(currentLocation);
//                float[] pointInECEF = CoordinateTransformator.WSG84toECEF(arGem.getLocation());
//                float[] pointInENU = CoordinateTransformator.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);
//
//                // Afterwards we transform the Navigation coordinates (ENU) to Camera coordinates
//                float[] cameraCoordinateVector = new float[4];
//
//                // To convert ENU coordinate to Camera coordinate, we will multiply camera projection matrix
//                // with ENU coordinate vector, the result is a vector [v0, v1, v2, v3].
//                Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);
//
//
//                // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
//                // if z > 0, the point will display on the opposite
//                if (cameraCoordinateVector[2] < 0) {
//
//                    //Then x = (0.5 + v0 / v3) * widthOfCameraView and y = (0.5 - v1 / v3) * heightOfCameraView.
//                    float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas.getWidth();
//                    float y = (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight();
//
//                    // We need to keep track of the position of the ARGems
//                    ARGemLayout gemLayout = this.arGemLayouts.get(arGem);
//                    gemLayout.updatePosition((int)x, (int)y);
//                    gemLayout.layout.requestLayout();
//                    arGem.setX(x);
//                    arGem.setY(y);
//
//                    canvas.drawCircle(x, y, radius, paint);
//                    canvas.drawText(arGem.getName(), x - (30 * arGem.getName().length() / 2), y - 80, paint);
//                }
//            }
//            lastDrawUpdate = System.currentTimeMillis();
//        }
//    }
}

