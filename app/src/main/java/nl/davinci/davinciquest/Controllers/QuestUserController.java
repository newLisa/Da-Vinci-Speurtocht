package nl.davinci.davinciquest.Controllers;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import nl.davinci.davinciquest.Entity.QuestUser;
import nl.davinci.davinciquest.Entity.Quest;

/**
 * Created by nicog on 4-11-2016.
 */

public class QuestUserController {

    QuestUser questUser = new QuestUser();
    Boolean done;
    ArrayList<Quest> quest= new ArrayList();

    public ArrayList<Quest> getQuestByUserId(Integer userId)
    {
        done = false;
        QuestUserController.GetQuestsByUserBackground getQuestsByUserBackground = new QuestUserController.GetQuestsByUserBackground();
        try {
            quest = getQuestsByUserBackground.execute(userId).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        return quest;
    }

    public class GetQuestsByUserBackground extends AsyncTask<Integer, String, ArrayList<Quest>>
    {

        @Override
        protected ArrayList<Quest> doInBackground(Integer... userId) {


            ArrayList<Quest> questList = new ArrayList<>();

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
                        Quest bgQuest = new Quest();
                        bgQuest.setId(Integer.parseInt(jo.getString("id")));
                        bgQuest.setName(jo.getString("naam"));
                        bgQuest.setCourse(jo.getString("opleiding"));
                        bgQuest.setInformation(jo.getString("informatie"));

                        questList.add(bgQuest);

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

            return questList;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<Quest> questList)
        {

        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

}
