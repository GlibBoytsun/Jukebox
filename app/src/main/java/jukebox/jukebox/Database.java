package jukebox.jukebox;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;

public class Database
{
    private Database() { } //Thank you, Java, for not having static classes /s



    private static URLConnection connectionOpen()
    {
        //if (connection != null)
        //    return;

        try
        {
            URL url = new URL("http://lowcost-env.8c9rdb3rdt.us-east-1.elasticbeanstalk.com/");
            URLConnection connection = url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            return connection;
        }
        catch (Exception e)
        {
            return null;
        }
    }



    public static void GetGroups(final Function<ArrayList<Group>, Object> callback)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                URLConnection connection = connectionOpen();

                ArrayList<Group> groups = new ArrayList<>();

                try
                {
                    String data = URLEncoder.encode("input", "UTF-8") + "=" + URLEncoder.encode("SELECT * FROM jukebox.groups;", "UTF-8");
                    OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String s = "", t;
                    while((t = reader.readLine()) != null)
                        s += t;
                    JSONArray arr = new JSONArray(s);
                    JSONObject jo;
                    for (int i = 0; i < arr.length(); i++)
                    {
                        Group g = new Group();
                        jo = arr.getJSONObject(i);
                        g.id = jo.getInt("id");
                        g.name = jo.getString("name");
                        groups.add(g);
                    }

                    callback.apply(groups);
                }
                catch (Exception e)
                {
                    callback.apply(null);
                }
            }
        }).start();
    }

    public static void GetPlaylist(final int groupID, final Function<ArrayList<Song>, Object> callback)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                URLConnection connection = connectionOpen();

                ArrayList<Song> result = new ArrayList<>();

                try
                {
                    String data = URLEncoder.encode("input", "UTF-8") + "=" + URLEncoder.encode(
                            "SELECT jukebox.songs.id as id, jukebox.playlistSongs.songIndex as ind, jukebox.songs.title as title, jukebox.songs.artist as artist, jukebox.songs.duration as duration, jukebox.songs.url as url FROM jukebox.playlists " +
                            "JOIN jukebox.playlistSongs ON jukebox.playlists.id = jukebox.playlistSongs.playlistID " +
                            "JOIN jukebox.songs ON jukebox.playlistSongs.songID = jukebox.songs.id " +
                            "WHERE jukebox.playlists.groupid = " + String.valueOf(groupID) + ";",
                            "UTF-8");
                    OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String s = "", t;
                    while((t = reader.readLine()) != null)
                        s += t;
                    JSONArray arr = new JSONArray(s);
                    JSONObject jo;
                    for (int i = 0; i < arr.length(); i++)
                    {
                        jo = arr.getJSONObject(i);
                        Song v = new Song();
                        v.id = jo.getInt("id");
                        v.index = jo.getInt("ind");
                        v.title = jo.getString("title");
                        v.artist = jo.getString("artist");
                        v.duration = jo.getInt("duration");
                        v.url = jo.getString("url");
                        result.add(v);
                    }

                    callback.apply(result);
                }
                catch (Exception e)
                {
                    callback.apply(null);
                }
            }
        }).start();
    }

    public static void GetPlayerState(final int groupID, final Function<PlayerState, Object> callback)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                URLConnection connection = connectionOpen();

                PlayerState result = null;

                try
                {
                    String data = URLEncoder.encode("input", "UTF-8") + "=" + URLEncoder.encode(
                            "SELECT jukebox.playlists.nextSongIndex as ind, UNIX_TIMESTAMP(jukebox.playlists.nextSongStartTime) as time FROM jukebox.playlists WHERE groupid = " + String.valueOf(groupID) + ";",
                            "UTF-8");
                    OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String s = "", t;
                    while((t = reader.readLine()) != null)
                        s += t;
                    JSONArray arr = new JSONArray(s);
                    JSONObject jo = arr.getJSONObject(0);
                    result = new PlayerState();
                    result.songIndex = jo.getInt("ind");
                    if (result.songIndex != -1)
                    {
                        result.startTime = Calendar.getInstance();
                        result.startTime.setTime(new Date(1000l * jo.getLong("time")));
                    }

                    callback.apply(result);
                }
                catch (Exception e)
                {
                    callback.apply(null);
                }
            }
        }).start();
    }

    public static void SetNextSong(final int groupID, final int nextIndex, final long nextStartTime)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                URLConnection connection = connectionOpen();

                try
                {
                    String data = URLEncoder.encode("input", "UTF-8") + "=" + URLEncoder.encode(
                            "UPDATE jukebox.playlists SET nextSongIndex = " + String.valueOf(nextIndex) + ", nextSongStartTime = FROM_UNIXTIME(" + String.valueOf(nextStartTime / 1000) + ") WHERE groupid = " + String.valueOf(groupID) + ";",
                            "UTF-8");
                    OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                }
                catch (Exception e)
                {
                    int asdf = 0;
                }
            }
        }).start();
    }
}
