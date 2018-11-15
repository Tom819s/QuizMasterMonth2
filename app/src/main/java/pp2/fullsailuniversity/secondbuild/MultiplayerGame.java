package pp2.fullsailuniversity.secondbuild;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class MultiplayerGame extends AppCompatActivity
{
    private static final String TAG = "MainActivityGame";


    private ConnectionsClient connectionsClient;

    // Our randomly generated name
    private final static String SERVICE_ID = "TRIVIAMASTERYAPP";


    private static final String[] REQUIRED_PERMISSIONS =
            new String[]{
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                    android.Manifest.permission.ACCESS_WIFI_STATE,
                    android.Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    private String opponentEndpointId;
    private String opponentName, userName, enemyScore;
    private TextView statusText;

    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    public static AtomicInteger iAtm, score;
    public static String urlToAPI;
    public static int gameTime;

    private long millisToAnswer, timeLeft, moveonLeft, timeToHitButton;
    private TextView question;
    private TextView timerText;
    QuizQuestion receivedQuestion;
    private TextView scorecounter, questioncounter;
    public static boolean hints, isHost, pressedTimer;
    private int numCorrect, numInRow, numQuestions, rightChime, wrongChime, opponentScore;
    private boolean previouscorrect, hasStopped, isFirstQuestion, isTF, hasendedgame;
    private Button next,
            exit,
            b1, b2, b3, b4;
    private CountDownTimer gameTimer, timesup, questionAnswerTimer;
    private ImageButton startbtn, timerbtn;
    private SoundPool rightwrongSound;
    private MediaPlayer tickingSound, alarm, loopingElectro;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate: starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multi_game);

        //Settings.Secure.getString(getContentResolver(), "bluetooth_name");
        userName = Build.MODEL;
        hasStopped = false;
        iAtm = new AtomicInteger();
        score = new AtomicInteger(0);

        gameTime = 20000;
        Log.d(TAG, "onCreate: gametime" + gameTime);

        ProgressBar pbar = findViewById(R.id.progressBarMulti);
        b1 = findViewById(R.id.button1Multi);
        b2 = findViewById(R.id.button2Multi);
        b3 = findViewById(R.id.button3Multi);
        b4 = findViewById(R.id.button4Multi);
        timerbtn = findViewById(R.id.timerButtonMulti);
        questioncounter = findViewById(R.id.questionNumberMulti);
        scorecounter = findViewById(R.id.scoreMulti);
        question = findViewById(R.id.userEmailMulti);
        next = findViewById(R.id.nextBMulti);
        exit = findViewById(R.id.exitBMulti);
        startbtn = findViewById(R.id.start_buttonMulti);
        timerText = findViewById(R.id.timerTextViewMulti);
        statusText = findViewById(R.id.statusTextMulti);
        connectionsClient = Nearby.getConnectionsClient(this);
        isFirstQuestion = true;
        isTF = false;

        timerbtn.setClickable(false);
        timerbtn.setAlpha(0.0f);

        findConnection();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            rightwrongSound = new SoundPool.Builder()
                    .setMaxStreams(2)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else
        {
            rightwrongSound = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        }
        rightChime = rightwrongSound.load(this, R.raw.correct, 1);
        wrongChime = rightwrongSound.load(this, R.raw.wrong, 1);


        loopingElectro = MediaPlayer.create(MultiplayerGame.this, R.raw.pixelsong);
        tickingSound = MediaPlayer.create(MultiplayerGame.this, R.raw.tickingclock);
        alarm = MediaPlayer.create(MultiplayerGame.this, R.raw.alarmringing);


        loopingElectro.setLooping(true);
        loopingElectro.start();
        scorecounter.setText("Score: 0");
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


        gameTimer = new CountDownTimer(20000, 250)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                Log.d(TAG, "attempting to start game..");
                startbtn.callOnClick();
            }

            @Override
            public void onFinish()
            {


                Toast.makeText(getApplicationContext(), "Problem Loading", Toast.LENGTH_LONG).show();
            }
        }.start();


        startbtn.setOnClickListener(v ->
                {

                    if (opponentEndpointId != null)
                    {
                        gameTimer.cancel();
                        pbar.setAlpha(0.0f);
                        pbar.setClickable(false);
                        startbtn.setClickable(false);
                        startbtn.setAlpha(0.0f);
                        startTimer();
                    }
                }
        );


        timerbtn.setOnClickListener(v ->
                {

                    questionAnswerTimer.cancel();
                    Log.d(TAG, "timerbutton onclick " + opponentEndpointId);
                    pressedTimer = true;
                    String timeToPress = "TIMER SUBMITTED\n" + timeToHitButton;
                    Payload timerPayload = Payload.fromBytes(timeToPress.toString().getBytes());
                    Nearby.getConnectionsClient(getApplicationContext()).sendPayload(opponentEndpointId, timerPayload);

                    timerbtn.setAlpha(0.0f);
                    timerbtn.setClickable(false);
                    timerbtn.setEnabled(false);
                    if (!isHost)
                    {
                        // wait for question to receive
                    }
                }
        );
        exit.setOnClickListener((view) ->
        {
            if (loopingElectro.isPlaying())
                loopingElectro.stop();
            leaveGame();

        });

        Log.d(TAG, "onCreate: ends");
//Setting of Q/A and Tags
    }

    @Override
    @TargetApi(23)
    protected void onStart()
    {
        super.onStart();

        if (!hasPermissions(this, REQUIRED_PERMISSIONS))
        {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }
    }


    @Override
    public void onBackPressed()
    {

        if (loopingElectro.isPlaying())
            loopingElectro.stop();
        if (gameTimer != null)
            gameTimer.cancel();
        if (timesup != null)
            timesup.cancel();
        leaveGame();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        hasStopped = true;
        if (loopingElectro.isPlaying())
            loopingElectro.stop();
        if (tickingSound.isPlaying())
            tickingSound.stop();
        if (alarm.isPlaying())
            alarm.stop();
        if (gameTimer != null)
            gameTimer.cancel();
        if (timesup != null)
            timesup.cancel();
    }

    @Override
    protected void onResume()
    {
        Log.d(TAG, "onResume starts");
        super.onResume();

        if (hasStopped)
        {
            if (!loopingElectro.isPlaying())
            {
                loopingElectro = MediaPlayer.create(MultiplayerGame.this, R.raw.gameplaymusicelectro);
                loopingElectro.setLooping(true);
                loopingElectro.start();
            }
            tickingSound = MediaPlayer.create(MultiplayerGame.this, R.raw.tickingclock);
            alarm = MediaPlayer.create(MultiplayerGame.this, R.raw.alarmringing);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                rightwrongSound = new SoundPool.Builder()
                        .setMaxStreams(2)
                        .setAudioAttributes(audioAttributes)
                        .build();
            } else
            {
                rightwrongSound = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
            }
            rightChime = rightwrongSound.load(this, R.raw.correct, 1);
            wrongChime = rightwrongSound.load(this, R.raw.wrong, 1);

            gameTimer = new CountDownTimer(timeLeft, 1000)
            {

                public void onTick(long millisUntilFinished)
                {

                    String count = Long.toString(millisUntilFinished / 1000);
                    millisToAnswer = gameTime - millisUntilFinished;
                    timeLeft = millisUntilFinished;
                    if (millisUntilFinished > (gameTime / 2 + 1000))
                    {
                        timerText.setTextColor(Color.rgb(0, 204, 0));
                    } else if (millisUntilFinished > (gameTime / 4 + 1000))
                    {
                        timerText.setTextColor(Color.rgb(255, 204, 0));
                    } else
                    {
                        if (!tickingSound.isPlaying())
                        {
                            tickingSound.setVolume(5.0f, 5.0f);
                            tickingSound.start();
                        }
                        timerText.setTextColor(Color.rgb(204, 0, 0));
                    }
                    timerText.setText(count);
                }

                public void onFinish()
                {
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

                    scorecounter.setText("Score: " + score.toString());

                    timesup = new CountDownTimer(moveonLeft, 1000)
                    {
                        @Override
                        public void onTick(long millisUntilFinished)
                        {
                            moveonLeft = millisUntilFinished;
                        }

                        @Override
                        public void onFinish()
                        {
                            next.callOnClick();
                        }
                    }.start();

                }

            }.start();
        }
        moveonLeft = 5000;
        Log.d(TAG, "onResume ends");
    }


    //next button functionality

    public void leaveGame()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(MultiplayerGame.this);

        builder.setCancelable(false);
        builder.setTitle("Leave Game");
        builder.setMessage("Are you sure you want to leave the game?");

        // Setting Negative "Cancel" Button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                dialog.cancel();
            }
        });

        // Setting Positive "Yes" Button
        builder.setPositiveButton("Leave", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
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

    public void GameLoop(QuizQuestion questionTransfered, int index)
    {

        timeLeft = 0;
        moveonLeft = 0;
        if (tickingSound.isPlaying())
        {
            tickingSound.stop();
        }

        if (alarm.isPlaying())
        {
            alarm.stop();
        }

        if (timesup != null)
            timesup.cancel();
        gameTimer.cancel();

        if (questionTransfered != null)
        {
            tickingSound.stop();
            tickingSound.release();
            tickingSound = MediaPlayer.create(MultiplayerGame.this, R.raw.tickingclock);
            Log.d(TAG, "question transfered" + questionTransfered.toString());
            List<Button> buttons = new ArrayList<Button>();
            buttons.add(b1);
            buttons.add(b2);
            buttons.add(b3);
            buttons.add(b4);

            questioncounter.setText("Question: " + (index + 1) + " / " + "10");
            gameTimer = new CountDownTimer(gameTime, 1000)
            {

                public void onTick(long millisUntilFinished)
                {

                    String count = Long.toString(millisUntilFinished / 1000);
                    millisToAnswer = gameTime - millisUntilFinished;
                    timeLeft = millisUntilFinished;
                    if (millisUntilFinished > (gameTime / 2 + 1000))
                    {
                        timerText.setTextColor(Color.rgb(0, 204, 0));
                    } else if (millisUntilFinished > (gameTime / 4 + 1000))
                    {
                        timerText.setTextColor(Color.rgb(255, 204, 0));
                    } else
                    {
                        if (!tickingSound.isPlaying())
                        {
                            tickingSound.setVolume(5.0f, 5.0f);
                            tickingSound.start();
                            if (hints)
                            {
                                buttons.get(0).setEnabled(false);
                                buttons.get(1).setEnabled(false);
                            }
                        }
                        timerText.setTextColor(Color.rgb(204, 0, 0));
                    }
                    timerText.setText(count);
                }

                public void onFinish()
                {
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

                    scorecounter.setText("Score: " + score.toString());

                    timesup = new CountDownTimer(5000, 1000)
                    {
                        @Override
                        public void onTick(long millisUntilFinished)
                        {
                            moveonLeft = millisUntilFinished;
                        }

                        @Override
                        public void onFinish()
                        {
                            next.callOnClick();
                        }
                    }.start();

                }

            }.

                    start();


            next.setEnabled(false);
            if (!questionTransfered.isTrueFalse)
            {
                isTF = false;
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

                question.setText(questionTransfered.questionString);
                questionTransfered.RandomizeQuestionOrder();

                b1.setText(questionTransfered.answers[0].m_answer);
                b2.setText(questionTransfered.answers[1].m_answer);
                b3.setText(questionTransfered.answers[2].m_answer);
                b4.setText(questionTransfered.answers[3].m_answer);


                if (questionTransfered.answers[0].isCorrect)

                {
                    b1.setTag("true");
                } else
                    b1.setTag("false");

                if (questionTransfered.answers[1].isCorrect)

                {
                    b2.setTag("true");
                } else
                    b2.setTag("false");

                if (questionTransfered.answers[2].isCorrect)

                {
                    b3.setTag("true");
                } else
                    b3.setTag("false");

                if (questionTransfered.answers[3].isCorrect)

                {
                    b4.setTag("true");
                } else
                    b4.setTag("false");

                for (int i = 0; i < buttons.size(); ++i)
                {

                    if (buttons.get(i).getTag() == "true")
                    {
                        buttons.remove(i);
                    }
                }

                int randomIndex = (int) (Math.random() * 1000) % 3;
                buttons.remove(randomIndex);
            } else
            {
                isTF = true;
                b1.setEnabled(true);
                b3.setEnabled(true);
                b2.setEnabled(false);
                b4.setEnabled(false);

                b1.setClickable(true);
                b3.setClickable(true);
                b2.setClickable(false);
                b4.setClickable(false);

                b1.setAlpha(0.7f);
                b3.setAlpha(0.7f);
                b2.setAlpha(0.0f);
                b4.setAlpha(0.0f);


                b1.setBackgroundColor(Color.LTGRAY);
                b3.setBackgroundColor(Color.LTGRAY);

                question.setText(questionTransfered.questionString);

                b1.setText("true");
                b3.setText("false");


                if (questionTransfered.correctAns)
                {
                    b1.setTag("true");
                } else
                    b1.setTag("false");

                if (questionTransfered.correctAns)
                {
                    b3.setTag("false");
                } else
                    b3.setTag("true");

            }

// set string values for the questions
            next.setOnClickListener((view) ->
                    {



                        gameTimer.cancel();
                        if (timesup != null)
                            timesup.cancel();
                        next.setEnabled(false);

                        if (iAtm.get() < 9)
                        {
                            String tosend = "START TIMER\n" + score.get();
                            Payload chatPayload = Payload.fromBytes(tosend.getBytes());
                            Nearby.getConnectionsClient(getApplicationContext()).sendPayload(opponentEndpointId, chatPayload);
                            //increment index for the game loop
                            startTimer(); //call game loop with new index value
                        } else
                        {

                            String tosend = "END GAME";
                            Payload chatPayload = Payload.fromBytes(tosend.getBytes());
                            Nearby.getConnectionsClient(getApplicationContext()).sendPayload(opponentEndpointId, chatPayload);

                            iAtm.set(10);
                            Intent results = new Intent(MultiplayerGame.this, multiResults.class);
                            String[] gameResults = new String[4];
                            gameResults[0] = Integer.toString(iAtm.get());
                            gameResults[1] = Integer.toString(score.get());
                            gameResults[2] = opponentName;
                            gameResults[3] = Integer.toString(opponentScore);
                            results.putExtra("gameResults", gameResults);
                            finish();
                            startActivity(results);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            //sends you to the results screen
                        }
                    }
            );


            //exit App

            //next button functionality
            b1.setOnClickListener((view) ->
            {
                if (tickingSound.isPlaying())
                {
                    tickingSound.stop();
                    tickingSound.reset();
                }

                if (alarm.isPlaying())
                {
                    alarm.stop();
                    alarm.reset();
                }

                b1.setEnabled(false);
                b2.setEnabled(false);
                b3.setEnabled(false);
                b4.setEnabled(false);
                next.setEnabled(true);
                gameTimer.cancel();
                gameTimer = new CountDownTimer(5000, 1000)
                {
                    @Override
                    public void onTick(long millisUntilFinished)
                    {
                        //do nothing
                    }

                    @Override
                    public void onFinish()
                    {
                        next.callOnClick();
                    }
                }.start();

                if (b1.getTag() == "true")
                {
                    timerText.setText("Correct!");
                    timerText.setTextColor(Color.GREEN);

                    rightwrongSound.play(rightChime, 1, 1, 0, 0, 1);
                    if (millisToAnswer < 2500)
                    {
                        score.set(score.get() + 10);
                        displayMedal4();
                    } else if (millisToAnswer < 6000)
                        score.set(score.get() + 5);
                    else if (millisToAnswer < 10000)
                        score.set(score.get() + 3);
                    else
                        score.set(score.get() + 1);
                    b1.setBackgroundColor(Color.GREEN);
                    b2.setBackgroundColor(Color.LTGRAY);
                    b3.setBackgroundColor(Color.LTGRAY);
                    b4.setBackgroundColor(Color.LTGRAY);
                    numInRow += 1;
                    numCorrect += 1;

                    if (numInRow == 3)
                    {
                        displayMedal2();
                        score.set(score.get() + 5);
                        numInRow = 0;
                    }
                } else
                {
                    numInRow = 0;
                    b2.setBackgroundColor(Color.LTGRAY);
                    b3.setBackgroundColor(Color.LTGRAY);
                    b4.setBackgroundColor(Color.LTGRAY);

                    rightwrongSound.play(wrongChime, 1, 1, 0, 0, 1);
                    timerText.setText("Wrong!");
                    timerText.setTextColor(Color.rgb(255, 0, 0));
                    b1.setBackgroundColor(Color.RED);
                    if (b2.getTag() == "true")
                        b2.setBackgroundColor(Color.GREEN);
                    else if (b3.getTag() == "true")
                        b3.setBackgroundColor(Color.GREEN);
                    else if (b4.getTag() == "true")
                        b4.setBackgroundColor(Color.GREEN);
                }

                scorecounter.setText("Score: " + score.toString());
            });


            b2.setOnClickListener((view) ->
            {

                if (tickingSound.isPlaying())
                {
                    tickingSound.stop();
                    tickingSound.reset();
                }

                if (alarm.isPlaying())
                {
                    alarm.stop();
                    alarm.reset();
                }

                gameTimer.cancel();

                gameTimer = new CountDownTimer(5000, 1000)
                {
                    @Override
                    public void onTick(long millisUntilFinished)
                    {

                    }

                    @Override
                    public void onFinish()
                    {
                        next.callOnClick();
                    }
                }.start();

                next.setEnabled(true);
                b1.setEnabled(false);
                b2.setEnabled(false);
                b3.setEnabled(false);
                b4.setEnabled(false);
                if (b2.getTag() == "true")
                {

                    rightwrongSound.play(rightChime, 1, 1, 0, 0, 1);
                    timerText.setText("Correct!");
                    timerText.setTextColor(Color.GREEN);
                    if (millisToAnswer < 2500)
                    {
                        score.set(score.get() + 10);
                        displayMedal4();
                    } else if (millisToAnswer < 6000)
                        score.set(score.get() + 5);
                    else if (millisToAnswer < 10000)
                        score.set(score.get() + 3);
                    else
                        score.set(score.get() + 1);
                    b1.setBackgroundColor(Color.LTGRAY);
                    b2.setBackgroundColor(Color.GREEN);
                    b3.setBackgroundColor(Color.LTGRAY);
                    b4.setBackgroundColor(Color.LTGRAY);
                    numInRow += 1;
                    numCorrect += 1;

                    if (numInRow == 3)
                    {
                        displayMedal2();
                        score.set(score.get() + 5);
                        numInRow = 0;
                    }

                } else
                {
                    numInRow = 0;
                    b1.setBackgroundColor(Color.LTGRAY);
                    b2.setBackgroundColor(Color.LTGRAY);
                    b3.setBackgroundColor(Color.LTGRAY);
                    b4.setBackgroundColor(Color.LTGRAY);

                    rightwrongSound.play(wrongChime, 1, 1, 0, 0, 1);
                    timerText.setText("Wrong!");
                    timerText.setTextColor(Color.rgb(255, 0, 0));

                    if (b1.getTag() == "true")
                        b1.setBackgroundColor(Color.GREEN);
                    else if (b3.getTag() == "true")
                        b3.setBackgroundColor(Color.GREEN);
                    else if (b4.getTag() == "true")
                        b4.setBackgroundColor(Color.GREEN);

                    b2.setBackgroundColor(Color.RED);
                }
                scorecounter.setText("Score: " + score.toString());
            });


            b3.setOnClickListener((view) ->
            {
                if (tickingSound.isPlaying())
                {
                    tickingSound.stop();
                    tickingSound.reset();
                }

                if (alarm.isPlaying())
                {
                    alarm.stop();
                    alarm.reset();
                }

                gameTimer.cancel();
                gameTimer = new CountDownTimer(5000, 1000)
                {
                    @Override
                    public void onTick(long millisUntilFinished)
                    {

                    }

                    @Override
                    public void onFinish()
                    {
                        next.callOnClick();
                    }
                }.start();

                next.setEnabled(true);
                b1.setEnabled(false);
                b2.setEnabled(false);
                b3.setEnabled(false);
                b4.setEnabled(false);

                if (b3.getTag() == "true")
                {
                    rightwrongSound.play(rightChime, 1, 1, 0, 0, 1);
                    if (millisToAnswer < 2500)
                    {
                        score.set(score.get() + 10);
                        displayMedal4();
                    } else if (millisToAnswer < 6000)
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
                    numInRow += 1;
                    numCorrect += 1;

                    if (numInRow == 3)
                    {
                        displayMedal2();
                        score.set(score.get() + 5);
                        numInRow = 0;
                    }

                } else
                {
                    numInRow = 0;
                    b1.setBackgroundColor(Color.LTGRAY);
                    b2.setBackgroundColor(Color.LTGRAY);
                    b3.setBackgroundColor(Color.LTGRAY);
                    b4.setBackgroundColor(Color.LTGRAY);
                    rightwrongSound.play(wrongChime, 1, 1, 0, 0, 1);
                    if (b1.getTag() == "true")
                        b1.setBackgroundColor(Color.GREEN);
                    else if (b2.getTag() == "true")
                        b2.setBackgroundColor(Color.GREEN);
                    else if (b4.getTag() == "true")
                        b4.setBackgroundColor(Color.GREEN);
                    b3.setBackgroundColor(Color.RED);
                    timerText.setTextColor(Color.RED);
                    timerText.setText("Wrong!");
                }
                scorecounter.setText("Score: " + score.toString());
            });

            b4.setOnClickListener((view) ->
            {

                if (tickingSound.isPlaying())
                {
                    tickingSound.stop();
                    tickingSound.reset();
                }

                if (alarm.isPlaying())
                {
                    alarm.stop();
                    alarm.reset();
                }

                gameTimer.cancel();
                gameTimer = new CountDownTimer(5000, 1000)
                {
                    @Override
                    public void onTick(long millisUntilFinished)
                    {

                    }

                    @Override
                    public void onFinish()
                    {
                        next.callOnClick();
                    }
                }.start();

                next.setEnabled(true);
                b1.setEnabled(false);
                b2.setEnabled(false);
                b3.setEnabled(false);
                b4.setEnabled(false);

                if (b4.getTag() == "true")
                {
                    rightwrongSound.play(rightChime, 1, 1, 0, 0, 1);
                    timerText.setText("Correct!");
                    timerText.setTextColor(Color.GREEN);
                    if (millisToAnswer < 2500)
                    {
                        score.set(score.get() + 10);
                        displayMedal4();
                    } else if (millisToAnswer < 6000)
                        score.set(score.get() + 5);
                    else if (millisToAnswer < 10000)
                        score.set(score.get() + 3);
                    else
                        score.set(score.get() + 1);
                    b1.setBackgroundColor(Color.LTGRAY);
                    b2.setBackgroundColor(Color.LTGRAY);
                    b3.setBackgroundColor(Color.LTGRAY);
                    b4.setBackgroundColor(Color.GREEN);
                    numInRow += 1;
                    numCorrect += 1;

                    if (numInRow == 3)
                    {
                        displayMedal2();
                        score.set(score.get() + 5);
                        numInRow = 0;
                    }
                } else
                {
                    numInRow = 0;
                    b1.setBackgroundColor(Color.LTGRAY);
                    b2.setBackgroundColor(Color.LTGRAY);
                    b3.setBackgroundColor(Color.LTGRAY);
                    b4.setBackgroundColor(Color.LTGRAY);
                    rightwrongSound.play(wrongChime, 1, 1, 0, 0, 1);
                    timerText.setText("Wrong!");
                    timerText.setTextColor(Color.RED);
                    if (b1.getTag() == "true")
                        b1.setBackgroundColor(Color.GREEN);
                    else if (b2.getTag() == "true")
                        b2.setBackgroundColor(Color.GREEN);
                    else if (b3.getTag() == "true")
                        b3.setBackgroundColor(Color.GREEN);
                    b4.setBackgroundColor(Color.RED);
                }
                scorecounter.setText("Score: " + score.toString());

            });
        }
    }


    public void displayMedal1()
    {

        LayoutInflater toastInflater = getLayoutInflater();
        View view = toastInflater.inflate(R.layout.medal_1,
                findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setView(view);
        toast.show();


    }

    public void displayMedal2()
    {

        LayoutInflater toastInflater = getLayoutInflater();
        View view = toastInflater.inflate(R.layout.medal_2,
                findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setView(view);
        toast.show();


    }

    public void displayMedal3()
    {

        LayoutInflater toastInflater = getLayoutInflater();
        View view = toastInflater.inflate(R.layout.medal_3,
                findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setView(view);
        toast.show();


    }

    public void displayMedal4()
    {

        LayoutInflater toastInflater = getLayoutInflater();
        View view = toastInflater.inflate(R.layout.medal_4,
                findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setView(view);
        toast.show();


    }

    public void displayMedal5()
    {

        LayoutInflater toastInflater = getLayoutInflater();
        View view = toastInflater.inflate(R.layout.medal_5,
                findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setView(view);
        toast.show();


    }

    private final PayloadCallback payloadCallback =
            new PayloadCallback()
            {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload)
                {
                    String receivedString = new String(payload.asBytes());
                    String lines[] = receivedString.split("\\r?\\n");

                    switch (lines[0])
                    {
                        case "QUIZ QUESTION":
                        {
                            Answer a1 = new Answer(lines[2], true);
                            Answer a2 = new Answer(lines[3], false);
                            Answer a3 = new Answer(lines[4], false);
                            Answer a4 = new Answer(lines[5], false);

                            receivedQuestion = new QuizQuestion(lines[1], a1, a2, a3, a4);
                            receivedQuestion.RandomizeQuestionOrder();
                            GameLoop(receivedQuestion, iAtm.get());
                            //setupScreen(lines)
                            //run code to set up reg. MC question
                            break;
                        }
                        case "QUIZ QUESTION TF":
                        {
                            //setupScreenTF(lines)
                            receivedQuestion = new QuizQuestion(lines[1], true);
                            //TODO change to check truth values
                            GameLoop(receivedQuestion, iAtm.get());
                            //run code to set up TF question
                            break;
                        }
                        case "DEBUFF":
                        {
                            Log.d(TAG, "onPayloadReceived: debuffed");
                            Toast.makeText(getApplicationContext(), opponentName + " played a trick on you!", Toast.LENGTH_LONG).show();

                            switch (lines[1])
                            {
                                case "TIME":
                                {
                                    if (gameTimer != null)
                                    {

                                        tickingSound = MediaPlayer.create(MultiplayerGame.this, R.raw.tickingclock);
                                        alarm = MediaPlayer.create(MultiplayerGame.this, R.raw.alarmringing);

                                        gameTimer.cancel();
                                        timeLeft = timeLeft / 2;
                                        gameTimer = new CountDownTimer(timeLeft, 1000)
                                        {
                                            @Override
                                            public void onTick(long millisUntilFinished)
                                            {

                                                String count = Long.toString(millisUntilFinished / 1000);
                                                millisToAnswer = gameTime - millisUntilFinished;
                                                timeLeft = millisUntilFinished;
                                                if (millisUntilFinished > (gameTime / 2 + 1000))
                                                {
                                                    timerText.setTextColor(Color.rgb(0, 204, 0));
                                                } else if (millisUntilFinished > (gameTime / 4 + 1000))
                                                {
                                                    timerText.setTextColor(Color.rgb(255, 204, 0));
                                                } else
                                                {
                                                    if (!tickingSound.isPlaying())
                                                    {
                                                        tickingSound.setVolume(5.0f, 5.0f);
                                                        tickingSound.start();
                                                    }
                                                    timerText.setTextColor(Color.rgb(204, 0, 0));
                                                }
                                                timerText.setText(count);

                                            }

                                            @Override
                                            public void onFinish()
                                            {

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

                                                scorecounter.setText("Score: " + score.toString());

                                                timesup = new CountDownTimer(moveonLeft, 1000)
                                                {
                                                    @Override
                                                    public void onTick(long millisUntilFinished)
                                                    {
                                                        moveonLeft = millisUntilFinished;
                                                    }

                                                    @Override
                                                    public void onFinish()
                                                    {
                                                        next.callOnClick();
                                                    }
                                                }.start();

                                            }
                                        }.start();
                                    }
                                    break;
                                }
                                case "FLIP":
                                {

                                    Log.d(TAG, "onPayloadReceived: flip");
                                    int orientation = getRequestedOrientation();

                                    CountDownTimer a = new CountDownTimer(3000, 125)
                                    {
                                        @Override
                                        public void onTick(long millisUntilFinished)
                                        {
                                            if (millisUntilFinished % 2 == 0)
                                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                                            else
                                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                                        }

                                        @Override
                                        public void onFinish()
                                        {

                                            setRequestedOrientation(orientation);
                                        }
                                    }.start();
                                    break;
                                }
                                case "REARRANGE":
                                {
                                    if (!isTF)
                                    {


                                        String text1 = (String) b1.getText();
                                        String text2 = (String) b2.getText();
                                        String text3 = (String) b3.getText();
                                        String text4 = (String) b4.getText();

                                        b1.setText(text3);
                                        b2.setText(text1);
                                        b3.setText(text4);
                                        b4.setText(text2);
                                    }
                                    else {

                                        String text1 = (String) b1.getText();
                                        String text2 = (String) b3.getText();

                                        b1.setText(text2);
                                        b3.setText(text1);
                                    }
                                    break;
                                }
                                case "FAIL":
                                {
                                    Log.d(TAG, "onPayloadReceived: fail");
                                    next.callOnClick();
                                    break;
                                }
                            }
                            break;
                        }
                        case "WAIT":
                        {
                            //waitScreen()
                            waitScreen();
                            //TODO set up debuffs on wait screen
                            //run code to setup debuff screen and wait for next question
                        }
                        case "START TIMER":
                        {
                            //startTiming()
                            if (questionAnswerTimer != null)
                                questionAnswerTimer.cancel();

                            Log.d(TAG, "onPayloadReceived: " + lines[1]);
                            if (lines[1] != null)
                                opponentScore = Integer.parseInt(lines[1]);
                            else
                                opponentScore = 0;
                            startTimer();

                            //run code to start timer
                            break;
                        }
                        case "TIMER SUBMITTED":
                        {
                            //compareTimes(lines)
                            if (isHost)
                            {
                                if (lines[1] != null && !pressedTimer)
                                {
                                    //if player pressed timer before host did, send them a question and let them answer it
                                    QuizQuestion toSend = receivedQuestion;
                                    Payload questionPayload = Payload.fromBytes(toSend.toString().getBytes());
                                    Nearby.getConnectionsClient(getApplicationContext()).sendPayload(opponentEndpointId, questionPayload);


                                }
                            }
                            timerText.setText("Waiting");
                            waitScreen();
                            //run code to compare times, if host hasn't hit timer yet, by default opponent wins
                            break;
                        }
                        case "END GAME":
                        {
                            //gotoResults(lines)
                            enemyScore = lines[1];
                            iAtm.set(10);
                            Intent results = new Intent(MultiplayerGame.this, multiResults.class);
                            String[] gameResults = new String[4];
                            gameResults[0] = Integer.toString(iAtm.get());
                            gameResults[1] = Integer.toString(score.get());
                            gameResults[2] = opponentName;
                            gameResults[3] = Integer.toString(opponentScore);
                            results.putExtra("gameResults", gameResults);
                            finish();
                            startActivity(results);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            // run code to load results screen
                            break;
                        }
                        default:
                        {
                            Toast.makeText(getApplicationContext(), "Issue with payload", Toast.LENGTH_LONG);
                        }
                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update)
                {
//                   update progress of incoming and outgoing payloads
                    //called when first byte is received, not whole payload !!!!
                }
            };

    // Callbacks for finding other devices
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback()
            {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info)
                {
                    Log.i(TAG, "onEndpointFound: endpoint found, connecting");
                    connectionsClient.requestConnection(userName, endpointId, connectionLifecycleCallback);
                }

                @Override
                public void onEndpointLost(String endpointId)
                {
                }
            };

    // Callbacks for connections to other devices
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback()
            {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo)
                {
                    Log.i(TAG, "onConnectionInitiated: accepting connection");
                    connectionsClient.acceptConnection(endpointId, payloadCallback);
                    opponentName = connectionInfo.getEndpointName();

                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result)
                {
                    if (result.getStatus().isSuccess())
                    {
                        Log.i(TAG, "onConnectionResult: connection successful");

                        connectionsClient.stopDiscovery();
                        connectionsClient.stopAdvertising();
                        Toast.makeText(getApplicationContext(), "Connected to " + opponentName, Toast.LENGTH_LONG).show();
                        opponentEndpointId = endpointId;
                        setStatusText("Connected!");
                    } else
                    {
                        Log.i(TAG, "onConnectionResult: connection failed");
                        Toast.makeText(getApplicationContext(), "Something went wrong with the connection", Toast.LENGTH_LONG);
                    }
                }

                @Override
                public void onDisconnected(String endpointId)
                {
                    Log.i(TAG, "onDisconnected: disconnected from the opponent");

                    Toast.makeText(getApplicationContext(), "Opponent Disconnected", Toast.LENGTH_LONG);
                    resetGame();
                }
            };

    @Override
    protected void onStop()
    {
        if (connectionsClient != null)
            connectionsClient.stopAllEndpoints();
        resetGame();

        super.onStop();
    }

    public void advertiseConnection(View view)
    {

        setStatusText("Searching....");
        isHost = true;
        startAdvertising();
    }

    public void findConnection()
    {

        setStatusText("Searching....");
        isHost = false;
        startDiscovery();
    }

    public void disconnect(View view)
    {
        resetGame();
        setStatusText("DISCONNECTED FROM CONNECTION");
    }

    private void startDiscovery()
    {
        DiscoveryOptions.Builder options = new DiscoveryOptions.Builder().setStrategy(STRATEGY);
        Nearby.getConnectionsClient(getApplicationContext()).startDiscovery(
                SERVICE_ID,
                endpointDiscoveryCallback,
                options.build())
                .addOnSuccessListener(
                        new OnSuccessListener<Void>()
                        {
                            @Override
                            public void onSuccess(Void unusedResult)
                            {
                                setStatusText("DISCOVERING!");
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                setStatusText("not DISCOVERING!");
                                Log.d(TAG, "onFailure: " + e.getMessage());
                                Toast.makeText(getApplicationContext(), "Something went wrong with the connection", Toast.LENGTH_LONG).show();
                            }
                        });
    }

    private void startAdvertising()
    {
        AdvertisingOptions.Builder options = new AdvertisingOptions.Builder().setStrategy(STRATEGY);
        Nearby.getConnectionsClient(getApplicationContext()).startAdvertising(
                userName,
                SERVICE_ID,
                connectionLifecycleCallback,
                options.build()
        )
                .addOnSuccessListener(
                        new OnSuccessListener<Void>()
                        {
                            @Override
                            public void onSuccess(Void unusedResult)
                            {// We're advertising!
                                setStatusText("Advertising");
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                // We were unable to start advertising.
                                setStatusText("Something went wrong with advertising");
                                Toast.makeText(getApplicationContext(), "Something went wrong with the connection", Toast.LENGTH_LONG).show();
                            }
                        });
    }

    private void resetGame()
    {
        opponentEndpointId = null;
        opponentName = null;

        setStatusText("Disconnected");
    }

    private void setStatusText(String text)
    {
        statusText.setText(text);
    }

    private void sendQuizQuestion(int i)
    {
        if (isHost)
        {
            Payload chatPayload = Payload.fromBytes(receivedQuestion.toString().getBytes());
            Nearby.getConnectionsClient(getApplicationContext()).sendPayload(opponentEndpointId, chatPayload);
        }
    }

    public void clickAnswerTimer(View V)
    {
        pressedTimer = true;
        String timeToPress = "TIMER SUBMITTED\n" + timeToHitButton;
        Payload timerPayload = Payload.fromBytes(timeToPress.toString().getBytes());
        Nearby.getConnectionsClient(getApplicationContext()).sendPayload(opponentEndpointId, timerPayload);

    }

    private void waitScreen()
    {

        questionAnswerTimer.cancel();
        next.setClickable(false);

        timerbtn.setEnabled(false);
        timerbtn.setAlpha(0.0f);
        timerbtn.setClickable(false);

// disabled if not enough points
        //in listener, disable others after deducting points

        b1.setText("Halve Opponent's time\n5pts");
        b2.setText("Rearrange Questions\n5pts");
        b3.setText("Make Opponent Fail Question\n15pts");
        b4.setText("Rotate Opponent's screen\n10pts");

        b1.setEnabled(true);
        b2.setEnabled(true);
        b3.setEnabled(true);
        b4.setEnabled(true);

        b1.setBackgroundColor(Color.LTGRAY);
        b2.setBackgroundColor(Color.LTGRAY);
        b3.setBackgroundColor(Color.LTGRAY);
        b4.setBackgroundColor(Color.LTGRAY);

        b1.setClickable(true);
        b2.setClickable(true);
        b3.setClickable(true);
        b4.setClickable(true);

        b1.setAlpha(1.0f);
        b2.setAlpha(1.0f);
        b3.setAlpha(1.0f);
        b4.setAlpha(1.0f);


        b1.setOnClickListener(v ->
                {//halve time

                    //"TIME"
                    if (score.get() > 4)
                    {
                        String toSend = "DEBUFF\nTIME";
                        Payload debuffPayload = Payload.fromBytes(toSend.getBytes());
                        Nearby.getConnectionsClient(getApplicationContext()).sendPayload(opponentEndpointId, debuffPayload);
                        score.set(score.get() - 5);
                        scorecounter.setText("Score: " + score.get());
                    } else
                        Toast.makeText(getApplicationContext(), "Not enough points!", Toast.LENGTH_LONG).show();
                    b1.setEnabled(false);
                }
        );

        b2.setOnClickListener(v ->
                {//rearrange questions
                    //"REARRANGE"
                    if (score.get() > 4)
                    {
                        String toSend = "DEBUFF\nREARRANGE";
                        Payload debuffPayload = Payload.fromBytes(toSend.getBytes());
                        Nearby.getConnectionsClient(getApplicationContext()).sendPayload(opponentEndpointId, debuffPayload);
                        score.set(score.get() - 5);
                        scorecounter.setText("Score: " + score.get());
                    } else
                        Toast.makeText(getApplicationContext(), "Not enough points!", Toast.LENGTH_LONG).show();
                    b2.setEnabled(false);
                }
        );

        b3.setOnClickListener(v ->
                {//fail question
                    //"FAIL"
                    if (score.get() > 14)
                    {
                        String toSend = "DEBUFF\nFAIL";
                        Payload debuffPayload = Payload.fromBytes(toSend.getBytes());
                        Nearby.getConnectionsClient(getApplicationContext()).sendPayload(opponentEndpointId, debuffPayload);
                        score.set(score.get() - 15);
                        scorecounter.setText("Score: " + score.get());
                    } else
                        Toast.makeText(getApplicationContext(), "Not enough points!", Toast.LENGTH_LONG).show();
                    b3.setEnabled(false);

                }
        );

        b4.setOnClickListener(v ->
                {//flip screen
                    //"FLIP"
                    if (score.get() > 9)
                    {
                        String toSend = "DEBUFF\nFLIP";
                        Payload debuffPayload = Payload.fromBytes(toSend.getBytes());
                        Nearby.getConnectionsClient(getApplicationContext()).sendPayload(opponentEndpointId, debuffPayload);
                        score.set(score.get() - 10);
                        scorecounter.setText("Score: " + score.get());
                    } else
                        Toast.makeText(getApplicationContext(), "Not enough points!", Toast.LENGTH_LONG).show();

                    b4.setEnabled(false);
                }
        );


        timerText.setText("Waiting for opponent to finish question");
    }


    private void startTimer()
    {
        if (!isFirstQuestion)
            iAtm.set(iAtm.get() + 1);

        questioncounter.setText("Question " + Integer.toString(iAtm.get() + 1));

        isFirstQuestion = false;

        question.setTextColor(Color.BLACK);
        question.setText("Hit before your opponent!");

        timerText.setTextColor(Color.BLACK);
        timerText.setText("Go!");
        pressedTimer = false;
        b1.setEnabled(false);
        b2.setEnabled(false);
        b3.setEnabled(false);
        b4.setEnabled(false);

        b1.setClickable(false);
        b2.setClickable(false);
        b3.setClickable(false);
        b4.setClickable(false);

        b1.setAlpha(0.0f);
        b2.setAlpha(0.0f);
        b3.setAlpha(0.0f);
        b4.setAlpha(0.0f);

        timerbtn.setEnabled(true);
        timerbtn.setClickable(true);
        timerbtn.setAlpha(1.0f);

        questionAnswerTimer = new CountDownTimer(20000, 100)
        {

            public void onTick(long millisUntilFinished)
            {
                String count = Long.toString(millisUntilFinished / 1000);
                timerText.setText(count);
                timeToHitButton = 20000 - millisUntilFinished;
            }

            public void onFinish()
            {
                timerbtn.setEnabled(false);
                timerbtn.setAlpha(0.0f);
                timerbtn.setClickable(false);

                timerText.setText("Out of time!");
                timesup = new CountDownTimer(5000, 1000)
                {
                    @Override
                    public void onTick(long millisUntilFinished)
                    {
                        String count = Long.toString(millisUntilFinished / 1000);
                        timerText.setText(count);
                    }

                    @Override
                    public void onFinish()
                    {
                        next.callOnClick();
                    }
                }.start();

            }

        }.start();


    }

    private static boolean hasPermissions(Context context, String... permissions)
    {
        for (String permission : permissions)
        {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Handles user acceptance (or denial) of our permission request.
     */

    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS)
        {
            return;
        }

        for (int grantResult : grantResults)
        {
            if (grantResult == PackageManager.PERMISSION_DENIED)
            {
                Toast.makeText(this, "Error: Missing Permissions!", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        recreate();
    }

}


