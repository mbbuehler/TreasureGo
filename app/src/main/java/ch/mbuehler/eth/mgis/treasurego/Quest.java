package ch.mbuehler.eth.mgis.treasurego;

/**
 * This class holds data about Quests.
 * A Quest is a subgame, where the user searches for one Treasure.
 */
public class Quest {

    /**
     * Target Treasure
     */
    private Treasure treasure;
    /**
     * The average speed in m/s
     */
    private double avgSpeed;
    /**
     * Quest status, e.g. COMPLETED
     */
    private QuestStatus status;

    private double gemCollectionTimeMillis = -1;

    /**
     * @param treasure    Target Treasure
     * @param status      COMPLETED
     */
    public Quest(Treasure treasure, QuestStatus status) {
        this.treasure = treasure;
        this.status = status;
    }

    public Treasure getTreasure() {
        return treasure;
    }

    public void setTreasure(Treasure treasure) {
        this.treasure = treasure;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public void setStatus(QuestStatus status) {
        this.status = status;
    }


    public double getGemCollectionTimeMillis() {
        if(gemCollectionTimeMillis > 0){
            return gemCollectionTimeMillis;
        }
        return -1;
    }

    public void setGemCollectionTimeMillis(double gemCollectionTimeMillis) {
        this.gemCollectionTimeMillis = gemCollectionTimeMillis;
    }

    /**
     * Calculates the reward achieved for this Quest
     *
     * @return int number of coins received
     */
    public int getReward() {
        return RewardCalculator.calculateReward(getTreasure(), getGemCollectionTimeMillis());
    }
}
