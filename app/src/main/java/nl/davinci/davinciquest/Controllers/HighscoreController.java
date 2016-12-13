package nl.davinci.davinciquest.Controllers;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import nl.davinci.davinciquest.Entity.Highscore;
import nl.davinci.davinciquest.Entity.LocationUser;
import nl.davinci.davinciquest.Entity.User;
import nl.davinci.davinciquest.Helper.StreamReader;

/**
 * Created by Vincent on 8-12-2016.
 */

public class HighscoreController {

    //Posts the highscore to the database
    //returns true when the post has been succesfull
    public Boolean PostHighscore(Highscore highscore)
    {
        PostHighScoreBackGround postHighScoreBackGround = new PostHighScoreBackGround();
        try
        {
            return postHighScoreBackGround.execute(highscore).get();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    //this class sends the user data to the API in a JSON object as a POST request
    public class PostHighScoreBackGround extends AsyncTask<Highscore, Void, Boolean>
    {
        String server_response;
        StreamReader streamReader = new StreamReader();

        @Override
        protected Boolean doInBackground(Highscore... highscore) {
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/highscores/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());

                try {
                    JSONObject obj = new JSONObject();
                    obj.put("score", highscore[0].getScore());
                    obj.put("user_id", highscore[0].getUserId());
                    obj.put("quest_id", highscore[0].getQuestId());
                    obj.put("markers_completed", highscore[0].getMarkersCompleted());

                    // locationUser[0].getAnswered_correct()
                    wr.writeBytes(obj.toString());
                    Log.e("JSON Input", obj.toString());
                    wr.flush();
                    wr.close();
                }
                catch (JSONException ex)
                {
                    ex.printStackTrace();
                }
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK)
                {
                    server_response = streamReader.readStream(urlConnection.getInputStream());
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
            return true;
        }

        @Override
        protected void onPostExecute(Boolean s)
        {
        }
    }

    public ArrayList<Highscore> GetHighscoresByQuestId(Integer questId)
    {
        GetHighScoreBackGround getHighScoreBackGround = new GetHighScoreBackGround();
        try
        {
            return getHighScoreBackGround.execute(questId).get();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public class GetHighScoreBackGround extends AsyncTask<Integer , Void ,ArrayList<Highscore>>
    {
        String server_response;
        StreamReader streamReader = new StreamReader();

        @Override
        protected ArrayList<Highscore> doInBackground(Integer... questId)
        {
            URL url;
            ArrayList<Highscore> highscores = new ArrayList();

            try
            {
                url = new URL("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/highscores/" + questId[0]);
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
                        Highscore highscore = new Highscore();
                        User user = new User();
                        highscore.setId(Integer.parseInt(jo.getString("id")));
                        highscore.setUserId(Integer.parseInt(jo.getString("user_id")));
                        highscore.setQuestId(Integer.parseInt(jo.getString("quest_id")));
                        highscore.setScore(Integer.parseInt(jo.getString("score")));
                        highscore.setMarkersCompleted(Integer.parseInt(jo.getString("markers_completed")));
                        JSONArray userArray = jo.getJSONArray("user");
                        JSONObject userObject = userArray.getJSONObject(0);
                        user.setId(Integer.parseInt(userObject.getString("id")));
                        user.setName(userObject.getString("name"));
                        user.setPin(Integer.parseInt(userObject.getString("pin")));
                        highscore.setUser(user);

                        highscores.add(highscore);
                    }
                }
            }
            catch(MalformedURLException e)
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

            return highscores;
        }

        @Override
        protected void onPostExecute(ArrayList<Highscore> s)
        {
        }
    }

    public Highscore GetHighscoresByQuestIdAndUserId(Integer questId, Integer userId)
    {
        GetHighScoreByQuestIdAndUserIdBackGround getHighScoreByQuestIdAndUserIdBackGround = new GetHighScoreByQuestIdAndUserIdBackGround();
        try
        {
            return getHighScoreByQuestIdAndUserIdBackGround.execute(questId, userId).get();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public class GetHighScoreByQuestIdAndUserIdBackGround extends AsyncTask<Integer, Void ,Highscore>
    {
        String server_response;
        StreamReader streamReader = new StreamReader();

        @Override
        protected Highscore doInBackground(Integer... ids)
        {
            URL url;
            Highscore highscore = new Highscore();
            try
            {
                url = new URL("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/highscores/" + ids[0] + "/" + ids[1]);
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
                        JSONObject jo = (JSONObject) ja.get(i);

                        User user = new User();

                        highscore.setId(Integer.parseInt(jo.getString("id")));
                        highscore.setUserId(Integer.parseInt(jo.getString("user_id")));
                        highscore.setQuestId(Integer.parseInt(jo.getString("quest_id")));
                        highscore.setScore(Integer.parseInt(jo.getString("score")));
                        highscore.setMarkersCompleted(Integer.parseInt(jo.getString("markers_completed")));

                        JSONArray userArray = jo.getJSONArray("user");
                        JSONObject userObject = userArray.getJSONObject(0);
                        user.setId(Integer.parseInt(userObject.getString("id")));
                        user.setName(userObject.getString("name"));
                        user.setPin(Integer.parseInt(userObject.getString("pin")));
                        highscore.setUser(user);
                    }
                }
            }
            catch(MalformedURLException e)
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

            return highscore;
        }

        @Override
        protected void onPostExecute(Highscore s)
        {
        }
    }
}