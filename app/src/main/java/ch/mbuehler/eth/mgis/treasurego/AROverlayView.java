package ch.mbuehler.eth.mgis.treasurego;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Set;

/**
 * Created by vanagnos on 03.04.2017. Updated by goebelf on 18.04.2018.
 * We extend the View Class of Android (see also: https://developer.android.com/reference/android/view/View.html)
 * A View occupies a rectangular area on the screen and is responsible for drawing and event handling.
 * Here we override the onDraw function and we also draw the ARPoints we want. See the onDraw()
 * function and how we transform the GPS84 coordinates to camera coordinates.
 */
public class AROverlayView extends View {

    Activity activity;
    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;

    /**
     * Gems that the user is supposed to collect.
     */
    private Set<ARGem> arGems;
    /**
     * We need access to displayed Toasts such that we can renew the content even when the old
     * Toast is still showing, so we use an instance variable for Toast.
     */
    private Toast toast;
    /**
     * Updates the Elements of the View that are not related to
     */
    private ARViewUpdater viewUpdater;

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);


    LinearLayout lL;



    /**
     * Constructor of the AROverlyView class. Takes the context and List of ARPoints as arguments.
     *
     * @param context the context creating the class
     * @param arGems  the List of ARPoints to be drawn
     */
    public AROverlayView(Context context, Set<ARGem> arGems, ARViewUpdater viewUpdater, Activity activity) {
        super(context);

        this.activity = activity;

        this.context = context;
        this.arGems = arGems;
        this.viewUpdater = viewUpdater;

        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);

        // Initialize arActivityView
        viewUpdater.updateARGemsNotFound(arGems.size());





    }

    /**
     * Whenever the orientation of the phone changes, this method should be called to force the View to be redrawn.
     *
     * @param rotatedProjectionMatrix the new projectionMatrix
     */
    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;

        // Here we force the View to be redrawn. So each time we update the projection matrix
        // the arActivityView is redrawn
        this.invalidate();
    }

    /**
     * Whenever the location changes, this method should be called to force the View to be redrawn.
     *
     * @param currentLocation the new location
     */
    public void updateCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;

        // Here we force the View to be redrawn. So each time we update the projection matrix
        // the arActivityView is redrawn
        this.invalidate();
    }

    /**
     * Called when the arActivityView should render its content. If the current Location is valid, we
     * calculate the positions of each of the ARGem corresponding to the user's current position
     * and draw them.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // if the current location is invalid
        if (currentLocation == null) {
            // do nothing
            return;
        }

        // variables for the point representation
        final int radius = 30;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(60);

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

//                imgView.setVisibility(View.VISIBLE);
                //arActivityView.addView(imgView);

                // placing the edit text at specific co-ordinates:
                //canvas.translate(0, 0);

                              canvas.drawCircle(x, y, radius, paint);
                canvas.drawText(arGem.getName(), x - (30 * arGem.getName().length() / 2), y - 80, paint);


                viewUpdater.updateGemParams((int)x, (int)y);
            }
        }
    }

    /**
     * Returns the OnTouchListener for this View.
     * @param targetTreasureUUID UUID of target Treasure
     * @return instance of AROverlayViewOnTouchListener, initialized with the variables of this View.
     */
    public OnTouchListener getOnTouchListener(final String targetTreasureUUID) {
        return new AROverlayViewOnTouchListener(targetTreasureUUID, context, arGems, toast, viewUpdater);
    }
}
