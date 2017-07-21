package jukebox.jukebox;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import java.util.HashMap;
import java.util.Map;

//TODO
// This class is taken from a WAYt project and was implemented by JABYS team as part of
// Dalhousie Fall 2016 course CSCI5708 Mobile Computing.

/**
 Stopwatch that works even when the app is closed. Its tickrate can vary slightly in order to optimize battery usage
 Example of usage

 s = new Stopwatch(ViewMain.this, 5000, 1000, new Runnable()
 {
 @Override
 public void run()
 {
 label.setText(String.valueOf(s.GetTimePassed()));
 }
 });
 s.Start();
 */
public class Stopwatch
{

    static Map<Integer, Stopwatch> activeStopwatches = new HashMap<Integer, Stopwatch>(); // Map that relates all active Stopwatches to their IDs

    // Description of Stopwatch interval. [Min, Min + Duration]
    private int intervalDesired;
    private int intervalMin = 9000;
    private int intervalDuration = 11000;

    private static int idPool = 0;
    private int id = idPool++;
    private int timePassed;
    private boolean isRunning = false;
    private Runnable callback;

    // References to external handlers
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private Context context;



    // ctor
    public Stopwatch(Context _context, int desiredInterval, int maxIntervalDeviation, Runnable _callback)
    {
        context = _context;
        intervalDesired = desiredInterval;
        intervalMin = desiredInterval - maxIntervalDeviation;
        intervalDuration = maxIntervalDeviation * 2;
        callback = _callback;
    }

    //Starts the Stopwatch by registering and scheduling it
    public void Start()
    {
        if (isRunning)
        {
            return;
        }

        isRunning = true;
        activeStopwatches.put(id, this);
        createAlarm();
    }

    // Schedules next tick using AlarmManager
    /*
     * Two functions below are inspired by Android Developers documentation
     * Title: Scheduling Repeating Alarms
     * Availablity: https://developer.android.com/training/scheduling/alarms.html
     */
    private void createAlarm()
    {
        if (alarmManager == null)
            alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("id", id);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setWindow(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + intervalMin, intervalDuration, alarmIntent);
    }

    // Cancels scheduled tick
    private void cancelAlarm()
    {
        alarmManager.cancel(alarmIntent);
    }

    // Stops Stopwatch by de-registering it and canceling scheduled tick
    public void Stop()
    {
        if (!isRunning)
            return;
        cancelAlarm();
        activeStopwatches.remove(id);
        isRunning = false;
        timePassed = 0;
    }

    // Tick handler. Invokes callback if one is set
    void tick()
    {
        timePassed += intervalDesired;
        createAlarm();
        if (callback != null)
            callback.run();
    }
}