package pp2.fullsailuniversity.secondbuild;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class multiResults extends AppCompatActivity
{
    private final static String TAG = "multiplayerResults";
    private MediaPlayer sound;
    private Button returntomenu, retrygame;
    private TextView resultsText, criteriaText;
    private int score, enemyscore;
    private String enemyName;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate: starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multi_results);
        returntomenu = findViewById(R.id.return_to_menu_multi);
        retrygame = findViewById(R.id.retryGame_multi);

        resultsText = findViewById(R.id.resultsTextMulti);
        criteriaText = findViewById(R.id.gradeCriteriaMulti);

        Bundle bundle = getIntent().getExtras();
        String[] gameResults = bundle.getStringArray("gameResults");
        if (gameResults != null)
        {
            enemyName = gameResults[2];
            enemyscore = Integer.parseInt(gameResults[3]);
            score = Integer.parseInt(gameResults[1]);
        }

        if (score > enemyscore)
        {
            resultsText.setText("You Win!");
            sound = MediaPlayer.create(getApplicationContext(), R.raw.cheersound);
            sound.start();

            criteriaText.setText("You: " + score + "\n" + enemyName + ": " + enemyscore);

        }
        else
        {
            resultsText.setText(enemyName + " Wins, you lost");
            criteriaText.setText(enemyName + ": " + enemyscore + "\nYou: " + score) ;
            sound = MediaPlayer.create(getApplicationContext(), R.raw.boo);
            sound.start();
        }


        returntomenu.setOnClickListener((View) ->
        {
            Intent toMainMenu = new Intent(multiResults.this, MainMenu.class);
            finish();
            startActivity(toMainMenu);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);


        });

        retrygame.setOnClickListener((View) ->
        {
            Intent toMainGame = new Intent(multiResults.this, multiplayerSetupMatch.class);
            finish();
            startActivity(toMainGame);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);


        });
    }

    @Override
    public void onBackPressed()
    {

        Intent host = new Intent(multiResults.this, MainMenu.class);
        finish();
        startActivity(host);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

    }





}
