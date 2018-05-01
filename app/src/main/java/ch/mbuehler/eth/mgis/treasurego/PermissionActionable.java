package ch.mbuehler.eth.mgis.treasurego;

/**
 * Should be implemented by classes that handle permission requests.
 */
public interface PermissionActionable {
    /**
     * Will be called when permission has been granted.
     */
    void onPermissionGranted();

    /**
     * Will be called when permission was denied for the first time.
     */
    void onPermissionDenied();

    /**
     * Will be called when permission was denied twice.
     */
    void onPermissionDeniedTwice();
}
