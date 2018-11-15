package pp2.fullsailuniversity.secondbuild;


import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;

public class MainMenu extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener
{

    private static final String TAG = "Main Menu";
    private static final int REQ_CODE = 101;


    public Button multi, quickStart;
    private static String[] userDataInformation;
    private MediaPlayer menuMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        menuMusic = MediaPlayer.create(MainMenu.this, R.raw.menuloop);
        menuMusic.setLooping(true);
        menuMusic.start();
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        quickStart = findViewById(R.id.quickstartButton);
        multi = findViewById(R.id.multiPB);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Stay Tuned For Upcoming Content", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        quickStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TriviaDBURLcreator triviaURL = new TriviaDBURLcreator();
                triviaURL.mNumQuestions = 10;
                triviaURL.mDifficulty = "ANYDIFF";
                triviaURL.mCategory = "ANY";
                try
                {
                    MainGameActivity.urlToAPI = triviaURL.createURL();
                    finish();
                    if (menuMusic.isPlaying())
                        menuMusic.stop();
                    menuMusic.release();
                    menuMusic = null;
                    Intent goToGame = new Intent(MainMenu.this, MainGameActivity.class);
                    goToGame.putExtra("myKey", 20);
                    startActivity(goToGame);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } catch (IOException e)
                {
                    e.printStackTrace();
                    Log.d(TAG, "quickStart IOException");
                }
            }
        });

        multi.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (menuMusic.isPlaying())
                    menuMusic.stop();
                menuMusic.release();
                menuMusic = null;
                finish();
                Intent goToGame = new Intent(MainMenu.this, multiplayerSetupMatch.class);
                startActivity(goToGame);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        Intent intent = getIntent();
        String[] userData = intent.getStringArrayExtra("myKey");


        if (userData != null)
        {
            Toast toast;
            if (userData[0] != null)
                toast = Toast.makeText(getApplicationContext(), "Logged in as : " + userData[0], Toast.LENGTH_LONG);
            else
                toast = Toast.makeText(getApplicationContext(), "Logged in", Toast.LENGTH_LONG);

            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);

            toast.show();
        }


    }


    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        } else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu)
    {
        //Setting user name and email values
        TextView userName = findViewById(R.id.UserName);
        TextView userEmail = findViewById(R.id.userEmail);

        //getting user info
        String[] userData = getIntent().getStringArrayExtra("myKey");
        if (userData != null)
        {
            userDataInformation = new String[3];
            userDataInformation[0] = userData[0];
            userDataInformation[1] = userData[1];
            userDataInformation[2] = userData[2];
            Log.d(TAG, "onCreate: userData");
            Log.d(TAG, "onCreate: userName = " + userData[0]);
            Log.d(TAG, "onCreate: userEmail = " + userData[1]);
            ImageView userPic = findViewById(R.id.userPhoto);

            userName.setText(userData[0]);
            userEmail.setText(userData[1]);

            if (!userData[2].equals("DEFAULT IMAGE"))
                Glide.with(this).load(userData[2]).into(userPic);
            else
                userPic.setImageResource(R.drawable.defaultuserimage);

        } else
        {
            userName.setText(userDataInformation[0]);
            userEmail.setText(userDataInformation[1]);
            ImageView userPic = findViewById(R.id.userPhoto);
            if (!userDataInformation[2].equals("DEFAULT IMAGE"))
                Glide.with(this).load(userDataInformation[2]).into(userPic);
            else
                userPic.setImageResource(R.drawable.defaultuserimage);

        }

        return super.onCreatePanelMenu(featureId, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.gameBadges)
        {

            // Handle achievement action

        } else if (id == R.id.leaderBoard)
        {

        } else if (id == R.id.nav_manage)
        {

        } else if (id == R.id.sing_out)
        {

            Intent intent = new Intent(this, MainActivity.class);
            String signout = "confirmed";
            intent.putExtra("thekey", signout);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void SoloButtonH(View view)
    {

        if (menuMusic.isPlaying())
            menuMusic.stop();
        menuMusic.release();
        menuMusic = null;
        Intent gotToGameSetup = new Intent(this, GameSetup.class);
        startActivity(gotToGameSetup);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (menuMusic != null && menuMusic.isPlaying())
            menuMusic.stop();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (menuMusic == null || !menuMusic.isPlaying())
        {
            menuMusic = MediaPlayer.create(MainMenu.this, R.raw.menuloop);
            menuMusic.setLooping(true);
            menuMusic.start();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

}
