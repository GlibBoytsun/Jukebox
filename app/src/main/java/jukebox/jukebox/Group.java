package jukebox.jukebox;

import java.util.ArrayList;

// Data structure to store group information
public class Group
{
    public int id; // Group ID
    public String name; // Group name

    public ArrayList<Song> playlist; // Group playlist

    // Returns a song that is at the specified index
    public Song GetSongByIndex(int index)
    {
        for (int i = 0; i < playlist.size(); i++)
            if (playlist.get(i).index == index)
                return playlist.get(i);
        return null;
    }
}
