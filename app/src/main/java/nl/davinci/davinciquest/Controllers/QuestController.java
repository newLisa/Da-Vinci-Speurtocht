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
import java.util.concurrent.ExecutionException;

import nl.davinci.davinciquest.Entity.Quest;

/**
 * Created by nicog on 4-11-2016.
 */

public class QuestController {
    Quest quest = new Quest();
    Boolean done;
    Boolean found;
    public Quest getQuest(Integer questId)
    {
        done = false;
        GetQuestBackground getQuestBackground = new GetQuestBackground();

        try {
            quest = getQuestBackground.execute(questId).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        return quest;
    }

    public Boolean checkQuest(Integer questId)
    {
        CheckQuestBackground checkQuestBackground = new CheckQuestBackground();

        try {
            found = checkQuestBackground.execute(questId).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        return found;
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

                        JSONObject jo = new JSONObject(next);

                        bgQuest.setId(Integer.parseInt(jo.getString("id")));
                        bgQuest.setName(jo.getString("naam"));
                        bgQuest.setCourse(jo.getString("opleiding"));
                        bgQuest.setInformation(jo.getString("informatie"));

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

    public class CheckQuestBackground extends AsyncTask<Integer, String, Boolean>
    {
        @Override
        protected Boolean doInBackground(Integer... questId) {
            Boolean found = false;
            try
            {
                URL url = new URL("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/speurtocht/" + questId[0].toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                String next;

                if ((next = bufferedReader.readLine()) != null)
                {
                    found = true;
                }
                else
                {
                    found = false;
                }
            }catch(MalformedURLException e)
            {
                found = false;
                e.printStackTrace();
            }
            catch(IOException e)
            {
                found = false;
                e.printStackTrace();
            }


            return found;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean found)
        {
            found = found;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

}
