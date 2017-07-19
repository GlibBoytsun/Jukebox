package jukebox.jukebox;


import android.content.Context;
import android.content.Intent;
import android.location.Location;
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


//TODO timed playlist updates
public class ViewPlayer extends AppCompatActivity implements AdapterView.OnItemClickListener
{
    Context context;
    TextView lInfo;
    Button bRetry;
    ListView lbPlaylist;
    TextView lCurrentSong;
    TextView lSongTime;
    Button bPlayStop;
    Button bNextTrack;
    Button bAddSongs;
    Button bDashboard;

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
        setTitle("Player");
        //init();

        if (Global.bgService == null)
        {
            Global.bgService = BackgroundService.GetInstance();
            Global.bgService.Initialize(this, this, new Function<Location, Object>()
            {
                @Override
                public Object apply(Location location)
                {
                    locationCallback(location);
                    return null;
                }
            });
        }
    }

    void init()
    {
        context = this;
        lInfo = (TextView)findViewById(R.id.PlayerInfo);
        bRetry = (Button)findViewById(R.id.PlayerRetry);
        lbPlaylist = (ListView)findViewById(R.id.PlayerPlaylist);
        lCurrentSong = (TextView)findViewById(R.id.PlayerCurrentSong);
        lSongTime = (TextView)findViewById(R.id.PlayerTime);
        bPlayStop = (Button)findViewById(R.id.PlayerPlayStop);
        bNextTrack = (Button)findViewById(R.id.PlayerNextTrack);
        bAddSongs = (Button)findViewById(R.id.PlayerAddSongs);
        bDashboard = (Button)findViewById(R.id.PlayerDashboard);

        bAddSongs.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bAddSong_OnClick();
            }
        });
        bDashboard.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bDashboard_OnClick();
            }
        });
        lbPlaylist.setOnItemClickListener(this);

        lInfo.setVisibility(View.VISIBLE);
        bRetry.setVisibility(View.INVISIBLE);
        lbPlaylist.setVisibility(View.INVISIBLE);
        lCurrentSong.setVisibility(View.INVISIBLE);
        lSongTime.setVisibility(View.INVISIBLE);
        bPlayStop.setVisibility(View.INVISIBLE);
        bNextTrack.setVisibility(View.INVISIBLE);
        bAddSongs.setVisibility(View.INVISIBLE);
        bDashboard.setVisibility(View.INVISIBLE);

        getPlaylist();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        init();
    }

    void bAddSong_OnClick()
    {
        Intent intent = new Intent(this, ViewAddSong.class);
        startActivity(intent);
    }

    void bDashboard_OnClick()
    {
        Intent intent = new Intent(this, ViewDashboard.class);
        startActivity(intent);
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
                        PlaylistRetrieved();
                    }
                });

                return null;
            }
        });
    }

    void PlaylistRetrieved()
    {
        ArrayList<Song> songs = Global.group.playlist;

        if (songs == null)
        {
            bRetry.setVisibility(View.VISIBLE);
            lInfo.setText("Couldn't retrieve playlist");
            return;
        }

        lInfo.setVisibility(View.INVISIBLE);
        bRetry.setVisibility(View.INVISIBLE);
        lbPlaylist.setVisibility(View.VISIBLE);
        lCurrentSong.setVisibility(View.VISIBLE);
        lSongTime.setVisibility(View.VISIBLE);
        bPlayStop.setVisibility(View.VISIBLE);
        bNextTrack.setVisibility(View.VISIBLE);
        bAddSongs.setVisibility(View.VISIBLE);
        bDashboard.setVisibility(View.VISIBLE);
        bNextTrack.setActivated(false);

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

    public void bRetry_OnClick(View v)
    {
        lInfo.setText("Retrieving playlist...");
        bRetry.setVisibility(View.INVISIBLE);
        getPlaylist();
    }

    public void bPlayStop_OnClick(View v)
    {
        bNextTrack.setActivated(false);
        if (bPlayStop.getText() == "...")
            return;
        else if (bPlayStop.getText() == "Play")
        {
            isMaster = true;
            Calendar startTime = (Calendar)nextState.curTime.clone();
            startTime.add(Calendar.SECOND, 5);
            Database.SetNextSong(Global.group.id, 0, startTime.getTimeInMillis());
            bAddSongs.setEnabled(false);
            bDashboard.setEnabled(false);
        }
        else if (bPlayStop.getText() == "Stop")
        {
            Global.player.pause(mOperationCallback);
            Log.d("D", "p1");
            Database.SetNextSong(Global.group.id, -1, Calendar.getInstance().getTimeInMillis());
            curSong = null;
            trackingStop();
            nextState.songIndex = -1;
            bNextTrack.setActivated(true);
            bAddSongs.setEnabled(true);
            bDashboard.setEnabled(true);
        }
    }

    public void bNextTrack_OnClick(View v)
    {
        if (nextState == null)
            return;
        Calendar startTime = (Calendar)nextState.curTime.clone();
        startTime.add(Calendar.SECOND, 3);
        Database.SetNextSong(Global.group.id, (nextState.songIndex + 1) % Global.group.playlist.size(), startTime.getTimeInMillis());
        bNextTrack.setActivated(false);
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
                        long diff = nextState.startTime.getTimeInMillis() - nextState.curTime.getTimeInMillis();
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
                        curTime = (int) (st.positionMs / 1000);
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
        Log.d("D", "delayed");
        Global.player.playUri(mOperationCallback, Global.group.GetSongByIndex(nextState.songIndex).url, 0, 0);
        Global.player.pause(mOperationCallback);

        if (spotifyTimer != null)
            return;

        long diff = nextState.startTime.getTimeInMillis() - nextState.curTime.getTimeInMillis();
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
                    Global.player.resume(mOperationCallback);
                    trackingStart();
                }
            }, diff);
        }
    }

    private void spotifyImmediatePlay()
    {
        Log.d("D", "immediate");
        int diff = (int)(nextState.curTime.getTimeInMillis() - nextState.startTime.getTimeInMillis());
        if (diff < 0)
            diff = 0;
        Global.player.playUri(mOperationCallback, Global.group.GetSongByIndex(nextState.songIndex).url, 0, diff);
        curSong = Global.group.GetSongByIndex(nextState.songIndex);
        trackingStart();
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

    boolean lock = false;
    private void NextStateCallback()
    {
        while (lock) ;
        lock = true;
        try
        {
            if (nextState != null && nextState.songIndex != -1)
            {
                //check if song is still playing. If not - update db
                Calendar maxSong = (Calendar) nextState.startTime.clone();
                //maxSong.setTime(nextState.startTime);
                try
                {
                    maxSong.add(Calendar.SECOND, Global.group.GetSongByIndex(nextState.songIndex).duration);
                }
                catch (Exception e)
                {

                }
                if (maxSong.getTimeInMillis() < nextState.curTime.getTimeInMillis())
                {
                    Log.d("D", "1");
                    if (isMaster)
                    {
                        Log.d("D", "11");
                        bDashboard.setEnabled(false);
                        Calendar startTime = (Calendar) nextState.curTime.clone();
                        startTime.add(Calendar.SECOND, 2);
                        Database.SetNextSong(Global.group.id, (nextState.songIndex + 1) % Global.group.playlist.size(), startTime.getTimeInMillis());
                    }
                    Log.d("D", "12");
                    nextState.songIndex = -1;
                    if (curSong != null)
                    {
                        Log.d("D", "13");
                        curSong = null;
                        Global.player.pause(mOperationCallback);
                        bDashboard.setEnabled(true);
                        trackingStop();
                        Log.d("D", "p2");
                    }
                }
                else if (nextState.curTime.getTimeInMillis() < nextState.startTime.getTimeInMillis())
                {
                    Log.d("D", "2");
                    //start buffering
                    if (spotifyTimer == null)
                        spotifyDelayedPlay(nextState.startTime);
                }
                else
                {
                    Log.d("D", "3");
                    if (curSong == null && spotifyTimer == null)
                        spotifyImmediatePlay();
                }
            }
            else if (curSong != null)
            {
                Log.d("D", "4");
                isMaster = false;
                curSong = null;
                bDashboard.setEnabled(true);
                Global.player.pause(mOperationCallback);
                trackingStop();
                Log.d("D", "p3");
            }
        }
        catch (Exception e)
        {
            int asdf = 0;
        }

        lock = false;
        updatePlayerState();
    }



    ArrayList<Location> locations = new ArrayList<>();
    ArrayList<Integer> songs = new ArrayList<>();
    long time = 0;
    Calendar lastCallback = null;

    private void trackingStart()
    {
        lastCallback = Calendar.getInstance();
        Global.bgService.Start();
    }

    private void trackingStop()
    {
        Global.bgService.Stop();

        if (locations.size() != 0)
        {
            double distance = 0;
            String locs = "";
            String s = "";

            locs = locations.get(0).getLatitude() + "," + locations.get(0).getLongitude();
            for (int i = 1; i < locations.size(); i++)
            {
                locs += ";" + locations.get(i).getLatitude() + "," + locations.get(i).getLongitude();
                distance += Utils.GPSDistance(locations.get(i - 1).getLatitude(), locations.get(i - 1).getLongitude(), locations.get(i).getLatitude(), locations.get(i).getLongitude());
            }

            if (songs.size() != 0)
            {
                s = songs.get(0).toString();
                for (int i = 1; i < songs.size(); i++)
                    s += ";" + songs.get(i).toString();
            }

            Database.PostData(Global.userID, Global.group.id, s, locs, (int)(time / 1000), distance, null);
        }

        trackingReset();
    }

    private void trackingReset()
    {
        time = 0;
        lastCallback = null;
        locations.clear();
        songs.clear();
    }

    private void locationCallback(Location l)
    {
        if (curSong == null)
            return;
        locations.add(new Location(l));
        songs.add(curSong.id);
        Calendar now = Calendar.getInstance();
        time += now.getTimeInMillis() - lastCallback.getTimeInMillis();
        lastCallback = now;
    }



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int index, long l)
    {

    }

    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onError(Error error) {

        }
    };
}
