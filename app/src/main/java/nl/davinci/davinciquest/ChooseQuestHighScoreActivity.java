package nl.davinci.davinciquest;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

import nl.davinci.davinciquest.Entity.Quest;


public class ChooseQuestHighScoreActivity extends Activity
{
    ArrayList<Quest> questList;
    ArrayList<String> questNames = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_choose_quest_high_score);

        GetQuestsBackground bk = new GetQuestsBackground();
        bk.execute("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/speurtocht");
    }

    public class GetQuestsBackground extends AsyncTask<String, String, ArrayList>
    {
        @Override
        protected ArrayList doInBackground(String... urlString)
        {
            ArrayList items = new ArrayList();

            try
            {
                URL url = new URL(urlString[0]);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                String next;

                while ((next = bufferedReader.readLine()) != null)
                {
                    JSONArray ja = new JSONArray(next);

                    for (int i = 0; i < ja.length(); i++)
                    {
                        Quest q = new Quest();
                        JSONObject jo = (JSONObject) ja.get(i);
                        q.setName(jo.getString("naam"));
                        questNames.add(jo.getString("naam"));
                        q.setId(Integer.parseInt(jo.getString("id")));
                        q.setCourse(jo.getString("opleiding"));
                        q.setInformation(jo.getString("informatie"));
                        items.add(q);
                    }
                }
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (JSONException e)
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
            questList = result;
            ListView highscoreQuestlist = (ListView) findViewById(R.id.choose_quest_list);
            highscoreQuestlist.setAdapter(new ArrayAdapter(ChooseQuestHighScoreActivity.this,android.R.layout.simple_list_item_1,questNames));

            highscoreQuestlist.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                final int position, long id)
                {
                    Intent i = new Intent(getApplicationContext(),HighScoreActivity.class);
                    i.putExtra("id", questList.get(position).getId());

                    startActivity(i);
                }
            });
        }

        @Override
        protected void onProgressUpdate(String... values)
        {
            super.onProgressUpdate(values);
        }
    }
}
