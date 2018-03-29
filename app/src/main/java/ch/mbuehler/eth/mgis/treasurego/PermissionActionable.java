package ch.mbuehler.eth.mgis.treasurego;

/**
 * Created by marcello on 29/03/18.
 */

public interface PermissionActionable {

    void onPermissionGranted();

    void onPermissionDenied();

    void onPermissionDeniedTwice();
}
