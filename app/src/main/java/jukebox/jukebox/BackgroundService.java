package jukebox.jukebox;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

//TODO
// This class is taken from a WAYt project and was implemented by JABYS team as part of
// Dalhousie Fall 2016 course CSCI5708 Mobile Computing.

// This class is responsible for periodically obtaining GPS data.
final class BackgroundService
{
    private static BackgroundService instance = null;// Singleton

    private Stopwatch stopwatch = null;
    private Positioning positioning = null;
    private Function<Location, Object> callback = null;



    private BackgroundService() { } // Singleton



    public static BackgroundService GetInstance() // Singleton
    {
        if (instance == null)
            instance = new BackgroundService();
        return instance;
    }

    // Initializes BackgroundService by creating a stopwatch and initializing GPS handler
    void Initialize(Context context, Activity activity, Function<Location, Object> _callback)
    {
        callback = _callback;
        stopwatch = new Stopwatch(context, 5 * 1000, 1 * 1000, stopwatchCallback);
        positioning = new Positioning(context, activity);
        positioning.UpdateCallback = positioningCallback;
    }

    // Starts the BackgroundService activity. Tries to obtain position right away
    public void Start()
    {
        //stopwatch.Start();
        stopwatchCallback.run();
    }

    // Stops the BackgroundService activity
    public void Stop()
    {
        stopwatch.Stop();
    }



    // Stopwatch callback. Stops the stopwatch and starts GPS activity
    private Runnable stopwatchCallback = new Runnable()
    {
        @Override
        public void run()
        {
            stopwatch.Stop();
            positioning.Start();
        }
    };

    // GPS handler callback. Stops GPS activity and resumes the stopwatch
    private Runnable positioningCallback = new Runnable()
    {
        @Override
        public void run()
        {
            positioning.Stop();
            callback.apply(positioning.GetLocation());
            stopwatch.Start();
        }
    };
}
