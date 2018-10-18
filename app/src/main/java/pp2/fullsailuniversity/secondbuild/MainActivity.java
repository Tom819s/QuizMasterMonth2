package pp2.fullsailuniversity.secondbuild;


import android.content.Intent;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import com.google.android.gms.auth.api.Auth;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;

import java.util.Objects;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "";
    private static int RC_SIGN_IN = 1001;

    private GoogleApiClient mGoogleApiClient;
    public GoogleSignInAccount accToSend;

    //Sign in Flow Functions

    private void startSignIn() {
        //TODO Create sign-in intent and auth flow
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    private void userSignOut() {
        //TODO Sign the user out and update UI
    }

    private void disconnect() {
        //TODO disconnect current account completely and update UI
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //TODO customize sig in button

        SignInButton signIn = findViewById(R.id.googleSignIn);
        signIn.setSize(SignInButton.SIZE_WIDE);
        signIn.setColorScheme(SignInButton.COLOR_DARK);


        // Sign in Listener
        signIn.setOnClickListener(v -> {
            switch (v.getId()) {
                case R.id.googleSignIn:
                    startSignIn();
                    break;
            }
        });


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        Log.d(TAG, "onCreate: Sign in option object created");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        //Create Google Api Client
        Log.d(TAG, "onCreate: Google Api Created");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            signInResultHandler(result);

            updateUI(result);
        }
    }


    private void signInResultHandler(GoogleSignInResult result) {

        Log.d(TAG, "signInResultHandler: SIGNIN RESULT CALLED");
        if (result.isSuccess()) {

            Log.d(TAG, "signInResultHandler: SIGNIN RESULT CALLED success = TRUE");
            GoogleSignInAccount account = result.getSignInAccount();


        } else {
            Status status = result.getStatus();
            int statusCode = status.getStatusCode();
            Log.d(TAG, "STATUS CODE: " + statusCode);

            if (statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {

                Log.d(TAG, "signInResultHandler: Sing in was cancelled");
            }
        }
    }

    private void updateUI(GoogleSignInResult result) {

        Intent goToMainMenu = new Intent(this, MainMenu.class);
        accToSend = result.getSignInAccount();

        // Array data as follows
        //userData[0] = user name,  userData[1] = user email, userData[2] = user name photo url
        String[] userData = new String[3];
        if (accToSend != null) {

            userData[0] = accToSend.getDisplayName();
            userData[1] = accToSend.getEmail();
            if (accToSend.getPhotoUrl() != null)
            userData[2] = accToSend.getPhotoUrl().toString();
            else
                {
                userData[2] = "DEFAULT IMAGE";
                Log.d(TAG, "updateUI: NO USER IMAGE FOUND");
            }

            Log.d(TAG, "updateUI: " + userData[0] + " " + userData[1]);
            goToMainMenu.putExtra("myKey", userData);

            startActivity(goToMainMenu);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();

        Intent goToMainMenu = new Intent(this, MainMenu.class);


        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        // Array data as follows
        //userData[0] = user name,  userData[1] = user email, userData[2] = user name photo url
        String[] userData = new String[3];
        if (account != null) {

            userData[0] = account.getDisplayName();
            userData[1] = account.getEmail();
            if (account.getPhotoUrl() != null)
                userData[2] = account.getPhotoUrl().toString();
            else
            {
                userData[2] = "DEFAULT IMAGE";
                Log.d(TAG, "updateUI: NO USER IMAGE FOUND");
            }

            Log.d(TAG, "updateUI: " + userData[0] + " " + userData[1]);
            goToMainMenu.putExtra("myKey", userData);

            startActivity(goToMainMenu);
        }
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.



    }

    public void loginHandlerB(View view) {
        Intent intent = new Intent(this, LoginActivity.class);

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            Log.d(TAG, " On stop Called. Disconnecting Google services");

            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Called");

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: Connection Failed : ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }


}
