package ch.mbuehler.eth.mgis.treasurego;

/**
 * Exception thrown when we did not find a Location.
 */
class LocationNotFoundException extends Exception {
    LocationNotFoundException(String message) {
        super(message);
    }
}
