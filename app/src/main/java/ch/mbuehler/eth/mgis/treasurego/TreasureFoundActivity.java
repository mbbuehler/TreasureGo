package ch.mbuehler.eth.mgis.treasurego;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity is called when the user has found a treasure.
 */
public class TreasureFoundActivity extends AppCompatActivity {

    /**
     * This objects holds information about the completed Quest
     * e.g. Treasure, averageSpeed, time, etc.
     */
    private Quest completedQuest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treasure_found);

        // Recover Treasure from Intent
        Intent intent = getIntent();
        String treasureUuid = intent.getStringExtra(Constant.TREASURE_KEY);
        // Retrieve the Quest object related to this treasure,
        // i.e. last saved Quest for this Treasure
        completedQuest = GameStatus.Instance().getLastQuestForTreasureUuid(treasureUuid);

        // Display information
        updateView();
    }

    /**
     * Updates all the fields displayed in the View
     */
    private void updateView() {
        TextView rewardView = findViewById(R.id.reward);
        String rewardText = String.format("+ %d %s", completedQuest.getReward(), getString(R.string.coins));
        rewardView.setText(rewardText);

        TextView treasureNameView = findViewById(R.id.treasureName);
        String treasureNameText = completedQuest.getTreasure().getName();
        treasureNameView.setText(treasureNameText);

        TextView arGemCollectionTimeTextView = findViewById(R.id.arGemCollectionTimeValue);
        String gemCollectionTimeText = Formatter.formatDouble(completedQuest.getGemCollectionTimeMillis() / 1000, 1) + getString(R.string.timeUnitSeconds);
        arGemCollectionTimeTextView.setText(gemCollectionTimeText);
    }

    /**
     * Closes the app
     *
     * @param view
     */
    public void closeApp(View view) {
        Toast.makeText(this, R.string.thanksBye, Toast.LENGTH_SHORT);
        // Solution to close app from Stackoverlow:
        // https://stackoverflow.com/questions/2092951/how-to-close-android-application
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("LOGOUT", true);
        startActivity(intent);

        finish();
    }

    /**
     * Redirects the user to MainActivity such that the game can be restarted with a new treasure.
     *
     * @param view
     */
    public void playAgain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
