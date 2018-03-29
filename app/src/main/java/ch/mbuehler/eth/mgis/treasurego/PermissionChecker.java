package ch.mbuehler.eth.mgis.treasurego;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is responsible for checking and requesting permissions.
 */
class PermissionChecker {

    /**
     * Make sure not to ask several times for permissions
     */
    private boolean hasUserDeniedPermissions = false;

    /**
     * Identifier used when requesting permissions. Can be any number.
     */
    static final int REQUEST_CODE_ASK_PERMISSION = 1;

    /**
     * Calling Activity
     */
    private Context context;

    /**
     * All required permissions
     */
    private static String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            // We don't need this because we store the CSV file with the app itself.
            // Uncomment this file if you want to read the CSV file from external storage.
//                Manifest.permission.READ_EXTERNAL_STORAGE
    };

    /**
     * @param context Activity. Make sure to pass the Activity as "this" (not getApplicationContext())
     */
    PermissionChecker(Context context) {
        this.context = context;
    }

    /**
     * Checks the required permissions and requests them if needed.
     * Required permissions:
     * - Manifest.permission.ACCESS_FINE_LOCATION
     * - Manifest.permission.ACCESS_COARSE_LOCATION
     */
    void checkPermissions() {
        // Holds permissions that we need to ask permissions for.
        final List<String> permissionsList = new ArrayList<>();
        // Add permissions that we don't have yet
        for (String permission : permissions) {
            addPermission(permissionsList, permission);
        }

        if (permissionsList.size() > 0) {
            // We have permissions to ask for
            ActivityCompat.requestPermissions((Activity) context, permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE_ASK_PERMISSION);
        }
    }


    /**
     * Checks if permission has been granted. If not the permission code is added to permissionList
     * https://inthecheesefactory.com/blog/things-you-need-to-know-about-android-m-permission-developer-edition/en
     *
     * @param permissionsList final List where we add permissions that have not been granted
     * @param permission      Manifest.permission.*
     * @return true if permission has been granted and false otherwise
     */
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            // Permission has not been granted. Add it to the list.
            permissionsList.add(permission);
        }
        return true;
    }

    /**
     * @return true if the user has already denied one permission
     */
    boolean isHasUserDeniedPermissions() {
        return hasUserDeniedPermissions;
    }

    /**
     * @param hasUserDeniedPermissions boolean
     */
    void setHasUserDeniedPermissions(boolean hasUserDeniedPermissions) {
        this.hasUserDeniedPermissions = hasUserDeniedPermissions;
    }

    /**
     * This method is called from CompassActivity
     * after the user has responded to a permission request
     *
     * @param requestCode  requestCode from requestPermissions()
     * @param permissions  not used
     * @param grantResults tells us if the user has granted permissions or not
     * @param activity     reference to CompassActivity
     */
    void handleRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults, CompassActivity activity) {

        switch (requestCode) {
            case PermissionChecker.REQUEST_CODE_ASK_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Thank the user for granting permissions
                    Toast.makeText(context, R.string.thanksHaveFun, Toast.LENGTH_SHORT).show();
                    activity.enableLocationUpdates();
                    setHasUserDeniedPermissions(false);
                } else if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_DENIED) {
                    // We did not get the permission.
                    // Memorize this such that we don't ask again right now.
                    setHasUserDeniedPermissions(true);
                }
                break;
            }
        }
    }
}
