package jukebox.jukebox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//TODO
// This class is taken from a WAYt project and was implemented by JABYS team as part of
// Dalhousie Fall 2016 course CSCI5708 Mobile Computing.

// This class is responsible for handling Stopwatch ticks (see Stopwatch class)
public class AlarmReceiver extends BroadcastReceiver
{
    // Tick receiver. Determines which stopwatch has ticked and invokes corresponding method
    @Override
    public void onReceive(Context context, Intent intent)
    {
        int id = intent.getIntExtra("id", -1);
        if (id == -1)
        {
            return;
        }

        Stopwatch s = Stopwatch.activeStopwatches.get(id);
        if (s == null)
        {
            return;
        }

        s.tick();
    }
}