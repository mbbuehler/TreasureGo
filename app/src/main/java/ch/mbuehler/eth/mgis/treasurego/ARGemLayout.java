package ch.mbuehler.eth.mgis.treasurego;

import android.app.Activity;
import android.widget.RelativeLayout;

/**
 * Holds the layout for one ARGem
 */
class ARGemLayout {
    RelativeLayout layout;
    RelativeLayout.LayoutParams params;

    ARGemLayout(RelativeLayout layout) {
        this.layout = layout;
        this.params = new RelativeLayout.LayoutParams(R.dimen.gem_width, R.dimen.gem_height);
        // Initialize margins with large negative values in order to hide them. After the first
        // update the Gem will be repositioned, i.e. margins are set to their correct values.
        this.params.leftMargin = -1000;
        this.params.rightMargin = -1000;
    }

    /**
     * Updates the position of the current ARGem. x, y are the new center coordinates.
     *
     * @param x        px
     * @param y        px
     * @param activity Context for querying resources
     */
    void updatePosition(int x, int y, Activity activity) {
        this.params.leftMargin = x - ((int) activity.getResources().getDimension(R.dimen.gem_width) / 2);
        this.params.topMargin = y - ((int) activity.getResources().getDimension(R.dimen.gem_width) / 2);
    }
}