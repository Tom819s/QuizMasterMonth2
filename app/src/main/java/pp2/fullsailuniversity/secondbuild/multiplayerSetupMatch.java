package pp2.fullsailuniversity.secondbuild;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class multiplayerSetupMatch extends AppCompatActivity
{
    private final static String TAG = "multiplayerSetup";
    private MediaPlayer loopSong;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate: starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiplayer_setup_match);
        loopSong = MediaPlayer.create(getApplicationContext(), R.raw.pixelsong);
        loopSong.setLooping(true);
        loopSong.start();

    }

    @Override
    public void onBackPressed()
    {

        loopSong.stop();

        Intent host = new Intent(multiplayerSetupMatch.this, MainMenu.class);
        finish();
        startActivity(host);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onStop()
    {

        if (loopSong.isPlaying())
            loopSong.stop();
        super.onStop();
    }

    @Override
    protected void onResume()
    {
        if (!loopSong.isPlaying())
        {
            loopSong = MediaPlayer.create(getApplicationContext(), R.raw.pixelsong);
            loopSong.start();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        if (loopSong.isPlaying())
            loopSong.stop();
        super.onDestroy();
    }

    public void loadHost(View v)
    {
        loopSong.stop();

        Intent host = new Intent(multiplayerSetupMatch.this, MultiplayerGameHost.class);
        finish();
        startActivity(host);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void loadClient(View v)
    {
        loopSong.stop();

        Intent join = new Intent(multiplayerSetupMatch.this, MultiplayerGame.class);
        finish();
        startActivity(join);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

    }

}
