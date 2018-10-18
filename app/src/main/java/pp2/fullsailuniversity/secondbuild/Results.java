package pp2.fullsailuniversity.secondbuild;


import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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
        ImageView gradeImage = findViewById(R.id.quizGrade);
        TextView resultsText = findViewById(R.id.resultsText);
        TextView criteriaText = findViewById(R.id.gradeCriteria);

        menu = findViewById(R.id.return_to_menu);
        retry = findViewById(R.id.retryGame);
        score = MainGameActivity.score.get();
        totalquestions = MainGameActivity.i.get();


        float points = ((float)score / (float)totalquestions);
        criteriaText.setText("Points Needed:\nA: " + (int)(totalquestions*7 + 1) + "\nB: " + (int)(totalquestions*6 + 1)+
                "\nC: " + (int)(totalquestions*4 + 1) + "\nD: " + (int)(totalquestions*2 + 1));
        if (points > 7.0f)
        {
            MediaPlayer cheerSound = MediaPlayer.create(this, R.raw.cheersound);
            cheerSound.start();
            gradeImage.setImageResource(R.drawable.grade_a);
            resultsText.setText("You passed the quiz with " + score + " points from " + totalquestions + " questions");
        }
        else if (points > 6.0f)
        {
            MediaPlayer cheerSound = MediaPlayer.create(this, R.raw.cheersound);
            cheerSound.start();
            gradeImage.setImageResource(R.drawable.grade_b);
            resultsText.setText("You passed the quiz with " + score + " points from " + totalquestions + " questions");
        }
        else if (points > 4.0f)
        {
            MediaPlayer cheerSound = MediaPlayer.create(this, R.raw.cheersound);
            cheerSound.start();
            gradeImage.setImageResource(R.drawable.grade_c);
            resultsText.setText("You passed the quiz with " + score + " points from " + totalquestions + " questions");
        }
        else if (points > 1.6f)
        {
            MediaPlayer cheerSound = MediaPlayer.create(this, R.raw.cheersound);
            cheerSound.start();
            gradeImage.setImageResource(R.drawable.grade_d);
            resultsText.setText("You barely passed the quiz with " + score + " points from " + totalquestions + " questions");
        }
        else{
                MediaPlayer booSound = MediaPlayer.create(this, R.raw.boo);
                booSound.start();
                gradeImage.setImageResource(R.drawable.grade_f);
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
            toMainGame.putExtra("myKey", 20);
            finish();
            startActivity(toMainGame);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);


        });
    }
}