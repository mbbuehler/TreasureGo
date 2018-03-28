package ch.mbuehler.eth.mgis.treasurego;

/**
 * Created by marcello on 28/03/18.
 */

public class Quest {

    private Treasure treasure;
    private double avgSpeed;
    private float temperature;
    private QuestStatus status;

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

    public int getReward(){
        return RewardCalculator.calculateReward(getTreasure(), getAvgSpeed(), getTemperature());
    }
}
