package jukebox.jukebox;

import java.util.ArrayList;

public class Group
{
    public int id;
    public String name;

    public ArrayList<Song> playlist;

    public Song GetSongByIndex(int index)
    {
        for (int i = 0; i < playlist.size(); i++)
            if (playlist.get(i).index == index)
                return playlist.get(i);
        return null;
    }
}
