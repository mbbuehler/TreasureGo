package ch.mbuehler.eth.mgis.treasurego;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/**
 * This activity is called when the user has found a treasure.
 */
public class TreasureFoundActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treasure_found);
    }

    /**
     * Closes the app
     * @param view
     */
    public void closeApp(View view){
        // TODO: say thanks and goodbye
        System.exit(0);
    }

    /**
     * Redirects the user to MainActivity such that the game can be restarted with a new treasure.
     * @param view
     */
    public void playAgain(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
