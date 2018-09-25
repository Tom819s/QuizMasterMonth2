package pp2.fullsailuniversity.secondbuild;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class GameSetup extends AppCompatActivity {
    private TriviaDBURLcreator triviaURL;
    private RadioGroup categoriesRadio, difficultiesRadio;
    private NumberPicker numpicker;

    private static final String TAG = "GameSetup";

    public GameSetup() {
        triviaURL = new TriviaDBURLcreator();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_setup);

        categoriesRadio = findViewById(R.id.radioGroupCategory);
        difficultiesRadio = findViewById(R.id.radioGroupDifficulty);
        numpicker = findViewById(R.id.numQuestionPicker);
        numpicker.setMinValue(10);
        numpicker.setMaxValue(30);
        numpicker.setValue(10);


        RadioButton catAll = findViewById(R.id.radioAny);
        catAll.setTag(" ");
        RadioButton catFilm = findViewById(R.id.radioFilm);
        catFilm.setTag("Film");
        RadioButton catGeo = findViewById(R.id.radioGeo);
        catGeo.setTag("Geography");
        RadioButton catGK = findViewById(R.id.radioGK);
        catGK.setTag("General Knowledge");
        RadioButton catSci = findViewById(R.id.radioSci);
        catSci.setTag("Science");

        RadioButton diffEasy = findViewById(R.id.radioEasy);
        diffEasy.setTag("Easy");
        RadioButton diffMod = findViewById(R.id.radioMedium);
        diffMod.setTag("Medium");
        RadioButton diffHard = findViewById(R.id.radioHard);
        diffHard.setTag("Hard");
        RadioButton diffAll = findViewById(R.id.radioAnyDif);
        diffAll.setTag("ANY");
    }


    public void playButtonHandler(View view) {
        try {
            int catID = categoriesRadio.getCheckedRadioButtonId();
            triviaURL.mCategory = findViewById(catID).getTag().toString();
            int diffID = difficultiesRadio.getCheckedRadioButtonId();
            triviaURL.mDifficulty = findViewById(diffID).getTag().toString();
            triviaURL.mNumQuestions = numpicker.getValue();
            MainGameActivity.urlToAPI = triviaURL.createURL();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            Log.d(TAG, "playButtonHandler: io exception creating url");
        }
        Intent goToGame = new Intent(this, MainGameActivity.class);
        startActivity(goToGame);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}

