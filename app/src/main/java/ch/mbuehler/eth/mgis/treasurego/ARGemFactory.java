package ch.mbuehler.eth.mgis.treasurego;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This is a Factory class that creates ARGems.
 */
class ARGemFactory {

    /**
     * HashMap containing name and resource id for the corresonding image.
     * See Constructor for values.
     */
    private HashMap<String, Integer> gemData;

    ARGemFactory() {
        gemData = new HashMap<>();
        gemData.put("Fireopal", R.drawable.fireopal_small);
        gemData.put("Apatite", R.drawable.apatite_small);
        gemData.put("Zircon", R.drawable.zircon_small);
        gemData.put("Moonstone", R.drawable.moonstone_small);
        gemData.put("Aquamarine", R.drawable.aquamarine_small);
    }

    /**
     * Samples a random ARGem name.
     * @return a random name
     */
    private String sampleRandomName(){
        int numberNames = gemData.keySet().size();
        int randomIndex = ThreadLocalRandom.current().nextInt(numberNames);
        return (String)gemData.keySet().toArray()[randomIndex];
    }

    /**
     * @param location Location where the ARGem should be placed.
     * @return a randomly sampled ARGem that is placed close to the given Location.
     */
    private ARGem getARGem(Location location){
        String name = sampleRandomName();
        int imageId = gemData.get(name);
        return new ARGem(name, location, imageId);
    }

    /**
     * Creates and returns n ARGems, initialized within the given distance bounds around
     * the center Location.
     * @param n number of ARGems to return
     * @param center center Location
     * @param distanceMin ARGems should not be closer to center than distanceMin
     * @param distanceMax ARGems should not be further away to center than distanceMax
     * @param altitude approximate altitude of ARGem
     * @return n ARGems
     */
    Set<ARGem> initializeRandomARGems(int n, Location center,
                                      double distanceMin, double distanceMax,
                                      double altitude){
        // Sample Locations
        LocationSampler locationSampler = new LocationSampler(center, distanceMin, distanceMax, altitude);
        List<Location> arPointLocations = locationSampler.sampleLocations(n);

        // Sample ARGems
        Set<ARGem> arGemsSet = new HashSet<>();
        for(Location location: arPointLocations){
            arGemsSet.add(getARGem(location));
        }
        return arGemsSet;
    }
}
