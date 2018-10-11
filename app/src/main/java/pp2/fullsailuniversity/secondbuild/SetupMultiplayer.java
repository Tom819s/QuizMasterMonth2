package pp2.fullsailuniversity.secondbuild;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.nearby.connection.PayloadTransferUpdate.Status;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.Random;

/** Activity controlling the Rock Paper Scissors game */
public class SetupMultiplayer extends AppCompatActivity {

    private static final String TAG = "SetupMultiplayer";

    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    private static final Strategy STRATEGY = Strategy.P2P_STAR;



    // Our handle to Nearby Connections
    private ConnectionsClient connectionsClient;

    // Our randomly generated name
    private final String codeName = "PlaceholderName";

    private String opponentEndpointId;
    private String opponentName;
    private int opponentScore;

    private Button findOpponentButton, disconnectButton, testMedal;
    private TextView opponentText;
    private TextView statusText;
    private TextView scoreText;

    // Callbacks for receiving payloads
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
//                    opponentChoice = GameChoice.valueOf(new String(payload.asBytes(), UTF_8));
                }

                @Override
                  public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
//                   if (update.getStatus() == Status.SUCCESS && myChoice != null && opponentChoice != null) {
//                       finishRound();
//                    }
                }
            };

    // Callbacks for finding other devices
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                    Log.i(TAG, "onEndpointFound: endpoint found, connecting");
                    connectionsClient.requestConnection(codeName, endpointId, connectionLifecycleCallback);
                }

                @Override
                public void onEndpointLost(String endpointId) {}
            };

    // Callbacks for connections to other devices
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Log.i(TAG, "onConnectionInitiated: accepting connection");
                    connectionsClient.acceptConnection(endpointId, payloadCallback);
                    opponentName = connectionInfo.getEndpointName();
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    if (result.getStatus().isSuccess()) {
                        Log.i(TAG, "onConnectionResult: connection successful");

                        connectionsClient.stopDiscovery();
                        connectionsClient.stopAdvertising();

                        opponentEndpointId = endpointId;
                        setOpponentName(opponentName);
                        setStatusText("Connected!");
                        setButtonState(true);
                    } else {
                        Log.i(TAG, "onConnectionResult: connection failed");
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    Log.i(TAG, "onDisconnected: disconnected from the opponent");
                    resetGame();
                }
            };

    @Override
    protected void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.multiplayer_join_activity);

        findOpponentButton = findViewById(R.id.findOpponentButton);
        disconnectButton = findViewById(R.id.disconnect);
        testMedal = findViewById(R.id.TestMedalButton);
        statusText = findViewById(R.id.statusText);


        connectionsClient = Nearby.getConnectionsClient(this);

        resetGame();
    }

    @Override
    public void onBackPressed() {

        Intent toMainMenu = new Intent(this, MainMenu.class);
        finish();
        startActivity(toMainMenu);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    @TargetApi(23)
    protected void onStart() {
        super.onStart();

        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
                    requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }
    }

    @Override
    protected void onStop() {
        connectionsClient.stopAllEndpoints();
        resetGame();

        super.onStop();
    }

    /** Returns true if the app was granted all the permissions. Otherwise, returns false. */
    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /** Handles user acceptance (or denial) of our permission request. */
    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Error: Missing Permissions!", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        recreate();
    }

    /** Finds an opponent to play the game with using Nearby Connections. */
    public void findOpponent(View view) {
        startAdvertising();
        startDiscovery();
        setStatusText("Searching....");
        findOpponentButton.setEnabled(false);
    }

    /** Disconnects from the opponent and reset the UI. */
    public void disconnect(View view) {
        connectionsClient.disconnectFromEndpoint(opponentEndpointId);
        resetGame();
    }

    /** Sends a {GameChoice} to the other player. */
    public void makeMove(View view) {
//        if (view.getId() == R.id.rock) {
//            sendGameChoice(GameChoice.ROCK);
//        } else if (view.getId() == R.id.paper) {
//            sendGameChoice(GameChoice.PAPER);
//        } else if (view.getId() == R.id.scissors) {
//            sendGameChoice(GameChoice.SCISSORS);
//        }
    }

    /** Starts looking for other players using Nearby Connections. */
    private void startDiscovery() {
        // Note: Discovery may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startDiscovery(
                getPackageName(), endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build());
    }

    public void randomMedal(View view){

        LayoutInflater toastInflater = getLayoutInflater();
        Random rand = new Random();

        int result = rand.nextInt()%5;

        if (result == 0){
        view = toastInflater.inflate(R.layout.medal_1,
                findViewById(R.id.relativeLayout1));
        }
        else if (result == 1){
            view = toastInflater.inflate(R.layout.medal_2,
                    findViewById(R.id.relativeLayout1));
        }else if (result == 2){
            view = toastInflater.inflate(R.layout.medal_3,
                    findViewById(R.id.relativeLayout1));
        }else if (result == 3){
            view = toastInflater.inflate(R.layout.medal_4,
                    findViewById(R.id.relativeLayout1));
        }else{
            view = toastInflater.inflate(R.layout.medal_5,
                    findViewById(R.id.relativeLayout1));
        }
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setView(view);
        toast.show();
    }

    /** Broadcasts our presence using Nearby Connections so other players can find us. */
    private void startAdvertising() {
        // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startAdvertising(
                codeName, getPackageName(), connectionLifecycleCallback,
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build());
    }

    /** Wipes all game state and updates the UI accordingly. */
    private void resetGame() {
        opponentEndpointId = null;
        opponentName = null;
        opponentScore = 0;

        setOpponentName("No Opponent");
        setStatusText("Disconnected");
        setButtonState(false);
    }

    /** Sends the user's selection of rock, paper, or scissors to the opponent. */
    @TargetApi(19)
    private void sendGameChoice() {
        //myChoice = choice;
//        connectionsClient.sendPayload(
//                opponentEndpointId, Payload.fromBytes(choice.getBytes(UTF_8)));

        setStatusText("Chose a button: ");
        // No changing your mind!
        setGameChoicesEnabled(false);
    }

    /** Determines the winner and update game state/UI after both players have chosen. */
    private void finishRound() {
       //used to wrap up the round
        setGameChoicesEnabled(true);
    }

    /** Enables/disables buttons depending on the connection status. */
    private void setButtonState(boolean connected) {
        findOpponentButton.setEnabled(true);
//        findOpponentButton.setVisibility(connected ? View.GONE : View.VISIBLE);
//        disconnectButton.setVisibility(connected ? View.VISIBLE : View.GONE);

        setGameChoicesEnabled(connected);
    }

    /** Enables/disables the rock, paper, and scissors buttons. */
    private void setGameChoicesEnabled(boolean enabled) {
//        rockButton.setEnabled(enabled);
//        paperButton.setEnabled(enabled);
//        scissorsButton.setEnabled(enabled);
    }

    /** Shows a status message to the user. */
    private void setStatusText(String text) {
        statusText.setText(text);
    }

    /** Updates the opponent name on the UI. */
    private void setOpponentName(String opponentName) {
        //opponentText.setText("Opponent Name");
    }

    /** Updates the running score ticker. */
    private void updateScore(int myScore, int opponentScore) {
        //scoreText.setText("");
    }
}
