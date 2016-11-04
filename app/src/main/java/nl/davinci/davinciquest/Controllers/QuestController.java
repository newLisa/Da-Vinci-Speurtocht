package nl.davinci.davinciquest.Controllers;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import nl.davinci.davinciquest.Entity.Quest;

/**
 * Created by nicog on 4-11-2016.
 */

public class QuestController {
    Quest quest = new Quest();
    Boolean done;
    public Quest getQuest(Integer questId)
    {
        done = false;
        GetQuestBackground getQuestBackground = new GetQuestBackground();
        getQuestBackground.execute(questId);
        while (done == false)   {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        return quest;
    }

    public class GetQuestBackground extends AsyncTask<Integer, String, Quest>
    {

        @Override
        protected Quest doInBackground(Integer... questId) {

            Quest bgQuest = new Quest();

            try
            {
                URL url = new URL("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/speurtocht/" + questId[0].toString());

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
                        bgQuest.setId(Integer.parseInt(jo.getString("id")));
                        bgQuest.setName(jo.getString("naam"));
                        bgQuest.setCourse(jo.getString("opleiding"));
                        bgQuest.setInformation(jo.getString("informatie"));

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

            return bgQuest;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Quest bgQuest)
        {
            done = true;
            quest = bgQuest;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

}
