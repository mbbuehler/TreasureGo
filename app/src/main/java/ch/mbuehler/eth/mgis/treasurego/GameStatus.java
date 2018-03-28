package ch.mbuehler.eth.mgis.treasurego;

import android.content.Context;

import java.util.ArrayList;

/**
 * Singleton holding the current game status.
 */
public class GameStatus {

    /**
     * Singleton instance
     */
    private static GameStatus instance;

    /**
     * True if the Treasures have been loaded
     */
    private boolean hasBeenInitialized;

    /**
     * Holds all Treasures for one session
     */
    private ArrayList<Treasure> allTreasures;

    /**
     * List of the uuids of all Treasures that have already been discovered in this session.
     */
    private ArrayList<String> uuidTreasuresFound;
    /**
     * Quests
     */
    private ArrayList<Quest> quests;

    private GameStatus(){
        allTreasures = new ArrayList<>();
        uuidTreasuresFound = new ArrayList<>();
        quests = new ArrayList<>();
        hasBeenInitialized = false;
    }

    /**
     * Returns the unique instance of GameStatus
     * Synchronized is needed to make this method thread safe
     * @return
     */
    static synchronized GameStatus Instance(){
        if(instance == null){
            instance = new GameStatus();
        }
        return instance;
    }

    /**
     * Resets all data for this GameStatus.
     * @param context ApplicationContext
     */
    void reset(Context context){
        // Load all treasures from file
        ArrayList<Treasure> allTreasures = new TreasureLoader().loadTreasures(context);
        Instance().setAllTreasures(allTreasures);

        // Initialize as empty ArrayList
        Instance().uuidTreasuresFound = new ArrayList<>();
        Instance().setQuests(new ArrayList<Quest>());
    }

    public ArrayList<Treasure> getAllTreasures() {
        return Instance().allTreasures;
    }

    public void setAllTreasures(ArrayList<Treasure> allTreasures) {
        Instance().allTreasures = allTreasures;
    }

    /**
     * Returns all the uuids of the Treasures that have been found
     * @return
     */
    public  ArrayList<String> getUuidTreasuresFound() {
        return Instance().uuidTreasuresFound;
    }

    public void addUuidTreasuresFound(String uuid){
        Instance().uuidTreasuresFound.add(uuid);
    }

    public ArrayList<Quest> getQuests() {
        return Instance().quests;
    }

    public void setQuests(ArrayList<Quest> quests) {
        Instance().quests = quests;
    }

    public void addQuest(Quest quest){
        Instance().quests.add(quest);
    }

    public boolean hasBeenInitialized() {
        return Instance().hasBeenInitialized;
    }
    public void setHasBeenInitialized(boolean hasBeenInitialized) {
        Instance().hasBeenInitialized = hasBeenInitialized;
    }

    public Quest getLastQuest(){
        int nQuests = getQuests().size();
        if(nQuests > 0){
            return getQuests().get(nQuests - 1);
        }
        return null;
    }
}
