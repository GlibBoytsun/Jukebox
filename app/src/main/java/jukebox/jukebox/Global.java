package jukebox.jukebox;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.SpotifyPlayer;


// A place to store all global activity-independent variables
public class Global
{
    public static SpotifyPlayer player; // Plays music
    public static Config playerConfig; // Sets up how the music is played
    public static Group group; // Group that user is currently in
    public static int userID; // User ID
    public static String userName; // Spotify name
    public static BackgroundService bgService; // Background service to periodically retrieve GPS coordinates
}
