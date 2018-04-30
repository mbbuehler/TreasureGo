package ch.mbuehler.eth.mgis.treasurego;

import android.content.Intent;
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
import android.widget.Toast;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by marcello on 23/04/18.
 */

public class AROverlayViewUpdater extends ViewUpdater {

    ARActivity activity;
    long lastDrawUpdate = System.currentTimeMillis();
    long DELTA_DRAW_UPDATE = 1;
    /**
     * A Gem is considered as collected when the user touches within this distance (pixels)
     * around the center of the Gem.
     */
    private final double TOUCH_DISTANCE_THRESHOLD = 60;
    /**
     * We need access to displayed Toasts such that we can renew the content even when the old
     * Toast is still showing, so we use an instance variable for Toast.
     */
    private Toast toast;

    private TextView arGemsNotFoundTextView;

    /**
     * Keep track of the time since we started the Quest
     */
    private long startTime;

    HashMap<ARGem, ARGemLayout> arGemLayouts = new HashMap<>();

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    RelativeLayout arActivityView;

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
     * Updates TextView that shows the user how many Gems are left to find.
     * @param numberARGemsNotFound number of ARGems that have not been collected
     */
    void updateARGemsNotFound(int numberARGemsNotFound){
        String text = String.format("%d %s", numberARGemsNotFound, activity.getString(R.string.arGemsLeft));
        arGemsNotFoundTextView.setText(text);
    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * @param activity Activity whose View will be updated
     */
    AROverlayViewUpdater(ARActivity activity, Set<ARGem> arGems) {
        this.activity = activity;
        arActivityView = activity.findViewById(R.id.activity_ar);

        for(ARGem arGem: arGems){
            RelativeLayout layout = (RelativeLayout) View.inflate(activity, R.layout.image_gemview, null);
            ARGemLayout gemLayout = new ARGemLayout(layout);
            ((ImageView)gemLayout.layout.findViewById(R.id.image_gem)).setImageResource(arGem.getImageId());
            arGemLayouts.put(arGem, gemLayout);
            arActivityView.addView(layout, gemLayout.params);
        }

        arGemsNotFoundTextView = activity.findViewById(R.id.arGemsNotFound);

        toast = Toast.makeText(activity, "", Toast.LENGTH_SHORT);
        startTime = System.currentTimeMillis();

    }

    void updateOnDraw(Canvas canvas, Location currentLocation, float[] rotatedProjectionMatrix){
        if(System.currentTimeMillis() - lastDrawUpdate > DELTA_DRAW_UPDATE) {

            // variables for the point representation
            final int radius = 30;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            paint.setTextSize(60);

            // Transform the ARPoints coordinates from WGS84 to camera coordinates
            for (ARGem arGem : ARGameStatus.Instance().getARGemsSet()) {

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
                    ARGemLayout gemLayout = this.arGemLayouts.get(arGem);
                    gemLayout.updatePosition((int)x, (int)y);
                    gemLayout.layout.requestLayout();
                    arGem.setX(x);
                    arGem.setY(y);

                    canvas.drawCircle(x, y, radius, paint);
                    canvas.drawText(arGem.getName(), x - (30 * arGem.getName().length() / 2), y - 80, paint);
                }
            }
            lastDrawUpdate = System.currentTimeMillis();
        }
    }

    public boolean onTouch(double x, double y){
        if(ARGameStatus.Instance().getARGemsSet().size() > 0){
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
            updateARGemsNotFound(ARGameStatus.Instance().getARGemsSet().size());

            if (ARGameStatus.Instance().getARGemsSet().isEmpty()) {
                // The user has found all ARGems. We can continue.
                onAllGemsCollected();
                return true;
            }
        } else {
            // The user did not catch an ARGem, but touched somewhere else.
            showToastInfo(R.string.noGemFoundHere);
        }
        return true;
    }
        return false;
    }

    /**
     * The user has collected all gems. This method stores the results and forwards
     * the user to the TreasureFoundActivity
     */
    private void onAllGemsCollected(){
        String targetTreasureUUID = ARGameStatus.Instance().getTargetTreasure().getUuid();
        // Update the current Quest
        Quest quest = GameStatus.Instance().getLastQuestForTreasureUuid(targetTreasureUUID);
        quest.setGemCollectionTimeMillis(getDeltaTimeMillis(getStartTime()));
        quest.setStatus(QuestStatus.COMPLETED);

        // Forward user to the next Activity
        Intent intent = new Intent(activity, TreasureFoundActivity.class);
        intent.putExtra(Constant.TREASURE_KEY, targetTreasureUUID);
        activity.startActivity(intent);

    }

    /**
     * Shows a message to the user.
     * @param message This String will be shown.
     */
    private void showToastInfo(String message){
        toast.setText(message);
        toast.show();
    }

    /**
     * Shows a message to the user.
     * @param message Id of R.string that should be displayed.
     */
    private void showToastInfo(int message){
        toast.setText(message);
        toast.show();
    }

    /**
     * Returns the ARGem that is closest to given coordinates x,y.
     * Time: O(n), n is number of Gems in arGems
     * @param x coordinate for x-axis
     * @param y coordinate for y-axis
     * @return closest ARGem
     */
    private ARGem findClosestGem(double x, double y){
        double closestDistance = 99999999;
        ARGem closestARGem = null;

        for (ARGem arGem : ARGameStatus.Instance().getARGemsSet()) {
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

