package jukebox.jukebox;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.ArrayList;
import java.util.function.Function;

//PHP server tutorial https://www.b4x.com/android/forum/threads/connect-android-to-mysql-database-tutorial.8339/
//https://www.tutorialspoint.com/android/android_php_mysql.htm
//https://github.com/spotify/android-sdk

public class ViewMain extends AppCompatActivity implements Player.NotificationCallback, ConnectionStateCallback
{
    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = "089d841ccc194c10a77afad9e1c11d54";
    private static final String REDIRECT_URI = "testschema://callback";

    private SpotifyPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            Log.d("a", "Operation callback successful");
        }

        @Override
        public void onError(Error error) {
            Log.d("a", "Operation callback error");
        }
    };

    public void openLoginWindow(View v) {
        final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(new String[]{"user-read-private", "playlist-read", "playlist-read-private", "streaming"})
                .build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    onAuthenticationComplete(response);
                    break;

                // Auth flow returned an error
                case ERROR:
                    Log.d("a", "Auth error");
                    break;

                // Most likely auth flow was cancelled
                default:
                    Log.d("a", "Auth default");
            }
        }
    }

    private void onAuthenticationComplete(AuthenticationResponse authResponse) {
        // Once we have obtained an authorization token, we can proceed with creating a Player.
        //logStatus("Got authentication token");
        if (mPlayer == null) {
            Config playerConfig = new Config(getApplicationContext(), authResponse.getAccessToken(), CLIENT_ID);
            // Since the Player is a static singleton owned by the Spotify class, we pass "this" as
            // the second argument in order to refcount it properly. Note that the method
            // Spotify.destroyPlayer() also takes an Object argument, which must be the same as the
            // one passed in here. If you pass different instances to Spotify.getPlayer() and
            // Spotify.destroyPlayer(), that will definitely result in resource leaks.
            mPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer player) {
                    //logStatus("-- Player initialized --");
                    mPlayer.setConnectivityStatus(mOperationCallback, getNetworkConnectivity(ViewMain.this));
                    mPlayer.addNotificationCallback(ViewMain.this);
                    mPlayer.addConnectionStateCallback(ViewMain.this);
                    // Trigger UI refresh
                    //updateView();
                    Log.d("D", "Player initialized");

                    Global.player = player;
                    Intent intent = new Intent(ViewMain.this, ViewGroups.class);
                    startActivity(intent);
                }

                @Override
                public void onError(Throwable error) {
                    Log.d("a", "Player init error");
                }
            });
        } else {
            mPlayer.login(authResponse.getAccessToken());
        }
    }

    private Connectivity getNetworkConnectivity(Context context) {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return Connectivity.fromNetworkType(activeNetwork.getType());
        } else {
            return Connectivity.OFFLINE;
        }
    }

    @Override
    public void onLoggedIn()
    {
        Log.d("a", "Logged in");
    }

    @Override
    public void onLoggedOut()
    {
        Log.d("a", "Logged out");
    }

    @Override
    public void onLoginFailed(Error error)
    {
        Log.d("a", "Login failed");
    }

    @Override
    public void onTemporaryError()
    {
        Log.d("a", "Temporary error");
    }

    @Override
    public void onConnectionMessage(String s)
    {
        Log.d("a", "Connection message");
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent)
    {
        Log.d("a", "Playback event");
    }

    @Override
    public void onPlaybackError(Error error)
    {
        Log.d("a", "Playback error");
    }
}
