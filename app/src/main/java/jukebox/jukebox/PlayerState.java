package jukebox.jukebox;

import java.util.Calendar;

// Data structure to store player state
public class PlayerState
{
    public int songIndex; // Index of song to be played
    public Calendar startTime; // When the song should start / started playing
    public Calendar curTime; // Current server time. Used for synchronizing playback.
}
