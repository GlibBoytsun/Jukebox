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
import java.util.function.Function;

public class ViewGroups extends AppCompatActivity implements AdapterView.OnItemClickListener
{
    Context context;
    TextView lInfo;
    Button bRetry;
    ListView lbGroups;
    ArrayList<Group> groups;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        context = this;
        lInfo = (TextView)findViewById(R.id.GroupsInfo);
        bRetry = (Button)findViewById(R.id.GroupsRetry);
        lbGroups = (ListView)findViewById(R.id.GroupsList);

        lbGroups.setOnItemClickListener(this);

        lInfo.setVisibility(View.VISIBLE);
        bRetry.setVisibility(View.INVISIBLE);
        lbGroups.setVisibility(View.INVISIBLE);

        updateGroups();
    }

    private void updateGroups()
    {
        Database.GetGroups(new Function<ArrayList<Group>, Object>()
        {
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

    public void bRetry_OnClick(View v)
    {
        lInfo.setText("Retrieving list of groups...");
        bRetry.setVisibility(View.INVISIBLE);
        updateGroups();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int index, long l)
    {
        Global.group = groups.get(index);
        Intent intent = new Intent(this, ViewPlayer.class);
        startActivity(intent);
    }
}
