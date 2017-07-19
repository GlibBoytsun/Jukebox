package jukebox.jukebox;

//TODO reference
/*
* Logic to how to obtain current position
* Title: Obtain position via GPS
* Author: Metcalfe.C
* Date: 2016
* Code version: 2.0
* Availability: https://github.com/pR0Ps/LocationShare
*/

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

public class Positioning
{
    private final static int PERMISSION_REQUEST = 1;

    private LocationManager locManager;
    private Location lastLocation;
    private boolean isActive = false;
    private boolean hasPermission = false;
    private Context context;
    private Activity activity;

    protected Runnable UpdateCallback = null;



    private LocationListener locListener = new LocationListener()
    {
        public void onLocationChanged(Location loc)
        {
            updateLocation(loc);
        }

        public void onProviderEnabled(String provider)
        {
            hasPermission = true;
        }

        public void onProviderDisabled(String provider)
        {
            hasPermission = false;
        }

        public void onStatusChanged(String provider, int status, Bundle extras)
        {
        }
    };



    public Positioning(Context _context, Activity _activity)
    {
        context = _context;
        activity = _activity;
    }

    public void Start()
    {
        try
        {
            if (isActive)
                return;
            if (locManager == null)
                locManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
            startRequestingLocation();
            isActive = true;
        }
        catch(Exception e)
        {
            int asdf = 0;
        }
    }

    public void Stop()
    {
        try
        {
            locManager.removeUpdates(locListener);
        }
        catch (SecurityException ignored)
        {
        }
        isActive = false;
    }

    public Location GetLocation()
    {
        return lastLocation;
    }

    public boolean IsActive()
    {
        return isActive;
    }

    public boolean HasPermission()
    {
        return hasPermission;
    }



    private boolean validLocation(Location location)
    {
        // If it takes more than 30 seconds to obtain location then the result is disregarded
        return location != null && SystemClock.elapsedRealtime() - location.getElapsedRealtimeNanos() < 30e9;
    }

    private void updateLocation(Location location)
    {
        // Check if location is obtained
        boolean locationEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean waitingForLocation = locationEnabled && !validLocation(location);
        boolean haveLocation = locationEnabled && !waitingForLocation;

        if (haveLocation)
        {
            lastLocation = location;
            if (UpdateCallback != null)
                UpdateCallback.run();
        }
    }

    /*
    public void openLocationSettings(View view)
    {
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }//*/

    private void startRequestingLocation()
    {
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (activity == null)//TODO request permission
            {
                isActive = false;
                return;
            }
            activity.requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_REQUEST);
            return;
        }

        //Start requesting location is possible

        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                doStart();
            }
        });
        //locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
    }

    private void doStart()
    {
        try
        {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
        }
        catch (SecurityException e)
        {
            int asdf = 0;
        }
    }
}