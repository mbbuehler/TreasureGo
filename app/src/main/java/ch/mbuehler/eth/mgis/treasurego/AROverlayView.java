package ch.mbuehler.eth.mgis.treasurego;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vanagnos on 03.04.2017. Updated by goebelf on 18.04.2018.
 * We extend the View Class of Android (see also: https://developer.android.com/reference/android/view/View.html)
 * A View occupies a rectangular area on the screen and is responsible for drawing and event handling.
 * Here we override the onDraw function and we also draw the ARPoints we want. See the onDraw()
 * function and how we transform the GPS84 coordinates to camera coordinates.
 */

public class AROverlayView extends View {

    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    private List<ARPoint> arPoints;

    /**
     * Constructor of the AROverlyView class. Takes the contex and List of ARPoints as arguments.
     * @param context the context creating the class
     * @param arPoints the List of ARPoints to be drawn
     */
    public AROverlayView(Context context,List<ARPoint> arPoints) {
        super(context);

        this.context = context;
        this.arPoints = arPoints;
    }

    /**
     * Whenever the orientation of the phone changes, this method should be called to force the View to be redrawn.
     * @param rotatedProjectionMatrix the new projectionMatrix
     */
    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;

        // Here we force the View to be redrawn. So each time we update the projection matrix
        // the view is redrawn
        this.invalidate();
    }

    /**
     * Whenever the location changes, this method should be called to force the View to be redrawn.
     * @param currentLocation the new location
     */
    public void updateCurrentLocation(Location currentLocation){
        this.currentLocation = currentLocation;

        // Here we force the View to be redrawn. So each time we update the projection matrix
        // the view is redrawn
        this.invalidate();
    }

    /**
     * Called when the view should render its content. If the current Location is valid, we
     * calculate the positions of each of the ARPoint corresponding to the user's current position
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
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(60);

        // Transform the ARPoints coordinates from WGS84 to camera coordinates
        for (int i = 0; i < arPoints.size(); i ++) {

            // First we transform from GPS coordinates to ECEF coordinates and then to Navigation Coordinates
            float[] currentLocationInECEF = CoordinateTransformator.WSG84toECEF(currentLocation);
            float[] pointInECEF = CoordinateTransformator.WSG84toECEF(arPoints.get(i).getLocation());
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
                float x  = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * canvas.getWidth();
                float y = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * canvas.getHeight();

                arPoints.get(i).x = x;
                arPoints.get(i).y = y;

                canvas.drawCircle(x, y, radius, paint);
                canvas.drawText(arPoints.get(i).getName(), x - (30 * arPoints.get(i).getName().length() / 2), y - 80, paint);
            }
        }
    }

    public OnTouchListener getOnTouchListener(){
        return new OnTouchListener(){

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                double DIST_THRESHOLD = 60;

                double x = motionEvent.getX();
                double y = motionEvent.getY();
                Log.v("TOUCH", String.format("%f, %f", x, y));

                double closestDistance = 99999999;
                ARPoint closestPoint = null;
                for(int i = 0; i < arPoints.size(); ++i) {
                    ARPoint point = arPoints.get(i);
                    double distance = point.euclideanDistanceTo(x, y);
                    if(closestPoint == null || distance < closestDistance){
                        closestPoint = point;
                        closestDistance = distance;
                    }
                }


                if(closestDistance < DIST_THRESHOLD)
                {
                    Toast.makeText(context, "touched Point "+closestPoint.getName(), Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(context, "only touched", Toast.LENGTH_SHORT).show();

                }


                    return false;
            }
        };
    }
}
