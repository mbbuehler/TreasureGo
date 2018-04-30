package ch.mbuehler.eth.mgis.treasurego;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Set;


/**
 * OnTouchListener for the AROverlayView.
 */
public class AROverlayViewOnTouchListener implements View.OnTouchListener{

    /**
     * Updates the Elements of the View that are not related to
     */
    private AROverlayViewUpdater viewUpdater;
    /**
     * Time in milliseconds when the user last touched the screen.
     * This variable is used to prevent triggering the onTouch Event too frequently.
     */
    private long lastTouch = 0;
    /**
     * This value is the delay after a TouchEvent until new TouchEvents are considered.
     */
    private final long DELAY_BETWEEN_TOUCHES = 200;  // ms

    AROverlayViewOnTouchListener(AROverlayViewUpdater viewUpdater) {
        this.viewUpdater = viewUpdater;

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
        if(currentTime - lastTouch > DELAY_BETWEEN_TOUCHES) {
            lastTouch = currentTime;

            // Get touch coordinates
            double x = motionEvent.getX();
            double y = motionEvent.getY();

            viewUpdater.onTouch(x, y);

        }
        // We did ignore this event.
        return false;
    }

}
