package ch.mbuehler.eth.mgis.treasurego;

import android.content.Intent;
import android.os.Bundle;
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
public class MainActivity extends AppCompatActivity {//implements AdapterView.OnItemSelectedListener{

    /**
     * Key that is used to pass data for a treasure to other Intents
     */
    public static final String TREASURE_KEY = "Treasure";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Do not reset the Treasures if we have already initalized them. This happens when we come
        // back to this Activity after finding a Treasure
        if(!GameStatus.Instance().hasBeenInitialized()){
            // The game has not been initialized. Initialize treasures now.
            resetTreasures();
            GameStatus.Instance().setHasBeenInitialized(true);
        } else{
            // The game has already been initialized. Update the view such that we can see which
            // Treasures we have already found.
            updateTreasureListview();
        }
        updateCurrentScore();

    }

    /**
     *
     * @param view
     */
    public void loadTreasures(View view){
        resetTreasures();
    }

    /**
     * Deletes all found and hidden treasures and reloads the provided treasures.
     *
     */
    public void resetTreasures(){
        // Delete found Treasures
        GameStatus.Instance().reset(getApplicationContext());
        // Reload View
        updateTreasureListview();
        Toast.makeText(getApplicationContext(), R.string.done, Toast.LENGTH_SHORT).show();
    }

    private void updateTreasureListview(){
        // We use a custom Adapter such that we can display each Treasure with name, reward and an image
        TreasureAdapter adapter = new TreasureAdapter(GameStatus.Instance().getAllTreasures(), GameStatus.Instance().getTreasureQuests(), getApplicationContext());

        // This view holds the Treasures to be selected
        final ListView treasureListView = findViewById(R.id.treasurelistview);

        // The adapter adds the data to the view
        treasureListView.setAdapter(adapter);

        // Set the listener that should wait for a user selection
        treasureListView.setOnItemClickListener(getOnTreasureSelectedListener());

        TextView scoreView = findViewById(R.id.scoreValue);
    }

    private void updateCurrentScore(){
        TextView scoreView = findViewById(R.id.scoreValue);
        String scoreText = String.format("%d coins", GameStatus.Instance().getTotalReward());
        scoreView.setText(scoreText);
    }

    /**
     * Returns an onItemClickListener. This listener specifies what should happen when the user has
     * selected a Treasure.
     * @return AdapterView.OnItemClickListener
     */
    public AdapterView.OnItemClickListener getOnTreasureSelectedListener(){
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            // This Listener is called when the user selects a Treasure from the list.
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
        };
        return listener;
    }
}
