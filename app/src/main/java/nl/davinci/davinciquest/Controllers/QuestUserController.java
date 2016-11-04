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
import nl.davinci.davinciquest.Entity.QuestUser;

/**
 * Created by nicog on 4-11-2016.
 */

public class QuestUserController {

    QuestUser questUser = new QuestUser();
    Boolean done;

    public QuestUser getQuestUser(Integer userId)
    {
        done = false;
        QuestUserController.GetQuestUserBackground getQuestUserBackground = new QuestUserController.GetQuestBackground();
        getQuestUserBackground.execute(userId);
        while (done == false)   {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        return questUser;
    }

    public class GetQuestsByUserBackground extends AsyncTask<Integer, String, QuestUser>
    {

        @Override
        protected QuestUser doInBackground(Integer... userId) {

            QuestUser bgQuestUser = new QuestUser();

            try
            {
                URL url = new URL("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/koppeltochtuser/activetochten/" + userId[0].toString());

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
