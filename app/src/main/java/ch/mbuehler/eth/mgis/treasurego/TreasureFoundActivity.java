package ch.mbuehler.eth.mgis.treasurego;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * This activity is called when the user has found a treasure.
 */
public class TreasureFoundActivity extends AppCompatActivity {

    private Quest completedQuest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treasure_found);

        Intent intent = getIntent();
        String treasureUuid = intent.getStringExtra(MainActivity.TREASURE_KEY);
        completedQuest = GameStatus.Instance().getLastQuestForTreasureUuid(treasureUuid);

        updateView();
    }

    private void updateView(){
        TextView rewardView = findViewById(R.id.reward);
        String rewardText = String.format("+ %d coins", completedQuest.getReward());
        rewardView.setText(rewardText);

        TextView treasureNameView = findViewById(R.id.treasureName);
        String treasureNameText = completedQuest.getTreasure().getName();
        treasureNameView.setText(treasureNameText);

        TextView avgSpeedView = findViewById(R.id.averageSpeedValue);
        String avgSpeedText = Formatter.formatDouble(completedQuest.getAvgSpeed(), 1) + " m/s";
        avgSpeedView.setText(avgSpeedText);

        TextView temperatureView = findViewById(R.id.temperatureValue);
        String temperatureText = Formatter.formatDouble(completedQuest.getTemperature(), 1) + " \u2103";
        temperatureView.setText(temperatureText);
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
