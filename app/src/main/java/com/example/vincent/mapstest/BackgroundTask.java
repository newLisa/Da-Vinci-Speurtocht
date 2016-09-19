package com.example.vincent.mapstest;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Vincent on 19-9-2016.
 */
public class BackgroundTask extends AsyncTask<String, String, String>
{

    @Override
    protected String doInBackground(String... urls) {
        ArrayList items = new ArrayList();

        try
        {
            URL url = new URL("http://localhost/speurtocht/index.php");

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));

            String next;

            while ((next = bufferedReader.readLine()) != null)
            {
                JSONArray ja = new JSONArray(next);

                for (int i = 0; i < ja.length(); i++)
                {
                    JSONObject jo = (JSONObject) ja.get(i);
                    items.add(jo.getString("id"));
                }
            }
        }catch(MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();



    }

    @Override
    protected void onPostExecute(String result) {



    }

    @Override
    protected void onProgressUpdate(String... values) {

        super.onProgressUpdate(values);
    }
}
