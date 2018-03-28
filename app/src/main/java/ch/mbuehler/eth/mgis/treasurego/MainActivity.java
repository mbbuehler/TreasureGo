package ch.mbuehler.eth.mgis.treasurego;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    /**
     * hiddenTreasures holds all Treasures that have not been found.
     */
    private ArrayList<Treasure> hiddenTreasures;
    /**
     * foundTreasures holds all Treasures that have been found by the user.
     */
    private ArrayList<Treasure> foundTreasures;



    /**
     * Key that is used to pass data for a treasure to other Intents
     */
    public static final String TREASURE_KEY = "Treasure";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!GameStatus.Instance().hasBeenInitialized()){
            resetTreasures();
            GameStatus.Instance().setHasBeenInitialized(true);
        } else{
            updateTreasureListview();
        }


    }

    /**
     * This Listener is called when the user selects a Treasure from the list
     * @param adapterView
     * @param view
     * @param i
     * @param l
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(getApplicationContext(), "selected!", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // We can leave this empty
    }

    /**
     *
     * @param view
     */
    public void loadTreasures(View view){
        resetTreasures();
    }

    /**
     * Deletes all found and hidden treasures and reloads the provided treasures
     * this.foundTreasures is empty
     * this.hiddenTreasures contains all Treasures
     *
     */
    public void resetTreasures(){
        // Delete all previously found treasures from foundTreasures

//        this.foundTreasures = new ArrayList<>();

        // Initialize hiddenTreasures from CSV file
//        hiddenTreasures = new TreasureLoader().loadTreasures(getApplicationContext());
        GameStatus.Instance().reset(getApplicationContext());
        updateTreasureListview();
    }

    private void updateTreasureListview(){

//        ArrayAdapter<Treasure> adapter = new ArrayAdapter<Treasure>(this, R.layout.support_simple_spinner_dropdown_item, hiddenTreasures);

        TreasureAdapter adapter = new TreasureAdapter(GameStatus.Instance().getAllTreasures(), GameStatus.Instance().getUuidTreasuresFound(), getApplicationContext());

        GameStatus s = GameStatus.Instance();
        // treasureSpinner is a select that holds all hidden treasures
//        Spinner treasureSpinner = (Spinner) findViewById(R.id.treasurespinner);
        // Fill treasureSpinner with treasures
//        treasureSpinner.setAdapter(adapter);

        // calls this.onItemSelected(...) when the user selects a treasure
//        treasureSpinner.setOnItemSelectedListener(this);

        final ListView treasureListView = (ListView) findViewById(R.id.treasurelistview);
        treasureListView.setAdapter(adapter);

        treasureListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, CompassActivity.class);
                Treasure selectedTreasure = (Treasure) parent.getAdapter().getItem(position);
                String serializedTreasure = selectedTreasure.serialize();
                intent.putExtra(TREASURE_KEY, serializedTreasure);
                startActivity(intent);
            }
        });

        adapter.notifyDataSetChanged();

        TextView scoreView = findViewById(R.id.score);
    }



}
