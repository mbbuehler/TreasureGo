package ch.mbuehler.eth.mgis.treasurego;

import android.content.Intent;
import android.location.Location;
import android.text.TextUtils;
import java.util.UUID;


/**
 * This class represents a Treasure object.
 */
public class Treasure implements Serializable{

    /**
     * Make Treasure objects identifiable via a uuid
     */
    private String uuid = UUID.randomUUID().toString();

    /**
     * Name of the treasure, that will be displayed to the user
     */
    private String name = "n.a";
    /**
     * Maximum reward obtainable when finding with treasure
     */
    private int reward = 0;
    /**
     * Location of hidden treasure
     */
    private Location location;

    /**
     * Constructor for treasure
     * @param name name of the treasure
     * @param reward maximum reward for this treasure
     * @param location location of this treasure
     */
    Treasure(String name, int reward, Location location){
        this.setName(name);
        this.setReward(reward);
        this.location = location;
    }

    /**
     * Constructor that is fed with a serialized treasure.
     * See this.serialize for the encoding of the attributes.
     * @param serializedData serialized treasure data
     */
    Treasure(String serializedData){
        String[] data = serializedData.split(";");
        this.name = data[0];
        this.reward = Integer.parseInt(data[1]);
        this.location = new Location("");
        this.location.setLongitude(Double.parseDouble(data[2]));
        this.location.setLatitude(Double.parseDouble(data[3]));
        this.uuid = data[4];
    }

    String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        name.replace(";", " ");
        this.name = name;
    }

    public int getReward() {
        return reward;
    }

    /**
     * Sets the reward for this Treasure. If a negative value is provided, the reward is set to 0.
     * @param reward positive integer
     */
    public void setReward(int reward) {
        if (reward < 0){
            this.reward = 0;
        } else{
            this.reward = reward;
        }
    }

    Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Represents the Treasure using its name
     * @return String
     */
    public String toString(){
        return String.format("%s", this.name);
    }

    /**
     * Encodes object attributes as a String:
     * name,reward,longitude,latitude,uuid
     * i.e. "Fast Warhorse;80;8.5077616906;47.4090014008;8eccd1a3-3b10-4c7b-9bc1-3786d9d60dc5"
     * The delimiter is determined by the Serializable Interface.
     * @return String
     */
    public String serialize(){
        String[] data = new String[]{this.name, Integer.toString(this.reward), Double.toString(this.location.getLongitude()), Double.toString(this.location.getLatitude()), this.uuid};
        return TextUtils.join(Serializable.DELIMITER, data);
    }

    /**
     * Creates a new Treasure with data from Intent.
     * Key for StringExtra: MainActivity.TREASURE_KEY
     * @param intent with StringExtra
     * @return Treasure
     */
    static Treasure unserializeTreasureFromIntent(Intent intent) {
        String treasureSerialized = intent.getStringExtra(MainActivity.TREASURE_KEY);
        return new Treasure(treasureSerialized);
    }

}
