package pp2.fullsailuniversity.secondbuild;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.w3c.dom.Text;

import java.util.Random;

/** Activity controlling the Rock Paper Scissors game */
public class SetupMultiplayer extends AppCompatActivity {

    private static final String TAG = "SetupMultiplayer", SERVICE_ID = "TRIVIAMASTERYAPP";

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
    private String opponentName, userName;
    private int opponentScore;

    private Button findOpponentButton, disconnectButton, testMedal, sendChat, discover;
    private TextView opponentText;
    private TextView statusText;
    private TextView scoreText;
    private TextView chatText;
    private EditText chatInput;

    // Callbacks for receiving payloads
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    String receivedString = new String(payload.asBytes());
                    chatText.append(Html.fromHtml(receivedString));
                    chatText.append("\n");
                }

                @Override
                  public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
//                   update progress of incoming and outgoing payloads
                    //called when first byte is received, not whole payload !!!!
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
                        Toast.makeText(getApplicationContext(),"Connected to " + opponentName, Toast.LENGTH_LONG).show();
                        opponentEndpointId = endpointId;
                        setStatusText("Connected!");
                    } else {
                        Log.i(TAG, "onConnectionResult: connection failed");
                        Toast.makeText(getApplicationContext(), "Something went wrong with the connection", Toast.LENGTH_LONG);
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

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        findOpponentButton = findViewById(R.id.findOpponentButton);
        discover = findViewById(R.id.discoverButton);
        disconnectButton = findViewById(R.id.disconnect);
        testMedal = findViewById(R.id.TestMedalButton);
        statusText = findViewById(R.id.statusText);
        chatText = findViewById(R.id.chatTextBox);
        chatText.setMovementMethod(new ScrollingMovementMethod());
        chatInput = findViewById(R.id.editText);
        sendChat = findViewById(R.id.sendChat);

        chatInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    sendChat.callOnClick();
                    handled = true;
                }
                return handled;
            }
        });
        Random random = new Random();

        // create a big random number - maximum is ffffff (hex) = 16777215 (dez)
        int nextInt = random.nextInt(0xffffff + 1);

        // format it as hexadecimal string (with hashtag and leading zeros)
        String colorCode = String.format("#%06x", nextInt);
        userName = "<font color='" + colorCode + "'>" + "Scholar" + (int)(Math.random() * 1000) + ":</font>";


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
    
    public void advertiseConnection(View view) {
    
        setStatusText("Searching....");
        startAdvertising();
    }
    
    public void findConnection(View view) {
    
        setStatusText("Searching....");
        startDiscovery();
        ;
    }

    /** Disconnects from the opponent and reset the UI. */
    public void disconnect(View view) {
        resetGame();
        setStatusText("DISCONNECTED FROM CONNECTION");
        findOpponentButton.setEnabled(true);
        discover.setEnabled(true);
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
        discover.setEnabled(false);
        findOpponentButton.setEnabled(false);
        DiscoveryOptions.Builder options = new DiscoveryOptions.Builder().setStrategy(STRATEGY);
        Nearby.getConnectionsClient(getApplicationContext()).startDiscovery(
                SERVICE_ID,
                endpointDiscoveryCallback,
                options.build())
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                statusText.setText("DISCOVERING!");
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                statusText.setText("not DISCOVERING!");
                                discover.setEnabled(true);
                                findOpponentButton.setEnabled(true);
                                Toast.makeText(getApplicationContext(), "Something went wrong with the connection", Toast.LENGTH_LONG).show();
                            }
                        });
    }
    
    
    
    /** Broadcasts our presence using Nearby Connections so other players can find us. */
    private void startAdvertising() {
        findOpponentButton.setEnabled(false);
        discover.setEnabled(false);
        AdvertisingOptions.Builder options = new AdvertisingOptions.Builder().setStrategy(STRATEGY);
        Nearby.getConnectionsClient(getApplicationContext()).startAdvertising("User",
                SERVICE_ID,
                connectionLifecycleCallback,
                options.build()
            )
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {// We're advertising!
                                statusText.setText("ADVERTISING!!!");
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // We were unable to start advertising.
                                statusText.setText("Something went wrong with advertising");
                                Toast.makeText(getApplicationContext(), "Something went wrong with the connection", Toast.LENGTH_LONG).show();
                                findOpponentButton.setEnabled(true);
                                discover.setEnabled(true);
                            }
                        });
    }

    /** Wipes all game state and updates the UI accordingly. */
    private void resetGame() {
        opponentEndpointId = null;
        opponentName = null;
        opponentScore = 0;

        setStatusText("Disconnected");
    }
//
//    /** Sends the user's selection of rock, paper, or scissors to the opponent. */
//    @TargetApi(19)
//    private void sendGameChoice() {
//        //myChoice = choice;
////        connectionsClient.sendPayload(
////                opponentEndpointId, Payload.fromBytes(choice.getBytes(UTF_8)));
//
//        setStatusText("Chose a button: ");
//        // No changing your mind!
//        setGameChoicesEnabled(false);
//    }


    /** Shows a status message to the user. */
    private void setStatusText(String text) {
        statusText.setText(text);
    }


    /** Updates the running score ticker. */
    public void sendChat(View view) {
        String input =  '\t' + chatInput.getText().toString();
        String tosend = userName + input;
        Payload chatPayload = Payload.fromBytes(tosend.getBytes());
        Nearby.getConnectionsClient(getApplicationContext()).sendPayload(opponentEndpointId, chatPayload);
        chatInput.setText("");
        chatText.append(Html.fromHtml(userName));
        chatText.append(input + '\n');
    
    
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
}
