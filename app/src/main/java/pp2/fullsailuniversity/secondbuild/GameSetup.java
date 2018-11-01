package pp2.fullsailuniversity.secondbuild;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import java.util.concurrent.atomic.AtomicInteger;

public class GameSetup extends AppCompatActivity {
    private TriviaDBURLcreator triviaURL;
    private RadioGroup categoriesRadio, difficultiesRadio, typeRadio;
    private NumberPicker numpicker, timepicker;
    private Switch hintsOn;
    private MediaPlayer menuMusic;

    private static final String TAG = "GameSetup";

    public GameSetup() {
        triviaURL = new TriviaDBURLcreator();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_game_setup);
        menuMusic = MediaPlayer.create(GameSetup.this, R.raw.menuloop);
        categoriesRadio = findViewById(R.id.radioGroupCategory);
        difficultiesRadio = findViewById(R.id.radioGroupDifficulty);
        typeRadio = findViewById(R.id.radioGroupType);
        numpicker = findViewById(R.id.numQuestionPicker);
        numpicker.setMinValue(10);
        numpicker.setMaxValue(30);
        numpicker.setValue(10);

        timepicker = findViewById(R.id.timePicker);
        timepicker.setMinValue(10);
        timepicker.setMaxValue(30);
        timepicker.setValue(20);

        hintsOn = findViewById(R.id.switch1);


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

        RadioButton typeAll = findViewById(R.id.wantBoth);
        typeAll.setTag("");
        RadioButton typeMC = findViewById(R.id.wantMC);
        typeMC.setTag("multiple");
        RadioButton typeTF = findViewById(R.id.wantTF);
        typeTF.setTag("boolean");
    }

    @Override
    public void onBackPressed() {

        Intent results = new Intent(this, MainMenu.class);
        finish();
        startActivity(results);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (menuMusic.isPlaying())
            menuMusic.stop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!menuMusic.isPlaying()){
            menuMusic = MediaPlayer.create(GameSetup.this, R.raw.menuloop);
            menuMusic.setLooping(true);
            menuMusic.start();}
    }
    public void playButtonHandler(View view) {
        try {
            int catID = categoriesRadio.getCheckedRadioButtonId();
            triviaURL.mCategory = findViewById(catID).getTag().toString();
            int typeID = typeRadio.getCheckedRadioButtonId();
            triviaURL.mType = findViewById(typeID).getTag().toString();
            int diffID = difficultiesRadio.getCheckedRadioButtonId();
            triviaURL.mDifficulty = findViewById(diffID).getTag().toString();
            triviaURL.mNumQuestions = numpicker.getValue();
            MainGameActivity.urlToAPI = triviaURL.createURL();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            Log.d(TAG, "playButtonHandler: io exception creating url");
        }
        if (hintsOn.isChecked())
            MainGameActivity.hints = true;
        else
            MainGameActivity.hints = false;
        Intent goToGame = new Intent(this, MainGameActivity.class);
        goToGame.putExtra("myKey", timepicker.getValue());
        startActivity(goToGame);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}

