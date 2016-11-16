package nl.davinci.davinciquest.Controllers;

import android.os.AsyncTask;
import android.preference.PreferenceManager;
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
import java.util.concurrent.ExecutionException;

import nl.davinci.davinciquest.Entity.LocationUser;
import nl.davinci.davinciquest.Helper.StreamReader;

/**
 * Created by nicog on 11/15/2016.
 */

public class LocationUserController {

    ArrayList<LocationUser> locationUserList = new ArrayList();
    StreamReader streamReader = new StreamReader();

    public ArrayList<LocationUser> getLocationUserArray()
    {
        LocationUserController.LocationUserArrayBackground locationUserArrayBackground = new LocationUserController.LocationUserArrayBackground();
        try {
            locationUserList = locationUserArrayBackground.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        return locationUserList;
    }

    public class LocationUserArrayBackground extends AsyncTask<String, String, ArrayList<LocationUser>>
    {

        @Override
        protected ArrayList<LocationUser> doInBackground(String... method) {


            ArrayList<LocationUser> locationUserList = new ArrayList<>();

            try
            {
                URL url = new URL("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/locationuser");

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
                        LocationUser bgLocationUser = new LocationUser();
                        bgLocationUser.setId(Integer.parseInt(jo.getString("id")));
                        bgLocationUser.setUser_id(Integer.parseInt(jo.getString("user_id")));
                        bgLocationUser.setLocation_id(Integer.parseInt(jo.getString("user_id")));
                        bgLocationUser.setAnswered_correct(Integer.parseInt(jo.getString("answered_correct")));

                        locationUserList.add(bgLocationUser);

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

            return locationUserList;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<LocationUser> locationUserList)
        {

        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    public Boolean postLocationUser(LocationUser locationUser)
    {
        LocationUserController.PostLocationUserBackground postLocationUserBackground = new LocationUserController.PostLocationUserBackground();
        postLocationUserBackground.execute(locationUser);
        return true;
    }

    //this class sends the user data to the API in a JSON object as a POST request
    public class PostLocationUserBackground extends AsyncTask<LocationUser , Void ,String> {
        String server_response;

        @Override
        protected String doInBackground(LocationUser... locationUser) {
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/locationuser/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream ());

                try {
                    JSONObject obj = new JSONObject();
                    obj.put("locatie_id" , locationUser[0].getLocation_id());
                    obj.put("user_id" , locationUser[0].getUser_id());
                    obj.put("answered_correct" , locationUser[0].getAnswered_correct());

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

                if(responseCode == HttpURLConnection.HTTP_OK){
                    server_response = streamReader.readStream(urlConnection.getInputStream());
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("Response", "" + server_response);

        }
    }
}
