package ch.mbuehler.eth.mgis.treasurego;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

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
//    private ArrayList<String> uuidTreasuresFound;

    /**
     * Contains data about previous Quests. For each Treasure we store all Quests that have
     * been finished for that Treasure
     * Key: uuid of Treasure
     * Value: ArrayList of Quest instances
     */
    private HashMap<String, ArrayList<Quest>> treasureQuests;


    private GameStatus(){
        allTreasures = new ArrayList<>();
        hasBeenInitialized = false;
        treasureQuests = new HashMap<>();
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
        Instance().treasureQuests = new HashMap<>();
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
    Set<String> getUuidTreasuresFound() {
        return Instance().treasureQuests.keySet();
    }

    /**
     * Adds a Quest to treasureQuest
     * If treasureQuest has no entry for the associated Treasure, a new entry is created.
     * If treasureQuest already has an entry for the associated Treasure, the provided Quest
     * is appended.
     * @param quest
     */
    void addQuest(Quest quest){
        String treasureKey = quest.getTreasure().getUuid();
        if(treasureQuests.get(treasureKey) == null){
            ArrayList<Quest> questList = new ArrayList<>();
            questList.add(quest);
            treasureQuests.put(treasureKey, questList);
        } else{
            treasureQuests.get(treasureKey).add(quest);
        }
    }

    /**
     * Returns the last Quest for the given Treasure UUID. If no such Quest exists,
     * this method returns null.
     * @param uuid uuid of a Treasure
     * @return Quest or null
     */
    public Quest getLastQuestForTreasureUuid(String uuid){
        Quest lastQuest;
        if(treasureQuests.containsKey(uuid) && treasureQuests.get(uuid).size() > 0){
            // The associated ArrayList contains at least one entry.
            // We return the last entry.
            ArrayList<Quest> questList = treasureQuests.get(uuid);
            lastQuest = questList.get(questList.size() - 1);
        } else {
            // There is no entry. We cannot return a Quest instance
            lastQuest = null;
        }
        return lastQuest;
    }

    boolean hasBeenInitialized() {
        return Instance().hasBeenInitialized;
    }
    void setHasBeenInitialized(boolean hasBeenInitialized) {
        Instance().hasBeenInitialized = hasBeenInitialized;
    }


}
