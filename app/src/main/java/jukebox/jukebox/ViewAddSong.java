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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ViewAddSong extends AppCompatActivity implements AdapterView.OnItemClickListener
{
    Context context;
    ArrayList<Song> curSongs = null;

    EditText tbFilter;
    Button bSearch;
    TextView lState;
    ListView lbSongs;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addsong);
        setTitle("Add Song");
        context = this;

        tbFilter = (EditText)findViewById(R.id.AddSongFilter);
        bSearch = (Button)findViewById(R.id.AddSongSearch);
        lState = (TextView)findViewById(R.id.AddSongState);
        lbSongs = (ListView)findViewById(R.id.AddSongList);

        bSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bSearch_OnClick();
            }
        });
        lbSongs.setOnItemClickListener(this);
    }

    void bSearch_OnClick()
    {
        if (tbFilter.getText().length() != 0)
        {
            curSongs = null;
            UpdateSongsList();
            SetStateText("Retrieving results...");
            SpotifyWebAPI.SearchSongs(tbFilter.getText().toString(), new Function<ArrayList<Song>, Object>()
            {
                @Override
                public Object apply(ArrayList<Song> songs)
                {
                    OnSongsRetrieved(songs);
                    return null;
                }
            });
        }
    }

    void OnSongsRetrieved(ArrayList<Song> songs)
    {
        curSongs = songs;
        if (songs == null)
        {
            SetStateText("Error while retrieving results");
            return;
        }
        SetStateText("");
        UpdateSongsList();
    }

    void SetStateText(final String s)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                lState.setText(s);
            }
        });
    }

    void UpdateSongsList()
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                List<String> arr = new ArrayList();
                if (curSongs != null)
                    for (int i = 0; i < curSongs.size(); i++)
                        arr.add(curSongs.get(i).artist + " - " + curSongs.get(i).title);

                lbSongs.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_selectable_list_item, arr));
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        if (curSongs != null)
        {
            Database.AddSong(Global.group.id, curSongs.get(i), new Function<Object, Object>()
            {
                @Override
                public Object apply(Object o)
                {
                    new Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            finish();
                        }
                    });
                    return null;
                }
            });
        }
    }
}
