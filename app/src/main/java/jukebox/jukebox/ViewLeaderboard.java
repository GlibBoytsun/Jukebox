package jukebox.jukebox;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ViewLeaderboard extends AppCompatActivity
{
    ListView lbList;

    String type;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        type = getIntent().getStringExtra("type");
        setTitle("Leaderboard (" + type + ")");
        context = this;

        lbList = (ListView)findViewById(R.id.LeaderboardList);

        retrieveLeaderboard();
    }

    private void retrieveLeaderboard()
    {
        Database.GetLeaderboard(Global.group.id, type, new Function<ArrayList<UserStats>, Object>()
        {
            @Override
            public Object apply(final ArrayList<UserStats> s)
            {
                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        leaderboardRetrieved(s);
                    }
                });
                return null;
            }
        });
    }

    private void leaderboardRetrieved(ArrayList<UserStats> stats)
    {
        List<String> arr = new ArrayList();
        for (int i = 0; i < stats.size(); i++)
            arr.add(stats.get(i).name +
                    "\r\nTime: " + Utils.TimeToText((long)stats.get(i).time) +
                    "\r\nDistance: " + Utils.Round(stats.get(i).distance, 2) + "m" +
                    "\r\nSpeed: " + Utils.Round(stats.get(i).speed, 2) + "m/s");

        lbList.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, arr));
    }
}
