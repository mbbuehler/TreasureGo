package ch.mbuehler.eth.mgis.treasurego;

import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Set;


/**
 * OnTouchListener for the AROverlayView.
 */
public class AROverlayViewOnTouchListener implements View.OnTouchListener{

    /**
     * UUID of the target Treasure
     */
    private String targetTreasureUUID;
    /**
     * Context of active Activity
     */
    private Context context;
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
     * A Gem is considered as collected when the user touches within this distance (pixels)
     * around the center of the Gem.
     */
    private final double TOUCH_DISTANCE_THRESHOLD = 60;
    /**
     * Time in milliseconds when the user last touched the screen.
     * This variable is used to prevent triggering the onTouch Event too frequently.
     */
    private long lastTouch = 0;
    /**
     * This value is the delay after a TouchEvent until new TouchEvents are considered.
     */
    private final long DELAY_BETWEEN_TOUCHES = 200;  // ms


    AROverlayViewOnTouchListener(String targetTreasureUUID, Context context, Set<ARGem> arGems, Toast toast, ARViewUpdater viewUpdater) {
        this.targetTreasureUUID = targetTreasureUUID;
        this.context = context;
        this.arGems = arGems;
        this.toast = toast;
        this.viewUpdater = viewUpdater;
    }

    /**
     * The user has collected all gems. This method stores the results and forwards
     * the user to the TreasureFoundActivity
     */
    private void onAllGemsCollected(){
        // Update the current Quest
        Quest quest = GameStatus.Instance().getLastQuestForTreasureUuid(targetTreasureUUID);
        quest.setGemCollectionTimeMillis(viewUpdater.getDeltaTimeMillis(viewUpdater.getStartTime()));
        quest.setStatus(QuestStatus.COMPLETED);

        // Forward user to the next Activity
        Intent intent = new Intent(context, TreasureFoundActivity.class);
        intent.putExtra(Constant.TREASURE_KEY, targetTreasureUUID);
        context.startActivity(intent);

    }

    /**
     * The user has touched the screen. If she touched on a Gem, it will be collected.
     * If not, a Toast will show a message.
     * @param view
     * @param motionEvent
     * @return true if Event was handled and false if it was ignored.
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // This should be called in all cases.
        view.performClick();

        // Only take action if the last TouchEvent was not a too short time ago.
        long currentTime = System.currentTimeMillis();
        // Ignore touches when all Gems have been collected
        if(currentTime - lastTouch > DELAY_BETWEEN_TOUCHES && arGems.size() > 0) {
            lastTouch = currentTime;

            // Get touch coordinates
            double x = motionEvent.getX();
            double y = motionEvent.getY();

            // Find closest ARGem
            ARGem closestARGem = findClosestGem(x, y);
            double closestDistance = closestARGem.euclideanDistanceTo(x, y);

            // If the are close enough to the closest ARGem, then take action.
            if (closestDistance < TOUCH_DISTANCE_THRESHOLD) {
                // We can't collect this Gem again.
                arGems.remove(closestARGem);

                // Inform user about his success.
                String info = closestARGem.getName() + " " + context.getString(R.string.collected);
                showToastInfo(info);

                // Update TextView
                viewUpdater.updateARGemsNotFound(arGems.size());

                if (arGems.isEmpty()) {
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
        // We did ignore this event.
        return false;
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

        for (ARGem arGem : arGems) {
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
