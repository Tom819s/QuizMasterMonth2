package pp2.fullsailuniversity.secondbuild;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.media.MediaPlayer;
import android.os.CountDownTimer;


public class MultiplayerGame extends AppCompatActivity implements GetTriviaJSONData.OnDataAvailable {
    private static final String TAG = "MainActivityGame";


    public static List<QuizQuestion> quiz;
    public static AtomicInteger i, score;
    public static String urlToAPI;
    private long millisToAnswer;
    private TextView question;
    private TextView timerText;
    private TextView scorecounter;
    private Button next,
            exit,
            b1, b2, b3, b4;
    private ImageButton startbtn;
    private CountDownTimer gameTimer, timesup, answerTimer;
    private MediaPlayer correctSound, wrongSound, tickingSound, alarm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_game);

        i = new AtomicInteger();
        score = new AtomicInteger(0);

        ProgressBar pbar = findViewById(R.id.progressBar);
        b1 = findViewById(R.id.button1);
        b2 = findViewById(R.id.button2);
        b3 = findViewById(R.id.button3);
        b4 = findViewById(R.id.button4);
        scorecounter = findViewById(R.id.score);
        question = findViewById(R.id.userEmail);
        next = findViewById(R.id.nextB);
        exit = findViewById(R.id.exitB);
        startbtn = findViewById(R.id.start_button);
        timerText = findViewById(R.id.timerTextView);


        correctSound = MediaPlayer.create(MultiplayerGame.this, R.raw.correct);
        wrongSound = MediaPlayer.create(MultiplayerGame.this, R.raw.wrong);
        tickingSound = MediaPlayer.create(MultiplayerGame.this, R.raw.tickingclock);
        alarm = MediaPlayer.create(MultiplayerGame.this, R.raw.alarmringing);

        scorecounter.setText("0");
        timerText.setText(" ");
        question.setText("");
        next.setEnabled(false);
        b1.setClickable(false);
        b1.setAlpha(0.0f);
        b2.setClickable(false);
        b2.setAlpha(0.0f);
        b3.setClickable(false);
        b3.setAlpha(0.0f);
        b4.setClickable(false);
        b4.setAlpha(0.0f);

        GetTriviaJSONData getTriviaJSONData = new GetTriviaJSONData(this, urlToAPI);
        getTriviaJSONData.execute(urlToAPI);


        gameTimer = new CountDownTimer(20000, 250) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "attempting to start game..");
                startbtn.callOnClick();
            }

            @Override
            public void onFinish() {

                Context context = getApplicationContext();
                CharSequence text = "Problem Loading";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }.start();


        startbtn.setOnClickListener(v ->
                {

                    if (quiz != null && quiz.size() > 0) {
                        pbar.setAlpha(0.0f);
                        pbar.setClickable(false);
                        startbtn.setClickable(false);
                        startbtn.setAlpha(0.0f);
                        GameLoop(0);
                    }
                }
        );

        exit.setOnClickListener((view) ->
        {
            leaveGame();
        });

        Log.d(TAG, "onCreate: ends");
//Setting of Q/A and Tags
    }

    @Override
    public void onBackPressed() {

        leaveGame();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume starts");
        super.onResume();
        Log.d(TAG, "onResume ends");
    }

    @Override
    public void onDataAvailable(List<QuizQuestion> data, DownloadStatus status) {
        if (status == DownloadStatus.OK) {
            Log.d(TAG, "onDataAvailable: data is " + data);
            quiz = data;
        } else {
            // download or processing failed
            Log.e(TAG, "onDataAvailable failed with status " + status);
        }
    }

    public void GameLoop(int index) {


        if (tickingSound.isPlaying()) {
            tickingSound.stop();
        }

        if (alarm.isPlaying()) {
            alarm.stop();
        }

        gameTimer.cancel();

        if (quiz != null && index < quiz.size()) {
            tickingSound.stop();
            tickingSound.release();
            tickingSound = MediaPlayer.create(MultiplayerGame.this, R.raw.tickingclock);

            List<Button> buttons = new ArrayList<Button>();
            buttons.add(b1);
            buttons.add(b2);
            buttons.add(b3);
            buttons.add(b4);


            gameTimer = new CountDownTimer(21000, 1000) {

                public void onTick(long millisUntilFinished) {
                    String count = Long.toString(millisUntilFinished / 1000);
                    millisToAnswer = 21000 - millisUntilFinished;
                    if (millisUntilFinished / 1000 > 10) {
                        timerText.setTextColor(Color.rgb(0, 204, 0));
                    } else if (millisUntilFinished / 1000 > 5) {
                        timerText.setTextColor(Color.rgb(255, 204, 0));
                    } else {
                        if (!tickingSound.isPlaying()) {
                            tickingSound.setVolume(5.0f, 5.0f);
                            tickingSound.start();
                            buttons.get(0).setEnabled(false);
                            buttons.get(1).setEnabled(false);
                        }
                        timerText.setTextColor(Color.rgb(204, 0, 0));
                    }
                    timerText.setText(count);
                }

                public void onFinish () {
                    tickingSound.stop();
                    alarm.setVolume(5.0f, 5.0f);
                    alarm.start();
                    timerText.setText("Time's Up!");
                    b1.setEnabled(false);
                    b2.setEnabled(false);
                    b3.setEnabled(false);
                    b4.setEnabled(false);
                    next.setEnabled(true);

                    if (b1.getTag() == "true")
                        b1.setBackgroundColor(Color.GREEN);
                    else if (b2.getTag() == "true")
                        b2.setBackgroundColor(Color.GREEN);
                    else if (b3.getTag() == "true")
                        b3.setBackgroundColor(Color.GREEN);
                    else if (b4.getTag() == "true")
                        b4.setBackgroundColor(Color.GREEN);

                    scorecounter.setText(score.toString());

                    timesup = new CountDownTimer(5000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            next.callOnClick();
                        }
                    }.start();

                }

            }.

                    start();


            next.setEnabled(false);

            b1.setEnabled(true);
            b2.setEnabled(true);
            b3.setEnabled(true);
            b4.setEnabled(true);

            b1.setClickable(true);
            b2.setClickable(true);
            b3.setClickable(true);
            b4.setClickable(true);

            b1.setAlpha(0.7f);
            b2.setAlpha(0.7f);
            b3.setAlpha(0.7f);
            b4.setAlpha(0.7f);


            b1.setBackgroundColor(Color.LTGRAY);
            b2.setBackgroundColor(Color.LTGRAY);
            b3.setBackgroundColor(Color.LTGRAY);
            b4.setBackgroundColor(Color.LTGRAY);

            question.setText(quiz.get(index).questionString);
            quiz.get(index).RandomizeQuestionOrder();

            b1.setText(quiz.get(index).answers[0].m_answer);
            b2.setText(quiz.get(index).answers[1].m_answer);
            b3.setText(quiz.get(index).answers[2].m_answer);
            b4.setText(quiz.get(index).answers[3].m_answer);


            if(quiz.get(index).answers[0].isCorrect)

            {
                b1.setTag("true");
            } else
                b1.setTag("false");

            if(quiz.get(index).answers[1].isCorrect)

            {
                b2.setTag("true");
            } else
                b2.setTag("false");

            if(quiz.get(index).answers[2].isCorrect)

            {
                b3.setTag("true");
            } else
                b3.setTag("false");

            if(quiz.get(index).answers[3].isCorrect)

            {
                b4.setTag("true");
            } else
                b4.setTag("false");

            for (int i = 0; i < buttons.size(); ++i) {

                if (buttons.get(i).getTag() == "true") {
                    buttons.remove(i);
                }
            }

            int randomIndex = (int) (Math.random() * 1000) % 3;
            buttons.remove(randomIndex);

// set string values for the questions
            next.setOnClickListener((view)->

                    {
                        gameTimer.cancel();
                        if (timesup != null)
                            timesup.cancel();
                        next.setEnabled(false);

                        if (i.get() < quiz.size() - 1) {
                            i.set(i.get() + 1); //increment index for the game loop
                            GameLoop(i.get()); //call game loop with new index value
                        } else {
                            i.set(quiz.size());
                            Intent results = new Intent(MultiplayerGame.this, Results.class);
                            finish();
                            startActivity(results);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            //sends you to the results screen
                        }
                    }
            );


            //exit App

            //next button functionality
            b1.setOnClickListener((view)->

            {
                if (tickingSound.isPlaying()) {
                    tickingSound.stop();
                    tickingSound.reset();
                }

                if (alarm.isPlaying()) {
                    alarm.stop();
                    alarm.reset();
                }

                b1.setEnabled(false);
                b2.setEnabled(false);
                b3.setEnabled(false);
                b4.setEnabled(false);
                next.setEnabled(true);
                gameTimer.cancel();
                gameTimer = new CountDownTimer(5000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        //do nothing
                    }

                    @Override
                    public void onFinish() {
                        next.callOnClick();
                    }
                }.start();

                if (b1.getTag() == "true") {
                    timerText.setText("Correct!");
                    timerText.setTextColor(Color.GREEN);
                    correctSound.start();
                    if (millisToAnswer < 6000)
                        score.set(score.get() + 5);
                    else if (millisToAnswer < 10000)
                        score.set(score.get() + 3);
                    else
                        score.set(score.get() + 1);
                    b1.setBackgroundColor(Color.GREEN);
                    b2.setBackgroundColor(Color.LTGRAY);
                    b3.setBackgroundColor(Color.LTGRAY);
                    b4.setBackgroundColor(Color.LTGRAY);
//                    Context context = getApplicationContext();
//                    CharSequence text = "Correct!";
//                    int duration = Toast.LENGTH_SHORT;
//                    correctSound.start();
//                    Toast toast = Toast.makeText(context, text, duration);
//                    toast.show();
                } else {

                    b2.setBackgroundColor(Color.LTGRAY);
                    b3.setBackgroundColor(Color.LTGRAY);
                    b4.setBackgroundColor(Color.LTGRAY);

                    wrongSound.start();
                    timerText.setText("Wrong!");
                    timerText.setTextColor(Color.rgb(255,0,0));
                    b1.setBackgroundColor(Color.RED);
                    if (b2.getTag() == "true")
                        b2.setBackgroundColor(Color.GREEN);
                    else if (b3.getTag() == "true")
                        b3.setBackgroundColor(Color.GREEN);
                    else if (b4.getTag() == "true")
                        b4.setBackgroundColor(Color.GREEN);
//                    Context context = getApplicationContext();
//                    CharSequence text = "Wrong!";
//                    int duration = Toast.LENGTH_SHORT;
//                    Toast toast = Toast.makeText(context, text, duration);
//                    toast.show();
                }

                scorecounter.setText(score.toString());
            });


            b2.setOnClickListener((view)->

            {

                if (tickingSound.isPlaying()) {
                    tickingSound.stop();
                    tickingSound.reset();
                }

                if (alarm.isPlaying()) {
                    alarm.stop();
                    alarm.reset();
                }

                gameTimer.cancel();

                gameTimer = new CountDownTimer(5000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        next.callOnClick();
                    }
                }.start();

                next.setEnabled(true);
                b1.setEnabled(false);
                b2.setEnabled(false);
                b3.setEnabled(false);
                b4.setEnabled(false);
                if (b2.getTag() == "true") {

                    correctSound.start();
                    timerText.setText("Correct!");
                    timerText.setTextColor(Color.GREEN);
                    if (millisToAnswer < 6000)
                        score.set(score.get() + 5);
                    else if (millisToAnswer < 10000)
                        score.set(score.get() + 3);
                    else
                        score.set(score.get() + 1);
                    b1.setBackgroundColor(Color.LTGRAY);
                    b2.setBackgroundColor(Color.GREEN);
                    b3.setBackgroundColor(Color.LTGRAY);
                    b4.setBackgroundColor(Color.LTGRAY);
//                    Context context = getApplicationContext();
//                    CharSequence text = "Correct!";
//                    int duration = Toast.LENGTH_SHORT;
//
//                    Toast toast = Toast.makeText(context, text, duration);
//                    toast.show();
                } else {

                    b1.setBackgroundColor(Color.LTGRAY);
                    b2.setBackgroundColor(Color.LTGRAY);
                    b3.setBackgroundColor(Color.LTGRAY);
                    b4.setBackgroundColor(Color.LTGRAY);
                    wrongSound.start();
                    timerText.setText("Wrong!");
                    timerText.setTextColor(Color.rgb(255,0,0));

                    if (b1.getTag() == "true")
                        b1.setBackgroundColor(Color.GREEN);
                    else if (b3.getTag() == "true")
                        b3.setBackgroundColor(Color.GREEN);
                    else if (b4.getTag() == "true")
                        b4.setBackgroundColor(Color.GREEN);

                    b2.setBackgroundColor(Color.RED);
//                    Context context = getApplicationContext();
//                    CharSequence text = "Wrong!";
//                    int duration = Toast.LENGTH_SHORT;
//
//                    Toast toast = Toast.makeText(context, text, duration);
//                    toast.show();
                }
                scorecounter.setText(score.toString());
            });


            b3.setOnClickListener((view)->

            {
                if (tickingSound.isPlaying()) {
                    tickingSound.stop();
                    tickingSound.reset();
                }

                if (alarm.isPlaying()) {
                    alarm.stop();
                    alarm.reset();
                }

                gameTimer.cancel();
                gameTimer = new CountDownTimer(5000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        next.callOnClick();
                    }
                }.start();

                next.setEnabled(true);
                b1.setEnabled(false);
                b2.setEnabled(false);
                b3.setEnabled(false);
                b4.setEnabled(false);

                if (b3.getTag() == "true") {
                    correctSound.start();
                    if (millisToAnswer < 6000)
                        score.set(score.get() + 5);
                    else if (millisToAnswer < 10000)
                        score.set(score.get() + 3);
                    else
                        score.set(score.get() + 1);
                    timerText.setText("Correct!");
                    timerText.setTextColor(Color.GREEN);

                    b1.setBackgroundColor(Color.LTGRAY);
                    b2.setBackgroundColor(Color.LTGRAY);
                    b3.setBackgroundColor(Color.GREEN);
                    b4.setBackgroundColor(Color.LTGRAY);
//                    Context context = getApplicationContext();
//                    CharSequence text = "Correct!";
//                    int duration = Toast.LENGTH_SHORT;
//
//                    Toast toast = Toast.makeText(context, text, duration);
//                    toast.show();
                } else {
                    b1.setBackgroundColor(Color.LTGRAY);
                    b2.setBackgroundColor(Color.LTGRAY);
                    b3.setBackgroundColor(Color.LTGRAY);
                    b4.setBackgroundColor(Color.LTGRAY);
                    wrongSound.start();
                    if (b1.getTag() == "true")
                        b1.setBackgroundColor(Color.GREEN);
                    else if (b2.getTag() == "true")
                        b2.setBackgroundColor(Color.GREEN);
                    else if (b4.getTag() == "true")
                        b4.setBackgroundColor(Color.GREEN);
                    b3.setBackgroundColor(Color.RED);
                    timerText.setTextColor(Color.RED);
                    timerText.setText("Wrong!");
//                    timerText.setTextColor(Color.RED);
//                    Context context = getApplicationContext();
//                    CharSequence text = "Wrong!";
//                    int duration = Toast.LENGTH_SHORT;
//
//                    Toast toast = Toast.makeText(context, text, duration);
//                    toast.show();
                }
                scorecounter.setText(score.toString());
            });

            b4.setOnClickListener((view)->
            {

                if (tickingSound.isPlaying()) {
                    tickingSound.stop();
                    tickingSound.reset();
                }

                if (alarm.isPlaying()) {
                    alarm.stop();
                    alarm.reset();
                }

                gameTimer.cancel();
                gameTimer = new CountDownTimer(5000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        next.callOnClick();
                    }
                }.start();

                next.setEnabled(true);
                b1.setEnabled(false);
                b2.setEnabled(false);
                b3.setEnabled(false);
                b4.setEnabled(false);

                if (b4.getTag() == "true") {
                    correctSound.start();
                    timerText.setText("Correct!");
                    timerText.setTextColor(Color.GREEN);
                    if (millisToAnswer < 6000)
                        score.set(score.get() + 5);
                    else if (millisToAnswer < 10000)
                        score.set(score.get() + 3);
                    else
                        score.set(score.get() + 1);
                    b1.setBackgroundColor(Color.LTGRAY);
                    b2.setBackgroundColor(Color.LTGRAY);
                    b3.setBackgroundColor(Color.LTGRAY);
                    b4.setBackgroundColor(Color.GREEN);
//                    Context context = getApplicationContext();
//                    CharSequence text = "Correct!";
//                    int duration = Toast.LENGTH_SHORT;
//
//                    Toast toast = Toast.makeText(context, text, duration);
//                    toast.show();
                } else {
                    b1.setBackgroundColor(Color.LTGRAY);
                    b2.setBackgroundColor(Color.LTGRAY);
                    b3.setBackgroundColor(Color.LTGRAY);
                    b4.setBackgroundColor(Color.LTGRAY);

                    wrongSound.start();
                    timerText.setText("Wrong!");
                    timerText.setTextColor(Color.RED);
                    if (b1.getTag() == "true")
                        b1.setBackgroundColor(Color.GREEN);
                    else if (b2.getTag() == "true")
                        b2.setBackgroundColor(Color.GREEN);
                    else if (b3.getTag() == "true")
                        b3.setBackgroundColor(Color.GREEN);
                    b4.setBackgroundColor(Color.RED);
//                    Context context = getApplicationContext();
//                    CharSequence text = "Wrong!";
//                    int duration = Toast.LENGTH_SHORT;
//
//                    Toast toast = Toast.makeText(context, text, duration);
//                    toast.show();
                }
                scorecounter.setText(score.toString());

            });
        }
    }

    public void leaveGame () {

        AlertDialog.Builder builder = new AlertDialog.Builder(MultiplayerGame.this);

        builder.setCancelable(false);
        builder.setTitle("Leave Game");
        builder.setMessage("Are you sure you want to leave the game?");

        // Setting Negative "Cancel" Button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        // Setting Positive "Yes" Button
        builder.setPositiveButton("Leave", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                tickingSound.stop();
                gameTimer.cancel();
                Intent menuActivity = new Intent(MultiplayerGame.this, MainMenu.class);
                finish();
                startActivity(menuActivity);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        builder.show();
    }

}