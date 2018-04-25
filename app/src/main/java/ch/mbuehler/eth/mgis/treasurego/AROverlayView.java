package ch.mbuehler.eth.mgis.treasurego;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import java.util.Set;

/**
 * Created by vanagnos on 03.04.2017. Updated by goebelf on 18.04.2018 and Marcel on 25.4.18.
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

        // Here we force the View to be redrawn. So each time we update the Location
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

        // Update dynamic elements of View
        viewUpdater.updateOnDraw(canvas, currentLocation, arGems, rotatedProjectionMatrix);
    }

    /**
     * Returns the OnTouchListener for this View.
     *
     * @param targetTreasureUUID UUID of target Treasure
     * @return instance of AROverlayViewOnTouchListener, initialized with the variables of this View.
     */
    public OnTouchListener getOnTouchListener(final String targetTreasureUUID) {
        return new AROverlayViewOnTouchListener(targetTreasureUUID, context, arGems, toast, viewUpdater);
    }
}
