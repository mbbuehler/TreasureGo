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
     * Temperature in degrees Celsius
     */
    private float temperature;
    /**
     * Quest status, e.g. COMPLETED
     */
    private QuestStatus status;

    /**
     * @param treasure Target Treasure
     * @param avgSpeed average speed achieved in this Quest
     * @param temperature degrees Celsius
     * @param status COMPLETED
     */
    public Quest(Treasure treasure, double avgSpeed, float temperature, QuestStatus status) {
        this.treasure = treasure;
        this.avgSpeed = avgSpeed;
        this.temperature = temperature;
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

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public void setStatus(QuestStatus status) {
        this.status = status;
    }

    /**
     * Calculates the reward achieved for this Quest
     * @return int number of coins received
     */
    public int getReward(){
        return RewardCalculator.calculateReward(getTreasure(), getAvgSpeed(), getTemperature());
    }
}
