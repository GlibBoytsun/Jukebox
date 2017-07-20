package jukebox.jukebox;

// Data structure for storing song information
public class Song
{
    public int id; // Song ID (in database)
    public int index; // Song index in playlist
    public String title; // Song title
    public String artist = ""; // Song artist
    public int duration; // Song duration in seconds
    public String url; // Spotify song URI
}
