package ch.mbuehler.eth.mgis.treasurego;

import android.widget.TextView;

/**
 * Created by marcello on 23/04/18.
 */

public abstract class ViewUpdater {


    long getDeltaTimeMillis(long startTime){
        long deltaTimeMillis = System.currentTimeMillis() - startTime;
        return deltaTimeMillis;
    }

    protected String getFormattedTimeDifference(long deltaTimeMillis){
        int seconds = (int) (deltaTimeMillis / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;

        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

}
