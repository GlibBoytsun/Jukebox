package jukebox.jukebox;

// Static utility class
public class Utils
{
    private Utils() { } //Thank you, Java, for not having static classes /s


    // Method for calculating difference between two sets of GPS coordinates in meters
    /*
    * Original reference:
    * Title: Calculate distance in meters when you know longitude and latitude in java [duplicate]
    * Author: Espen Herseth Halvorsen
    * Date: May 8, 2009
    * Availability: https://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java
    */
    //
    public static double GPSDistance(double lat1, double lng1, double lat2, double lng2)
    {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c;
    }

    // Method for converting number of seconds into pretty text
    public static String TimeToText(long sec)
    {
        int s = (int)(sec % 60);
        int m = (int)(sec / 60);
        int h = (int)(sec / 3600);

        String r, t;

        t = String.valueOf(s);
        if (t.length() == 1)
            t = "0" + t;
        r = t;

        t = String.valueOf(m);
        if (t.length() == 1)
            t = "0" + t;
        r = t + ":" + r;

        t = String.valueOf(h);
        if (t.length() == 1)
            t = "0" + t;
        r = t + ":" + r;

        return r;
    }

    // Function that rounds given number to a specified number of digits after decimal point
    public static double Round(double v, int d)
    {
        return ((double)Math.round(v * Math.pow(10, d))) / Math.pow(10, d);
    }
}
