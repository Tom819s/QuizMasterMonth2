package pp2.fullsailuniversity.secondbuild;


import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Results extends AppCompatActivity {
    public static int score, totalquestions;
    Button menu, retry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results_screen);
        ImageView passedImage = findViewById(R.id.passedimage);
        ImageView failedImage = findViewById(R.id.failedimage);
        TextView resultsText = findViewById(R.id.resultsText);

        menu = findViewById(R.id.return_to_menu);
        retry = findViewById(R.id.retryGame);
        score = MainGameActivity.score.get();
        totalquestions = MainGameActivity.i.get();


        int points = (score / totalquestions);

        if (points > 4)
        {
            MediaPlayer cheerSound = MediaPlayer.create(this, R.raw.cheersound);
            cheerSound.start();
            passedImage.setAlpha(1.0f);
            failedImage.setAlpha(0.0f);
            resultsText.setText("You passed the quiz with " + score + " points from " + totalquestions + " questions");
        }
        else
        {
            MediaPlayer booSound = MediaPlayer.create(this, R.raw.boo);
            booSound.start();
            passedImage.setAlpha(0.0f);
            failedImage.setAlpha(1.0f);
            resultsText.setText("You failed the quiz with " + score + " points from " + totalquestions + " questions");
        }
        menu.setOnClickListener((View) ->
        {
            Intent toMainMenu = new Intent(Results.this, MainMenu.class);
            finish();
            startActivity(toMainMenu);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);


        });
        retry.setOnClickListener((View) ->
        {
            Intent toMainGame = new Intent(Results.this, MainGameActivity.class);
            finish();
            startActivity(toMainGame);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);


        });
    }
}