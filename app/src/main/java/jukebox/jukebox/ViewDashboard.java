package jukebox.jukebox;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ViewDashboard extends AppCompatActivity
{
    TextView lWelcome;
    TextView lTime;
    TextView lDistance;
    TextView lSpeed;
    Button bLeaderboardTime;
    Button bLeaderboardDistance;
    Button bLeaderboardSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        setTitle("Dashboard");

        lWelcome = (TextView)findViewById(R.id.DashboardWelcome);
        lTime = (TextView)findViewById(R.id.DashboardTime);
        lDistance = (TextView)findViewById(R.id.DashboardDistance);
        lSpeed = (TextView)findViewById(R.id.DashboardSpeed);
        bLeaderboardTime = (Button)findViewById(R.id.DashboardLeaderboardTime);
        bLeaderboardDistance = (Button)findViewById(R.id.DashboardLeaderboardDistance);
        bLeaderboardSpeed = (Button)findViewById(R.id.DashboardLeaderboardSpeed);

        bLeaderboardTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            { bLeaderboard_OnClick("Time");
            }
        });
        bLeaderboardDistance.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {bLeaderboard_OnClick("Distance");
            }
        });
        bLeaderboardSpeed.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            { bLeaderboard_OnClick("Speed");
            }
        });

        lWelcome.setText("Welcome, " + Global.userName + "!");

        retrieveInformation();
    }

    private void bLeaderboard_OnClick(String type)
    {
        Intent intent = new Intent(this, ViewLeaderboard.class);
        intent.putExtra("type", type);
        startActivity(intent);
    }

    private void retrieveInformation()
    {
        Database.GetUserStats(Global.userID, new Function<double[], Object>()
        {
            @Override
            public Object apply(double[] d)
            {
                onStatsRetrieved(d);
                return null;
            }
        });
    }

    private void onStatsRetrieved(final double[] stats)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                if (stats != null)
                {
                    lTime.setText("Total workout time: " + Utils.TimeToText((long) stats[0]));
                    lDistance.setText("Total distance traveled: " + Utils.Round(stats[1], 2) + "m");
                    lSpeed.setText("Average speed: " + Utils.Round(stats[1] / stats[0], 2) + "m/s");
                }
                else
                {
                    lTime.setText("Total workout time: 00:00:00");
                    lDistance.setText("Total distance traveled: 0m");
                    lSpeed.setText("Average speed: 0m/s");
                }
            }
        });
    }
}
