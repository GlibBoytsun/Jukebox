package jukebox.jukebox;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

public class ViewPlayer extends AppCompatActivity implements AdapterView.OnItemClickListener
{
    Context context;
    TextView lInfo;
    Button bRetry;
    ListView lbPlaylist;
    TextView lCurrentSong;
    TextView lSongTime;
    Button bPlayStop;

    Song curSong = null;
    PlayerState nextState = null;
    Timer updateTimer = null;
    Timer spotifyTimer = null;

    boolean isMaster = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        context = this;
        lInfo = (TextView)findViewById(R.id.PlayerInfo);
        bRetry = (Button)findViewById(R.id.PlayerRetry);
        lbPlaylist = (ListView)findViewById(R.id.PlayerPlaylist);
        lCurrentSong = (TextView)findViewById(R.id.PlayerCurrentSong);
        lSongTime = (TextView)findViewById(R.id.PlayerTime);
        bPlayStop = (Button)findViewById(R.id.PlayerPlayStop);

        lbPlaylist.setOnItemClickListener(this);

        lInfo.setVisibility(View.VISIBLE);
        bRetry.setVisibility(View.INVISIBLE);
        lbPlaylist.setVisibility(View.INVISIBLE);
        lCurrentSong.setVisibility(View.INVISIBLE);
        lSongTime.setVisibility(View.INVISIBLE);
        bPlayStop.setVisibility(View.INVISIBLE);

        getPlaylist();
    }

    private void getPlaylist()
    {
        Database.GetPlaylist(Global.group.id, new Function<ArrayList<Song>, Object>()
        {
            @Override
            public Object apply(ArrayList<Song> songs)
            {
                Global.group.playlist = songs;

                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ArrayList<Song> songs = Global.group.playlist;

                        if (songs == null)
                        {
                            bRetry.setVisibility(View.VISIBLE);
                            lInfo.setText("Couldn't retrieve playlist");
                            return;
                        }

                        //curSong = songs.get(0);

                        lInfo.setVisibility(View.INVISIBLE);
                        bRetry.setVisibility(View.INVISIBLE);
                        lbPlaylist.setVisibility(View.VISIBLE);
                        lCurrentSong.setVisibility(View.VISIBLE);
                        lSongTime.setVisibility(View.VISIBLE);
                        bPlayStop.setVisibility(View.VISIBLE);

                        List<String> arr = new ArrayList();
                        for (int i = 0; i < songs.size(); i++)
                            arr.add(songs.get(i).artist + " - " + songs.get(i).title);

                        lbPlaylist.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_selectable_list_item, arr));

                        updatePlayerState();

                        updateTimer = new Timer("Player update timer");
                        updateTimer.schedule(new TimerTask()
                        {
                            @Override
                            public void run()
                            {
                                updateOnlineState();
                                updatePlayerState();
                            }
                        }, 0, 1000);
                    }
                });

                return null;
            }
        });
    }

    public void bRetry_OnClick(View v)
    {
        lInfo.setText("Retrieving playlist...");
        bRetry.setVisibility(View.INVISIBLE);
        getPlaylist();
    }

    public void bPlayStop_OnClick(View v)
    {
        if (bPlayStop.getText() == "...")
            return;
        else if (bPlayStop.getText() == "Play")
        {
            isMaster = true;
            Calendar startTime = Calendar.getInstance();
            startTime.add(Calendar.SECOND, 5);
            Database.SetNextSong(Global.group.id, 0, startTime.getTimeInMillis());
        }
        else if (bPlayStop.getText() == "Stop")
        {
            Global.player.pause(mOperationCallback);
            Database.SetNextSong(Global.group.id, -1, Calendar.getInstance().getTimeInMillis());
            curSong = null;
            nextState.songIndex = -1;
        }
    }

    private void updatePlayerState()
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                if (curSong == null)
                {
                    bPlayStop.setVisibility(View.VISIBLE);
                    lCurrentSong.setText("");
                    lSongTime.setText("");
                    if (nextState == null)
                        bPlayStop.setText("...");
                    else if (nextState.songIndex == -1)
                        bPlayStop.setText("Play");
                    else
                    {
                        long diff = nextState.startTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
                        diff /= 1000;
                        diff++;
                        bPlayStop.setText("Starting in " + String.valueOf(diff));
                    }
                }
                else
                {
                    lCurrentSong.setText("Currently playing: " + curSong.artist + " - " + curSong.title);

                    PlaybackState st = Global.player.getPlaybackState();
                    int curTime = 0;
                    if (st.isPlaying)
                        curTime = (int)(st.positionMs / 1000);
                    String s1 = String.valueOf(curTime % 60);
                    if (s1.length() == 1)
                        s1 = "0" + s1;
                    s1 = String.valueOf(curTime / 60) + ":" + s1;

                    String s2 = String.valueOf(curSong.duration % 60);
                    if (s2.length() == 1)
                        s2 = "0" + s2;
                    s2 = String.valueOf(curSong.duration / 60) + ":" + s2;

                    lSongTime.setText(s1 + " / " + s2);
                    bPlayStop.setText("Stop");

                    if (nextState != null && nextState.songIndex == -1)
                    {
                        //stop
                        curSong = null;
                        updatePlayerState();
                    }
                }
            }
        });
    }

    private void spotifyDelayedPlay(final Calendar startTime)
    {
        Global.player.playUri(mOperationCallback, Global.group.GetSongByIndex(nextState.songIndex).url, 0, 0);
        Global.player.pause(mOperationCallback);

        if (spotifyTimer != null)
            return;

        long diff = nextState.startTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
        if (diff < 0)
            Global.player.resume(mOperationCallback);
        else
        {
            Timer t = new Timer("Music starter");
            t.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    curSong = Global.group.GetSongByIndex(nextState.songIndex);
                    spotifyTimer = null;
                    spotifyImmediatePlay(startTime);
                }
            }, diff);
        }
    }

    private void spotifyImmediatePlay(final Calendar startTime)
    {
        int diff = (int)(Calendar.getInstance().getTimeInMillis() - nextState.startTime.getTimeInMillis());
        if (diff < 0)
            diff = 0;
        Global.player.playUri(mOperationCallback, Global.group.GetSongByIndex(nextState.songIndex).url, 0, diff);
        curSong = Global.group.GetSongByIndex(nextState.songIndex);
    }

    private void updateOnlineState()
    {
        Database.GetPlayerState(Global.group.id, new Function<PlayerState, Object>()
        {
            @Override
            public Object apply(PlayerState playerState)
            {
                nextState = playerState;
                NextStateCallback();
                return null;
            }
        });
    }

    private void NextStateCallback()
    {
        if (nextState.songIndex != -1)
        {
            //check if song is still playing. If not - update db
            Calendar maxSong = (Calendar)nextState.startTime.clone();
            //maxSong.setTime(nextState.startTime);
            maxSong.add(Calendar.SECOND, Global.group.GetSongByIndex(nextState.songIndex).duration);
            if (maxSong.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
            {
                if (isMaster)
                {
                    Calendar startTime = Calendar.getInstance();
                    startTime.add(Calendar.SECOND, 2);
                    Database.SetNextSong(Global.group.id, (nextState.songIndex + 1) % Global.group.playlist.size(), startTime.getTimeInMillis());
                }
                nextState.songIndex = -1;
                //Database.SetNextSong(Global.group.id, -1, Calendar.getInstance().getTimeInMillis());
                if (curSong != null)
                {
                    curSong = null;
                    Global.player.pause(mOperationCallback);
                }
            }
            else if (Calendar.getInstance().getTimeInMillis() < nextState.startTime.getTimeInMillis())
            {
                //start buffering
                if (spotifyTimer == null)
                    spotifyDelayedPlay(nextState.startTime);
            }
            else
            {
                if (curSong == null && spotifyTimer == null)
                    spotifyImmediatePlay(nextState.startTime);
            }
        }
        else if (curSong != null)
        {
            isMaster = false;
            curSong = null;
            Global.player.pause(mOperationCallback);
        }

        updatePlayerState();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int index, long l)
    {

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
}
