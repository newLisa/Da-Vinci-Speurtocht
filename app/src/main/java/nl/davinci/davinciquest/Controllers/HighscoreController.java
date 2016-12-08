package nl.davinci.davinciquest.Controllers;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import nl.davinci.davinciquest.Entity.Highscore;
import nl.davinci.davinciquest.Helper.StreamReader;

/**
 * Created by Vincent on 8-12-2016.
 */

public class HighscoreController
{

    public Boolean PostHighscore(Highscore highscore)
    {
        PostHighScoreBackGround postHighScoreBackGround = new PostHighScoreBackGround();
        try
        {
            return postHighScoreBackGround.execute(highscore).get();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } catch (ExecutionException e)
        {
            e.printStackTrace();
        }

        return false;
    }
    //this class sends the user data to the API in a JSON object as a POST request
    public class PostHighScoreBackGround extends AsyncTask<Highscore , Void ,Boolean>
    {
        String server_response;
        StreamReader streamReader = new StreamReader();

        @Override
        protected Boolean doInBackground(Highscore... highscore)
        {
            URL url;
            HttpURLConnection urlConnection = null;

            try
            {
                url = new URL("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/highscores/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream ());

                try
                {
                    JSONObject obj = new JSONObject();
                    obj.put("score" , highscore[0].getScore());
                    obj.put("user_id" , highscore[0].getUserId());
                    obj.put("quest_id" , highscore[0].getQuestId());

                    // locationUser[0].getAnswered_correct()
                    wr.writeBytes(obj.toString());
                    Log.e("JSON Input", obj.toString());
                    wr.flush();
                    wr.close();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK)
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
}
