package nl.davinci.davinciquest;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;

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


public class DatabaseListActivity extends ListActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_list);

        BackgroundTask bk = new BackgroundTask();
        bk.execute("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/highscores");
    }

    public class BackgroundTask extends AsyncTask<String, String, ArrayList>
    {
        @Override
        protected ArrayList doInBackground(String... urlString) {
            ArrayList items = new ArrayList();

            try
            {
                URL url = new URL(urlString[0]);

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
                        items.add("user:" + jo.getString("user_id") + " heeft gescoord: " + jo.getString("score"));
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
            return items;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList result)
        {
            setListAdapter(new ArrayAdapter(DatabaseListActivity.this,android.R.layout.simple_list_item_1,result));

        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }
}


