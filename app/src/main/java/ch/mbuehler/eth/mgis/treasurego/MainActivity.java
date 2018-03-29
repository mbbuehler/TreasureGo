package ch.mbuehler.eth.mgis.treasurego;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The MainActivity welcomes the user and shows a list of Treasure objects. The user can select
 * a Treasure and start searching.
 */
public class MainActivity extends AppCompatActivity implements PermissionActionable {//implements AdapterView.OnItemSelectedListener{

    /**
     * Key that is used to pass data for a treasure to other Intents
     */
    public static final String TREASURE_KEY = "Treasure";

    /**
     * Indicate when the user has selected a Treasure. This prevents that
     * more than one CompassActivity is created.
     */
    private boolean userHasTreasureSelected = false;

    /**
     * Required permissions for this class
     */
    String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    /**
     * Class for checking permissions
     */
    PermissionChecker permissionChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionChecker = new PermissionChecker(this);
        // Ask for permissions if the user has not already denied that twice
        permissionChecker.checkPermissions(permissions);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Do not reset the Treasures if we have already initalized them. This happens when we come
        // back to this Activity after finding a Treasure
        if (!GameStatus.Instance().hasBeenInitialized()) {
            // The game has not been initialized. Initialize treasures now.
            resetTreasures();
            GameStatus.Instance().setHasBeenInitialized(true);
        } else {
            // The game has already been initialized. Update the view such that we can see which
            // Treasures we have already found.
            updateTreasureListview();
        }
        updateCurrentScore();
    }

    /**
     * This method is called after the user has responded to a permission request
     *
     * @param requestCode  requestCode from requestPermissions()
     * @param permissions  not used
     * @param grantResults tells us if the user has granted permissions or not
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults) {
        // permissionChecker handles this.
        // See method documentation of handleRequestPermissionsResult(...) for more information.
        permissionChecker.handleRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public void onPermissionGranted() {
        Toast.makeText(this, R.string.thanksHaveFun, Toast.LENGTH_SHORT).show();
    }

    /**
     * The user has denied permissions. Inform about consequences and ask again.
     */
    public void onPermissionDenied() {
        // Tell the user what might happen if we run it without permissions.
        Toast.makeText(this, R.string.onPermissionsDeniedOnce, Toast.LENGTH_LONG).show();
        // Ask again after a few seconds
        Handler askAgainHandler = new Handler();
        askAgainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                permissionChecker.checkPermissions(permissions);
            }
        }, 3000);
        // It was the first time we ask the user. Give him one more chance.}
    }

    /**
     * The user has denied twice. Show message.
     */
    @Override
    public void onPermissionDeniedTwice() {
        Toast.makeText(this, R.string.onPermissionsDeniedTwice, Toast.LENGTH_LONG).show();
        // We cannot run the app without permissions. Show message and close app after a few seconds.
        Handler askAgainHandler = new Handler();
        askAgainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 3000);
    }

    /**
     * OnClickListener. Is executed when the user presses the reset Button.
     *
     * @param view
     */
    public void loadTreasures(View view) {
        resetTreasures();
    }

    /**
     * Deletes all found and hidden treasures and reloads the provided treasures.
     */
    public void resetTreasures() {
        // Delete found Treasures
        GameStatus.Instance().reset(getApplicationContext());
        // Reload View
        updateTreasureListview();
        if (GameStatus.Instance().hasBeenInitialized()) {
            // Don't display Toast if we are loading the Treasures for the first time
            Toast.makeText(getApplicationContext(), R.string.done, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTreasureListview() {
        // We use a custom Adapter such that we can display each Treasure with name, reward and an image
        TreasureAdapter adapter = new TreasureAdapter(GameStatus.Instance().getAllTreasures(), GameStatus.Instance().getTreasureQuests(), getApplicationContext());

        // This view holds the Treasures to be selected
        final ListView treasureListView = findViewById(R.id.treasurelistview);

        // The adapter adds the data to the view
        treasureListView.setAdapter(adapter);

        // Set the listener that should wait for a user selection
        treasureListView.setOnItemClickListener(getOnTreasureSelectedListener());
    }

    private void updateCurrentScore() {
        TextView scoreView = findViewById(R.id.scoreValue);
        String scoreText = String.format("%d coins", GameStatus.Instance().getTotalReward());
        scoreView.setText(scoreText);
    }

    /**
     * Returns an onItemClickListener. This listener specifies what should happen when the user has
     * selected a Treasure.
     *
     * @return AdapterView.OnItemClickListener
     */
    public AdapterView.OnItemClickListener getOnTreasureSelectedListener() {
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            // This Listener is called when the user selects a Treasure from the list.
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // If we do not check here, we might end up
                // creating several CompassActivity instances
                if (!userHasTreasureSelected) {
                    userHasTreasureSelected = true;

                    // We want to go to the CompassActivity
                    Intent intent = new Intent(MainActivity.this, CompassActivity.class);

                    // Fetch the target Treasure from the list.
                    Treasure selectedTreasure = (Treasure) parent.getAdapter().getItem(position);
                    // Serialize and send the target Treasure with the Intent.
                    String serializedTreasure = selectedTreasure.serialize();
                    intent.putExtra(TREASURE_KEY, serializedTreasure);
                    // Starting the CompassActivity.
                    startActivity(intent);
                }
            }
        };
        return listener;
    }
}
