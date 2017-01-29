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
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import nl.davinci.davinciquest.Entity.Polygon;
import nl.davinci.davinciquest.Entity.Quest;

/**
 * Created by nicog on 4-11-2016.
 */

public class PolygonController {
    ArrayList polygons = new ArrayList();
    Boolean done;
    Boolean found;
    public ArrayList getPolygon(Integer questId)
    {
        done = false;
        GetPolyogonBackground getPolygonBackground = new GetPolyogonBackground();

        try {
            polygons = getPolygonBackground.execute(questId).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return polygons;
    }

    public class GetPolyogonBackground extends AsyncTask<Integer, String, ArrayList>
    {

        @Override
        protected ArrayList doInBackground(Integer... questId) {

            ArrayList polygons = new ArrayList();

            try
            {
                URL url = new URL("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/polygon/" + questId[0].toString());

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));

                String next;

/*                while ((next = bufferedReader.readLine()) != null)
                {
                    JSONObject jo = new JSONObject(next);

                    Polygon bgPolygon = new Polygon();
                    bgPolygon.setId(Integer.parseInt(jo.getString("id")));
                    bgPolygon.setLat(Double.parseDouble(jo.getString("lat")));
                    bgPolygon.setLng(Double.parseDouble(jo.getString("lng")));
                    bgPolygon.setOrderNumber(Integer.parseInt(jo.getString("order_number")));

                    polygons.add(bgPolygon);
                }*/

                while ((next = bufferedReader.readLine()) != null)
                {
                    JSONArray ja = new JSONArray(next);

                    for (int i = 0; i < ja.length(); i++)
                    {
                        JSONObject jo = (JSONObject) ja.get(i);
                        Polygon bgPolygon = new Polygon();
                        bgPolygon.setId(Integer.parseInt(jo.getString("id")));
                        bgPolygon.setLat(Double.parseDouble(jo.getString("lat")));
                        bgPolygon.setLng(Double.parseDouble(jo.getString("lng")));
                        bgPolygon.setOrderNumber(Integer.parseInt(jo.getString("order_number")));

                        polygons.add(bgPolygon);
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

            return polygons;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList polygonArray)
        {
            done = true;
            polygons = polygonArray;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }
}
