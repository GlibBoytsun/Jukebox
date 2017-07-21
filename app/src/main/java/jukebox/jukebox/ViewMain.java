package jukebox.jukebox;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
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

// Main view. Allows user to log in with their Spotify account
/*
* Almost all code in this class is taken from the Spotify Android SDK
* Title: Spotify SDK for Android devices
* Author: spotify
* Date: May 3, 2017
* Availability: https://github.com/spotify/android-sdk
*/
public class ViewMain extends AppCompatActivity implements Player.NotificationCallback, ConnectionStateCallback
{
    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = "e3141966e1bf4496a62607309847bf0b";
    private static final String REDIRECT_URI = "https://www.getpostman.com/oauth2/callback";

    private SpotifyPlayer mPlayer;

    // Initializes current view
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }



    // Spotify operation callback. Effectively does nothing.
    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onError(Error error) {

        }
    };

    // Opens Spotify login window
    public void openLoginWindow(View v) {
        final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(new String[]{"user-read-private", "playlist-read", "playlist-read-private", "streaming"})
                .build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    // Spotify login window callback
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                case TOKEN:
                    onAuthenticationComplete(response);
                    break;

                case ERROR:
                    break;

                default:
            }
        }
    }

    // Authentication handler. Creates Spotify player instance after user has logged in. Opens Group view afterwards.
    private void onAuthenticationComplete(AuthenticationResponse authResponse) {
        if (mPlayer == null) {
            Config playerConfig = new Config(getApplicationContext(), authResponse.getAccessToken(), CLIENT_ID);
            Global.playerConfig = playerConfig;
            mPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer player) {
                    mPlayer.setConnectivityStatus(mOperationCallback, getNetworkConnectivity(ViewMain.this));

                    Global.player = player;
                    openGroupView();
                }

                @Override
                public void onError(Throwable error) {
                    Log.d("a", "Player init error");
                }
            });
        } else {
            mPlayer.login(authResponse.getAccessToken());
            openGroupView();
        }
    }

    // Opens Group view
    private void openGroupView()
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                Intent intent = new Intent(ViewMain.this, ViewGroups.class);
                startActivity(intent);
            }
        });
    }

    // Returns network connectivity status (online / offline)
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

    // Unused callback
    @Override
    public void onLoggedIn()
    {
        Log.d("a", "Logged in");
    }

    // Unused callback
    @Override
    public void onLoggedOut()
    {
        Log.d("a", "Logged out");
    }

    // Unused callback
    @Override
    public void onLoginFailed(Error error)
    {
        Log.d("a", "Login failed");
    }

    // Unused callback
    @Override
    public void onTemporaryError()
    {
        Log.d("a", "Temporary error");
    }

    // Unused callback
    @Override
    public void onConnectionMessage(String s)
    {
        Log.d("a", "Connection message");
    }

    // Unused callback
    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent)
    {
         Log.d("a", "Playback event");
    }

    // Unused callback
    @Override
    public void onPlaybackError(Error error)
    {
        Log.d("a", "Playback error");
    }
}