package jukebox.jukebox;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;


// Class for handling all Spotify queries. All interactions are done through a REST API
// All queries are executed in separate threads. Queries are encoded and passed as part of URL.
// Data is returned in JSON format. It is parsed and a callback function is called with obtained data.
public class SpotifyWebAPI
{
    // Creates a connection to the Spotify REST API and sets user OAuth token
    private static URLConnection connectionOpen(String surl, String params)
    {
        try
        {
            URL url = new URL(surl + (params == "" ? "" : ("?" + params)));
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + Global.playerConfig.oauthToken);
            return connection;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    // Returns a list of songs that correspond to a specified search query
    public static void SearchSongs(final String filter, final Function<ArrayList<Song>, Object> callback)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    String data = "type=track&q=" + URLEncoder.encode(filter, "UTF-8");
                    URLConnection connection = connectionOpen("https://api.spotify.com/v1/search", data);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String s = "", t;
                    while((t = reader.readLine()) != null)
                        s += t;

                    ArrayList<Song> result = new ArrayList<Song>();
                    JSONObject trunk = new JSONObject(s).getJSONObject("tracks");
                    JSONArray tracks = trunk.getJSONArray("items");
                    for (int i = 0; i < tracks.length(); i++)
                    {
                        Song ns = new Song();
                        JSONObject song = tracks.getJSONObject(i);
                        JSONArray artists = song.getJSONArray("artists");
                        for (int j = 0; j < artists.length(); j++)
                            ns.artist += (j == 0 ? "" : ", ") + artists.getJSONObject(j).getString("name");
                        ns.title = song.getString("name");
                        ns.duration = song.getInt("duration_ms") / 1000;
                        ns.url = song.getString("uri");
                        result.add(ns);
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

    // Retrieves user's Spotify username
    public static void GetUserName(final Function<String[], Object> callback)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    //String data = "type=track&q=" + URLEncoder.encode(filter, "UTF-8");
                    URLConnection connection = connectionOpen("https://api.spotify.com/v1/me", "");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String s = "", t;
                    while((t = reader.readLine()) != null)
                        s += t;

                    JSONObject o = new JSONObject(s);
                    String name = o.getString("display_name");
                    String id = o.getString("id");
                    if (name == "null")
                        name = id;

                    callback.apply(new String[] {id, name });
                }
                catch (Exception e)
                {
                    callback.apply(null);
                }
            }
        }).start();
    }
}
