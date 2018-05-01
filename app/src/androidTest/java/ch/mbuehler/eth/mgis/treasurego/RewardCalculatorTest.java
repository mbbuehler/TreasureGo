package ch.mbuehler.eth.mgis.treasurego;

import android.content.Context;
import android.location.Location;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RewardCalculatorTest {
    @Test
    public void useAppContext() throws Exception {

        Treasure treasure = new Treasure("", 100, new Location(""));

        assertEquals(50, RewardCalculator.calculateReward(treasure, 60*1000));
        assertEquals(100, RewardCalculator.calculateReward(treasure, 30*1000));
        assertEquals(100, RewardCalculator.calculateReward(treasure, 0));
        assertEquals(75, RewardCalculator.calculateReward(treasure, 45*1000));


    }
}
