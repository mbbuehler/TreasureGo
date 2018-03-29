package ch.mbuehler.eth.mgis.treasurego;

/**
 * Custom interface for Serializable classes.
 * Implemented by classes passing data between Intents.
 */
interface Serializable {

    /**
     * Delimiter used to differentiate attribute values.
     */
    CharSequence DELIMITER = ";";

    /**
     * Serializes relevant attributes of the class and returns a String containing the attribute values.
     *
     * @return String
     */
    String serialize();

}
