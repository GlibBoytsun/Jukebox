package jukebox.jukebox;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

//TODO reference
final class BackgroundService
{
    private static BackgroundService instance = null;

    private Stopwatch stopwatch = null;
    private Positioning positioning = null;
    private boolean isInitialized = false;
    private Function<Location, Object> callback = null;

    public String msg = "";//TODO rm



    private BackgroundService()
    {

    }



    public static BackgroundService GetInstance()
    {
        if (instance == null)
            instance = new BackgroundService();
        return instance;
    }

    void Initialize(Context context, Activity activity, Function<Location, Object> _callback)
    {
        callback = _callback;
        stopwatch = new Stopwatch(context, 5 * 1000, 1 * 1000, stopwatchCallback);
        positioning = new Positioning(context, activity);
        positioning.UpdateCallback = positioningCallback;
        isInitialized = true;
    }

    public void Start()
    {
        //stopwatch.Start();
        stopwatchCallback.run();
    }

    public void Stop()
    {
        stopwatch.Stop();
    }

    public boolean IsInitialized()
    {
        return isInitialized;
    }



    private Runnable stopwatchCallback = new Runnable()
    {
        @Override
        public void run()
        {
            msg = "stopwatch callback";
            stopwatch.Stop();
            positioning.Start();
        }
    };

    private Runnable positioningCallback = new Runnable()
    {
        @Override
        public void run()
        {
            msg = "positioning callback";
            positioning.Stop();
            callback.apply(positioning.GetLocation());
            stopwatch.Start();
        }
    };
}
