package jukebox.jukebox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//TODO reference
public class AlarmReceiver extends BroadcastReceiver
{

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