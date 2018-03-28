package ch.mbuehler.eth.mgis.treasurego;

/**
 * Created by marcello on 16/03/18.
 */

interface Serializable {

    /**
     * Delimiter used to differentiate attribute values.
     */
    CharSequence DELIMITER = ";";

    /**
     * Serializes relevant attributes of the class and returns a String containing the attribute values.
     * @return String
     */
    String serialize();

}
