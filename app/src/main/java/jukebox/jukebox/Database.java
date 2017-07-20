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


// Class for handling all database queries. All interactions are done through a REST service
// All queries are executed in separate threads. Queries are encoded and passed as part of URL.
// Data is returned in JSON format. It is parsed and a callback function is called with obtained data.
public class Database
{
    private Database() { } //Thank you, Java, for not having static classes /s



    // Opens and initializes a connection to the REST service
    private static URLConnection connectionOpen()
    {
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



    // Retrieves a list of all registered groups
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

    // Retrieves a playlist of a given group
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

    // Retrieves player state for a given group (which song is playing and when did/will it start playing)
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
                            "SELECT jukebox.playlists.nextSongIndex as ind, UNIX_TIMESTAMP(jukebox.playlists.nextSongStartTime) as time, UNIX_TIMESTAMP(NOW()) as curTime FROM jukebox.playlists WHERE groupid = " + String.valueOf(groupID) + ";",
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
                    result.curTime = Calendar.getInstance();
                    result.curTime.setTime(new Date(1000l * jo.getLong("curTime")));

                    callback.apply(result);
                }
                catch (Exception e)
                {
                    callback.apply(null);
                }
            }
        }).start();
    }

    // Sets next song to be played
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

    // Adds specified song to the playlist
    public static void AddSong(final int groupID, final Song song, final Function<Object, Object> callback)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    URLConnection connection = connectionOpen();
                    String data = URLEncoder.encode("input", "UTF-8") + "=" + URLEncoder.encode(
                            "insert into jukebox.songs(title, artist, duration, URL) values (\"" + song.title + "\", \"" + song.artist + "\", " + song.duration + ", \"" + song.url + "\");",
                            "UTF-8");
                    OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String s = "", t;
                    while((t = reader.readLine()) != null)
                        s += t;

                    connection = connectionOpen();
                    data = URLEncoder.encode("input", "UTF-8") + "=" + URLEncoder.encode(
                            "select id from jukebox.songs where URL like \"" + song.url + "\";",
                            "UTF-8");
                    wr = new OutputStreamWriter(connection.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    s = "";
                    while((t = reader.readLine()) != null)
                        s += t;
                    int id = new JSONArray(s).getJSONObject(0).getInt("id");

                    connection = connectionOpen();
                    data = URLEncoder.encode("input", "UTF-8") + "=" + URLEncoder.encode(
                            "select max(songIndex) as ind from jukebox.playlistSongs where playlistID = " + groupID + "",
                            "UTF-8");
                    wr = new OutputStreamWriter(connection.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    s = "";
                    while((t = reader.readLine()) != null)
                        s += t;
                    int ind = 0;
                    try
                    {
                        ind = new JSONArray(s).getJSONObject(0).getInt("ind");
                    }
                    catch(Exception e)
                    {

                    }

                    connection = connectionOpen();
                    data = URLEncoder.encode("input", "UTF-8") + "=" + URLEncoder.encode(
                            "insert into jukebox.playlistSongs (playlistID, songID, songIndex) values (" + groupID + ", " +
                                    id + ", " + ind + ");",
                            "UTF-8");
                    wr = new OutputStreamWriter(connection.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    s = "";
                    while((t = reader.readLine()) != null)
                        s += t;

                    callback.apply(null);
                }
                catch (Exception e)
                {
                    callback.apply(null);
                }
            }
        }).start();
    }

    // Gets user ID that is associated with the given Spotify account
    public static void GetUserID(final String UID, final String name, final Function<Integer, Object> callback)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    URLConnection connection = connectionOpen();
                    String data = URLEncoder.encode("input", "UTF-8") + "=" + URLEncoder.encode(
                            "INSERT INTO jukebox.users (Name, UID) " +
                                    "select \"" + name + "\", \"" + UID + "\" from dual " +
                                    "WHERE not exists (select 1 from jukebox.users where UID like \"" + UID + "\");",
                            "UTF-8");
                    OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String s = "", t;
                    while((t = reader.readLine()) != null)
                        s += t;

                    connection = connectionOpen();
                    data = URLEncoder.encode("input", "UTF-8") + "=" + URLEncoder.encode(
                            "select id from jukebox.users where UID like \"" + UID + "\";",
                            "UTF-8");
                    wr = new OutputStreamWriter(connection.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    s = "";
                    while((t = reader.readLine()) != null)
                        s += t;
                    int id = new JSONArray(s).getJSONObject(0).getInt("id");

                    callback.apply(id);
                }
                catch (Exception e)
                {
                    callback.apply(null);
                }
            }
        }).start();
    }

    // Retrieves user statistics for all groups
    public static void GetUserStats(final int userID, final Function<double[], Object> callback)
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
                            "select sum(time) as time, sum(distance) as distance from jukebox.data " +
                                    "where userID = " + String.valueOf(userID) + ";",
                            "UTF-8");
                    OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String s = "", t;
                    while((t = reader.readLine()) != null)
                        s += t;

                    JSONObject jo = new JSONArray(s).getJSONObject(0);
                    double time = jo.getDouble("time");
                    double distance = jo.getDouble("distance");

                    callback.apply(new double[] { time, distance });
                }
                catch (Exception e)
                {
                    callback.apply(null);
                }
            }
        }).start();
    }

    // Retrieves leaderboard for a given group. Supports multiple sorting types (distance, time, speed)
    public static void GetLeaderboard(final int groupID, final String type, final Function<ArrayList<UserStats>, Object> callback)
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
                            "select users.name, data.time, data.distance, (data.distance / data.time) as speed from jukebox.data join jukebox.users on data.userID = users.ID " +
                                    "where data.groupID = " + groupID + " order by " + type.toLowerCase() + " desc;",
                            "UTF-8");
                    OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String s = "", t;
                    while((t = reader.readLine()) != null)
                        s += t;

                    ArrayList<UserStats> result = new ArrayList<>();
                    JSONArray arr = new JSONArray(s);
                    JSONObject jo;
                    for (int i = 0; i < arr.length(); i++)
                    {
                        jo = arr.getJSONObject(i);
                        UserStats v = new UserStats();
                        v.name = jo.getString("name");
                        v.time = jo.getDouble("time");
                        v.distance = jo.getDouble("distance");
                        try
                        {
                            v.speed = jo.getDouble("speed");
                        }
                        catch(Exception e)
                        {

                        }
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

    // Submits workout statistics
    public static void PostData(final int UserID, final int GroupID, final String songs, final String locations, final int time, final double distance, final Function<Object, Object> callback)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    URLConnection connection = connectionOpen();
                    String data = URLEncoder.encode("input", "UTF-8") + "=" + URLEncoder.encode(
                            "insert into jukebox.data (userID, groupID, dateEnd, songs, locationHistory, time, distance) " +
                                    "values (" + UserID + ", " + GroupID + ", now(), \"" + songs + "\", \"" + locations + "\", " + time + ", " + distance + ");",
                            "UTF-8");
                    OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while(reader.readLine() != null) ;

                    if (callback != null)
                        callback.apply(null);
                }
                catch (Exception e)
                {
                    if (callback != null)
                        callback.apply(null);
                }
            }
        }).start();
    }
}
