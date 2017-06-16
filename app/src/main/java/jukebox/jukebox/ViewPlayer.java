package jukebox.jukebox;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
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
                curSong = songs.get(0);

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

    private void updatePlayerState()
    {
        if (curSong == null)
        {
            lCurrentSong.setText("");
            lSongTime.setText("");
            bPlayStop.setText("Play");
        }
        else
        {
            lCurrentSong.setText("Currently playing: " + curSong.artist + " - " + curSong.title);
            String s = String.valueOf(curSong.duration % 60);
            if (s.length() == 1)
                s = "0" + s;
            lSongTime.setText("0:00" + " / " + String.valueOf(curSong.duration / 60) + ":" + s);
            bPlayStop.setText("Play");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int index, long l)
    {

    }
}
