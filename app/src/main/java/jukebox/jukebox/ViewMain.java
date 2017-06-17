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

//REFERENCE
//Almost all code in this class is taken from the Spotify Android SDK (https://github.com/spotify/android-sdk)
public class ViewMain extends AppCompatActivity implements Player.NotificationCallback, ConnectionStateCallback
{
    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = "e3141966e1bf4496a62607309847bf0b";
    private static final String REDIRECT_URI = "https://www.getpostman.com/oauth2/callback";

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

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                case TOKEN:
                    onAuthenticationComplete(response);
                    break;

                case ERROR:
                    Log.d("a", "Auth error");
                    break;

                default:
                    Log.d("a", "Auth default");
            }
        }
    }

    private void onAuthenticationComplete(AuthenticationResponse authResponse) {
        if (mPlayer == null) {
            Config playerConfig = new Config(getApplicationContext(), authResponse.getAccessToken(), CLIENT_ID);
            mPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer player) {
                    mPlayer.setConnectivityStatus(mOperationCallback, getNetworkConnectivity(ViewMain.this));
                    Log.d("D", "Player initialized");

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
        }
    }

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

//Reference for most of the code in this file is provided before declaration of this class