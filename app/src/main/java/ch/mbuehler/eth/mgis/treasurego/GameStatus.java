package ch.mbuehler.eth.mgis.treasurego;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by marcello on 27/03/18.
 */

public class GameStatus {

    private static GameStatus instance;

    private boolean hasBeenInitialized;

    private ArrayList<Treasure> allTreasures;

    private ArrayList<String> uuidTreasuresFound;
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
    public static synchronized GameStatus Instance(){
        if(instance == null){
            instance = new GameStatus();
        }
        return instance;
    }

    public void reset(Context context){
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
