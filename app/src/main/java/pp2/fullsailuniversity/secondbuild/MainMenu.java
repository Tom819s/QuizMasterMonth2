package pp2.fullsailuniversity.secondbuild;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;

public class MainMenu extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Main Menu";

    public Button quickStart;
    public Button multi, lobby;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        quickStart = findViewById(R.id.quickstartButton);
        multi = findViewById(R.id.multiPB);
        lobby = findViewById(R.id.lobbyPB);
        multi.setEnabled(false);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Stay Tuned For Upcoming Content", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        lobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent gotToGameSetup = new Intent(MainMenu.this, SetupMultiplayer.class);
                startActivity(gotToGameSetup);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        quickStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TriviaDBURLcreator triviaURL = new TriviaDBURLcreator();
                triviaURL.mNumQuestions = 10;
                triviaURL.mDifficulty = "ANYDIFF";
                triviaURL.mCategory = "ANY";
                try {
                    MainGameActivity.urlToAPI = triviaURL.createURL();
                    finish();
                    Intent goToGame = new Intent(MainMenu.this, MainGameActivity.class);
                    startActivity(goToGame);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "quickStart IOException");
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void SoloButtonH(View view) {

        Intent gotToGameSetup = new Intent(this, GameSetup.class);
        startActivity(gotToGameSetup);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

    }
}
