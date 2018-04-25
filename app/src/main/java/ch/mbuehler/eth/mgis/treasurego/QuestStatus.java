package ch.mbuehler.eth.mgis.treasurego;

/**
 * Enum for the Status of Quests.
 */
public enum QuestStatus {
    /**
     * The user has reached a Location close to the targetLocation, but hasn't found all Gems
     */
    SEARCHING_GEMS,
    /**
     * The Quest has been completed
     */
    COMPLETED
}
