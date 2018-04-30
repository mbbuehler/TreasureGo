package ch.mbuehler.eth.mgis.treasurego;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Singleton holding the current status for ARActivity. All views and listeners associated with
 * the ARActivity can access variables via this class.
 */
public class ARGameStatus {
        /**
         * Singleton instance
         */
        private static ch.mbuehler.eth.mgis.treasurego.ARGameStatus instance;


    Treasure targetTreasure;
    HashMap<ARGem, Boolean> arGems;


        /**
         * Returns the unique instance of GameStatus
         * Synchronized is needed to make this method thread safe
         *
         * @return
         */
        static synchronized ch.mbuehler.eth.mgis.treasurego.ARGameStatus Instance() {
            if (instance == null) {
                instance = new ch.mbuehler.eth.mgis.treasurego.ARGameStatus();
            }
            return instance;
        }

    public Treasure getTargetTreasure() {
        return targetTreasure;
    }

    public void setTargetTreasure(Treasure targetTreasure) {
        this.targetTreasure = targetTreasure;
    }

    public Set<ARGem> getARGemsSet(){
            return arGems.keySet();
    }

    public void removeARGem(ARGem arGem){
        arGems.remove(arGem);
    }

    public HashMap<ARGem, Boolean> getArGems() {
        return arGems;
    }

    public void setArGems(HashMap<ARGem, Boolean> arGems) {
        this.arGems = arGems;
    }
}
