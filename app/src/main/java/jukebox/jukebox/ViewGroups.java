package jukebox.jukebox;

import android.content.Context;
import android.content.Intent;
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


public class ViewGroups extends AppCompatActivity implements AdapterView.OnItemClickListener
{
    Context context;// Current context
    ArrayList<Group> groups;// List of retrieved groups

    //UI elements
    TextView lInfo;
    Button bRetry;
    ListView lbGroups;

    //Initialize view, retrieve all UI elements and register event handlers
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        setTitle("Groups");

        context = this;
        lInfo = (TextView)findViewById(R.id.GroupsInfo);
        bRetry = (Button)findViewById(R.id.GroupsRetry);
        lbGroups = (ListView)findViewById(R.id.GroupsList);

        lbGroups.setOnItemClickListener(this);

        lInfo.setVisibility(View.VISIBLE);
        bRetry.setVisibility(View.INVISIBLE);
        lbGroups.setVisibility(View.INVISIBLE);

        retrieveUser();
        updateGroups();
    }

    // Registers / logins current Spotify user. Obtains user's Spotify name and ID
    private void retrieveUser()
    {
        SpotifyWebAPI.GetUserName(new Function<String[], Object>()
        {
            @Override
            public Object apply(String[] s)
            {
                userNameRetrieved(s[0], s[1]);
                return null;
            }
        });
    }

    // Registers / logins current Spotify user. Callback for Spotify ID. Stores user's Spotify name and ID and queries Database for their ID
    private void userNameRetrieved(String id, String name)
    {
        Global.userName = name;
        Database.GetUserID(id, name, new Function<Integer, Object>()
        {
            @Override
            public Object apply(Integer i)
            {
                userIDRetrieved(i);
                return null;
            }
        });
    }

    // Callback for Database ID. Stores user's ID
    private void userIDRetrieved(int id)
    {
        Global.userID = id;
    }

    // Retrieves list of registered groups from the Database
    private void updateGroups()
    {
        Database.GetGroups(new Function<ArrayList<Group>, Object>()
        {
            //Saves retrieved list and updates ListView
            @Override
            public Object apply(ArrayList<Group> g)
            {
                groups = g;

                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (groups == null)
                        {
                            bRetry.setVisibility(View.VISIBLE);
                            lInfo.setText("Couldn't retrieve list of groups");
                            return;
                        }

                        lInfo.setVisibility(View.INVISIBLE);
                        lbGroups.setVisibility(View.VISIBLE);
                        List<String> arr = new ArrayList();
                        for (int i = 0; i < groups.size(); i++)
                            arr.add(groups.get(i).name);

                        lbGroups.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_selectable_list_item, arr));
                    }
                });
                return null;
            }
        });
    }

    // Retry button click handler. Re-retrieves list of groups
    public void bRetry_OnClick(View v)
    {
        lInfo.setText("Retrieving list of groups...");
        bRetry.setVisibility(View.INVISIBLE);
        updateGroups();
    }

    //ListView item click handler. Retrieves selected group and navigates user to the Player view
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int index, long l)
    {
        Global.group = groups.get(index);
        Intent intent = new Intent(this, ViewPlayer.class);
        startActivity(intent);
    }
}
