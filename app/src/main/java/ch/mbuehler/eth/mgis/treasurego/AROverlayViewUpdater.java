package ch.mbuehler.eth.mgis.treasurego;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;


/**
 * This class manages View updates for the AROverlayView
 */
class AROverlayViewUpdater extends ViewUpdater {

    /**
     * Activity for context
     */
    ARActivity activity;
    /**
     * A Gem is considered as collected when the user touches within this distance (pixels)
     * around the center of the Gem.
     */
    private final double TOUCH_DISTANCE_THRESHOLD = 90;
    /**
     * We need access to displayed Toasts such that we can renew the content even when the old
     * Toast is still showing, so we use an instance variable for Toast.
     */
    private Toast toast;
    /**
     * Info View for how many gems have yet to be found.
     */
    private TextView arGemsNotFoundTextView;
    /**
     * Holds a layout for each Gem
     */
    private HashMap<ARGem, ARGemLayout> arGemLayouts = new HashMap<>();
    /**
     * Object holding paint information. See initPaint() for values.
     */
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * Main View of this activity. Here we will add the Views for the Gems.
     */
    private RelativeLayout arActivityView;

    /**
     * Updates TextView that shows the user how many Gems are left to find.
     *
     * @param numberARGemsNotFound number of ARGems that have not been collected
     */
    private void updateARGemsNotFound(int numberARGemsNotFound) {
        String text = String.format("%d %s", numberARGemsNotFound, activity.getString(R.string.arGemsLeft));
        arGemsNotFoundTextView.setText(text);
    }

    /**
     * @param activity Activity whose View will be updated
     */
    AROverlayViewUpdater(ARActivity activity) {
        this.activity = activity;

        // Bind Views
        arActivityView = activity.findViewById(R.id.activity_ar);
        arGemsNotFoundTextView = activity.findViewById(R.id.arGemsNotFound);

        // Init Toast
        toast = Toast.makeText(activity, "", Toast.LENGTH_SHORT);

        initARGems();
        initPaint();

        // Initialize info view
        updateARGemsNotFound(ARGameStatus.Instance().getARGems().size());
    }

    /**
     * Creates and saves the Layout for the ARGems
     */
    private void initARGems() {
        for (ARGem arGem : ARGameStatus.Instance().getARGems()) {
            RelativeLayout layout = (RelativeLayout) View.inflate(activity, R.layout.image_gemview, null);
            ARGemLayout gemLayout = new ARGemLayout(layout);
            ((ImageView) gemLayout.layout.findViewById(R.id.image_gem)).setImageResource(arGem.getImageId());
            arGemLayouts.put(arGem, gemLayout);
            arActivityView.addView(layout, gemLayout.params);
        }
    }

    /**
     * Initializes the values for the painted components.
     */
    private void initPaint() {
        paint.setColor(ContextCompat.getColor(activity, R.color.orange));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(activity.getResources().getDimension(R.dimen.text_size));
    }

    /**
     * Called upon onDraw(). Updates the AR components in the View, i.e. the ARGems and the
     * text.
     *
     * @param canvas                  Canvas from onDraw()
     * @param currentLocation         We need to know the current position so we can calculate the
     *                                corresponding x,y coordinates of the screen.
     * @param rotatedProjectionMatrix Projection matrix from sensors.
     */
    void updateOnDraw(Canvas canvas, Location currentLocation, float[] rotatedProjectionMatrix) {

        // Transform the ARPoints coordinates from WGS84 to camera coordinates
        for (ARGem arGem : ARGameStatus.Instance().getARGems()) {

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

                updateARGemPosition(arGem, x, y);

                // Draw the ARGem name
                canvas.drawText(arGem.getName(), x - (30 * arGem.getName().length() / 2), y - 100, paint);
            }
        }
    }

    /**
     * Updates the ARGem position.
     *
     * @param arGem ARGem
     * @param x     x-coordinate on the screen (pixel)
     * @param y     y-coordinate on the screen (pixel)
     */
    private void updateARGemPosition(ARGem arGem, double x, double y) {
        // The position is determined by the layout
        ARGemLayout gemLayout = this.arGemLayouts.get(arGem);
        gemLayout.updatePosition((int) x, (int) y, activity);
        gemLayout.layout.requestLayout();
        // We need to keep track of the position such that we can calculate their
        // distance to other points afterwards
        arGem.setX(x);
        arGem.setY(y);
    }

    /**
     * The user has touched the screen. If an ARGem has been touched, collect it. Otherwise
     * show an info Toast telling the user that she did not catch an ARGem.
     *
     * @param x x-coordinate on the screen (pixel)
     * @param y y-coordinate on the screen (pixel)
     */
    void onTouch(double x, double y) {
        // Only perform action if we are not done yet
        if (ARGameStatus.Instance().getARGems().size() > 0) {
            // Find closest ARGem
            ARGem closestARGem = findClosestGem(x, y);
            double closestDistance = closestARGem.euclideanDistanceTo(x, y);

            // If the are close enough to the closest ARGem, then take action.
            if (closestDistance < TOUCH_DISTANCE_THRESHOLD) {
                // We can't collect this Gem again.
                ARGameStatus.Instance().removeARGem(closestARGem);
                arActivityView.removeView(arGemLayouts.get(closestARGem).layout);

                // Inform user about his success.
                String info = closestARGem.getName() + " " + activity.getString(R.string.collected);
                showToastInfo(info);

                // Update TextView
                updateARGemsNotFound(ARGameStatus.Instance().getARGems().size());

                if (ARGameStatus.Instance().getARGems().isEmpty()) {
                    // The user has found all ARGems. We can continue.
                    onAllGemsCollected();
                }
            } else {
                // The user did not catch an ARGem, but touched somewhere else.
                showToastInfo(R.string.noGemFoundHere);
            }
        }
    }

    /**
     * The user has collected all gems. This method stores the results and forwards
     * the user to the TreasureFoundActivity
     */
    private void onAllGemsCollected() {
        String targetTreasureUUID = ARGameStatus.Instance().getTargetTreasure().getUuid();
        // Update the current Quest
        Quest quest = GameStatus.Instance().getLastQuestForTreasureUuid(targetTreasureUUID);
        quest.setGemCollectionTimeMillis(getDeltaTimeMillis(ARGameStatus.Instance().getStartTime()));
        quest.setStatus(QuestStatus.COMPLETED);

        // Forward user to the next Activity
        Intent intent = new Intent(activity, TreasureFoundActivity.class);
        intent.putExtra(Constant.TREASURE_KEY, targetTreasureUUID);
        activity.startActivity(intent);
        activity.finish();
    }

    /**
     * Shows a message to the user.
     *
     * @param message This String will be shown.
     */
    private void showToastInfo(String message) {
        toast.setText(message);
        toast.show();
    }

    /**
     * Shows a message to the user.
     *
     * @param message Id of R.string that should be displayed.
     */
    private void showToastInfo(int message) {
        toast.setText(message);
        toast.show();
    }

    /**
     * Returns the ARGem that is closest to given coordinates x,y.
     * Time: O(n), n is number of Gems in arGems
     *
     * @param x coordinate for x-axis
     * @param y coordinate for y-axis
     * @return closest ARGem or null if no close ARGem has been found.
     */
    private ARGem findClosestGem(double x, double y) {
        // Initialize to a large value such that we can be sure that an ARGem will be closer
        double closestDistance = 99999999;
        ARGem closestARGem = null;

        for (ARGem arGem : ARGameStatus.Instance().getARGems()) {
            double distance = arGem.euclideanDistanceTo(x, y);
            if (closestARGem == null || distance < closestDistance) {
                // We found an ARGem that is closer.
                closestARGem = arGem;
                closestDistance = distance;
            }
        }
        return closestARGem;
    }
}

