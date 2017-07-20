package jukebox.jukebox;


//TODO
// This class is taken from a WAYt project and was implemented by JABYS team as part of
// Dalhousie Fall 2016 course CSCI5708 Mobile Computing.

/*
* Original reference:
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

// Class that is responsible for retrieving GPS position
public class Positioning
{
    private final static int PERMISSION_REQUEST = 1;

    private LocationManager locManager; // Android location manager
    private Location lastLocation; // Last retreived location
    private boolean isActive = false; // Is GPS activity currently active?
    private Context context; // Current context
    private Activity activity; // Current activity

    protected Runnable UpdateCallback = null;



    // GPS activity change listener
    private LocationListener locListener = new LocationListener()
    {
        public void onLocationChanged(Location loc)
        {
            updateLocation(loc);
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };



    // ctor
    public Positioning(Context _context, Activity _activity)
    {
        context = _context;
        activity = _activity;
    }

    // Starts GPS activity. Requests permission if necessary
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

    // Stops GPS activity
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

    // Location getter
    public Location GetLocation()
    {
        return lastLocation;
    }



    // Validates location
    private boolean validLocation(Location location)
    {
        // If it takes more than 30 seconds to obtain location then the result is disregarded
        return location != null && SystemClock.elapsedRealtime() - location.getElapsedRealtimeNanos() < 30e9;
    }

    // Checks new location validity and invokes a callback if one is set
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

    // Starts GPS activity
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

        //Start requesting location is possible. This has to be done on main thread
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

    // Actually starts GPS activity
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