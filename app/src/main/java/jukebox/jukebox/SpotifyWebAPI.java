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


public class SpotifyWebAPI
{
    private static URLConnection connectionOpen(String surl, String params)
    {
        try
        {
            URL url = new URL(surl + (params == "" ? "" : ("?" + params)));
            URLConnection connection = url.openConnection();
            //connection.setDoInput(true);
            //connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + Global.playerConfig.oauthToken);
            return connection;
        }
        catch (Exception e)
        {
            return null;
        }
    }

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
}
