package ch.mbuehler.eth.mgis.treasurego;

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
     * Make sure not to ask several times (max 2 times) for permissions
     */
    private boolean hasUserDeniedPermissions = false;
    private boolean hasUserDeniedPermissionsTwice = false;

    /**
     * Identifier used when requesting permissions. Can be any number.
     */
    static final int REQUEST_CODE_ASK_PERMISSION = 1;

    /**
     * Calling Activity
     */
    private Context context;

    /**
     *  Holds permissions that we need to ask permissions for.
     */
    private List<String> permissionsList = new ArrayList<>();

    /**
     * @param context Activity. Make sure to pass the Activity as "this" (not getApplicationContext())
     */
    PermissionChecker(Context context) {
        this.context = context;
    }

    /**
     * Checks the required permissions and requests them if needed.
     * @param permissions String[] with Manifest.permission.*
     *                    e.g. [Manifest.permission.ACCESS_FINE_LOCATION]
     */
    void checkPermissions(String[] permissions) {
//        permissionsList = new ArrayList<>();
        // Add permissions that we don't have yet
        for (String permission : permissions) {
            addPermission(permission);
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
     * @param permission      Manifest.permission.*
     * @return true if permission has been granted and false otherwise
     */
    private boolean addPermission(String permission) {
        if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            // Permission has not been granted. Add it to the list.
            permissionsList.add(permission);
        }
        return true;
    }

    /**
     * @return true if the user has already denied one permission
     */
    boolean hasUserDeniedPermissions() {
        return hasUserDeniedPermissions;
    }

    /**
     * @param hasUserDeniedPermissions boolean
     */
    void setHasUserDeniedPermissions(boolean hasUserDeniedPermissions) {
        this.hasUserDeniedPermissions = hasUserDeniedPermissions;
    }

    /**
     * @return true if the user has already denied permissions twice
     */
    boolean hasUserDeniedPermissionsTwice() {
        return hasUserDeniedPermissionsTwice;
    }

    /**
     * @param hasUserDeniedPermissionsTwice boolean
     */
    void setHasUserDeniedPermissionsTwice(boolean hasUserDeniedPermissionsTwice) {
        this.hasUserDeniedPermissionsTwice = hasUserDeniedPermissionsTwice;
    }

    /**
     * This method is called from CompassActivity
     * after the user has responded to a permission request
     *
     * @param requestCode  requestCode from requestPermissions()
     * @param permissions  not used
     * @param grantResults tells us if the user has granted permissions or not
     * @param actionable reference to PermissionActionable specifying
     *                   what to to in case permissions have been granted / denied
     */
    void handleRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults, PermissionActionable actionable) {

        switch (requestCode) {
            case PermissionChecker.REQUEST_CODE_ASK_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Thank the user for granting permissions
                    Toast.makeText(context, R.string.thanksHaveFun, Toast.LENGTH_SHORT).show();
                    actionable.onPermissionGranted();
                    setHasUserDeniedPermissions(false);
                    setHasUserDeniedPermissionsTwice(false);
                } else if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_DENIED) {
                    // We did not get the permission.
                    // Memorize this such that we don't ask again right now.
                    if(!hasUserDeniedPermissions()){
                        // It was the first time we asked
                        setHasUserDeniedPermissions(true);
                        actionable.onPermissionDenied();
                    }else if(hasUserDeniedPermissions() && !hasUserDeniedPermissionsTwice()){
                        // The user has denied permissions once. Ask once more
                        setHasUserDeniedPermissionsTwice(true);
                        actionable.onPermissionDeniedTwice();
                    }
                }
                break;
            }
        }
    }
}
