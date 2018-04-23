package ch.mbuehler.eth.mgis.treasurego;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by marcello on 23/04/18.
 */

public class ARGemFactory {

    List<String> arGemNames = Arrays.asList("Spinel",
            "Fireopal",
            "Apatite",
            "Zircon",
            "Moonstone",
            "Aquamarine");

    private String sampleRandomName(){
        int numberNames = arGemNames.size();
        int randomIndex = ThreadLocalRandom.current().nextInt(numberNames);
        return arGemNames.get(randomIndex);
    }

    ARGem getARGem(Location location){
        String name = sampleRandomName();
        return new ARGem(name, location);
    }

    HashMap<ARGem, Boolean> initializeRandomARGems(int n, Location center,
                                                   double distanceMin, double distanceMax,
                                                   double altitude){
        LocationSampler locationSampler = new LocationSampler(center, distanceMin, distanceMax, altitude);
        List<Location> arPointLocations = locationSampler.sampleLocations(n);
        Log.v("ALT", Double.toString(altitude));
        List<ARGem> arGemsList = new ArrayList<>();
        StringJoiner sj = new StringJoiner("],[", "[", "]");
        for(Location loc: arPointLocations){
            sj.add(String.format("%f,%f", loc.getLatitude(), loc.getLongitude()));
            ARGem newARGem = getARGem(loc);
            arGemsList.add(newARGem);
        }
        Log.v("Coords", sj.toString());

        // Initialize all ARGems as 'not found'
        HashMap<ARGem, Boolean> arGemsMap = new HashMap<>();
        for(ARGem gem: arGemsList){
            arGemsMap.put(gem, false);
        }
        return arGemsMap;
    }


}
