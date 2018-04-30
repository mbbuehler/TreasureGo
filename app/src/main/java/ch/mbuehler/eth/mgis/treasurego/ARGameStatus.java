package ch.mbuehler.eth.mgis.treasurego;

import java.util.Set;

/**
 * Singleton holding the current status for ARActivity. All views and listeners associated with
 * the ARActivity can access variables via this class.
 */
class ARGameStatus {
    /**
     * Singleton instance
     */
    private static ch.mbuehler.eth.mgis.treasurego.ARGameStatus instance;

    /**
     * Treasure the user is searching for.
     */
    private Treasure targetTreasure;
    /**
     * This variable holds all ARGems the user has left to collect.
     */
    private Set<ARGem> arGems;
    /**
     * Keep track of the time since we started the Quest (needed for calculating reward)
     */
    private long startTime;


    /**
     * Returns the unique instance of GameStatus
     * Synchronized is needed to make this method thread safe
     *
     * @return Singleton instance of ARGameStatus
     */
    static synchronized ch.mbuehler.eth.mgis.treasurego.ARGameStatus Instance() {
        if (instance == null) {
            instance = new ch.mbuehler.eth.mgis.treasurego.ARGameStatus();
        }
        return instance;
    }

    private ARGameStatus() {
        startTime = System.currentTimeMillis();
    }

    Treasure getTargetTreasure() {
        return targetTreasure;
    }

    void setTargetTreasure(Treasure targetTreasure) {
        this.targetTreasure = targetTreasure;
    }

    Set<ARGem> getARGems() {
        return arGems;
    }

    /**
     * Removes the given arGem to found such that it won't be displayed any more.
     * @param arGem ARGem to remove
     */
    void removeARGem(ARGem arGem) {
        arGems.remove(arGem);
    }

    void setArGems(Set<ARGem> arGems) {
        this.arGems = arGems;
    }

    long getStartTime() {
        return startTime;
    }

    /**
     * Resets startTime.
     * Call this method whenever the user starts to search for ARGems.
     */
    void resetTime(){
        startTime = System.currentTimeMillis();
    }
}
