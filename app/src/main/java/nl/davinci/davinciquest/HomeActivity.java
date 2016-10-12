package nl.davinci.davinciquest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class HomeActivity extends AppCompatActivity {

    String nickname;
    String m_Text;
    int pin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        //get the nickname and pin from memory if they are not there, return the defauultones
        nickname = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("NickName", "Anonymous");
        pin = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("PIN", Integer.toString(0000)));

        Button mapBut = (Button) findViewById(R.id.mapButton);
        mapBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getApplicationContext(),MapsActivity.class);
                startActivity(i);
            }
        });

        Button databaseButton = (Button) findViewById(R.id.databaseButton);
        databaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(getApplicationContext(),DatabaseListActivity.class);
                startActivity(i);
            }
        });

        Button pinButton = (Button) findViewById(R.id.PinButton);
        pinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle(nickname);
                builder.setMessage(Integer.toString(pin));
                builder.setNegativeButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

//        if (nickname.equals("Anonymous"))
//        {
            ShowNickNameDialog();
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case 1: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                }
                else
                {
                    Toast.makeText(HomeActivity.this, R.string.error_location_refused, Toast.LENGTH_LONG).show();
                    Intent i = new Intent(getApplicationContext(),HomeActivity.class);
                    startActivity(i);
                }
            }
            // If we need to check for other permissions, then this is the place to be
        }
    }

    void ShowNickNameDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kies Nickname");

// Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                m_Text = input.getText().toString();

                if(!m_Text.isEmpty())
                {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("NickName", m_Text).commit();
                    nickname = m_Text;
                    GeneratePIN();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                    builder.setTitle("Nickname not valid");
                    builder.show();
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    void GeneratePIN()
    {
        int min = 1000;
        int max = 9999;

        Random r = new Random();
        pin = r.nextInt(max - min + 1) + min;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(nickname);
        builder.setMessage(Integer.toString(pin));

        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("PIN", Integer.toString(pin)).commit();

        builder.setNegativeButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
                PostUserData pud = new PostUserData();
                pud.execute("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/user/");

            }
        });

        builder.show();
    }

    public class PostUserData extends AsyncTask<String , Void ,String> {
        String server_response;

        @Override
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream ());

                try {
                    JSONObject obj = new JSONObject();
                    obj.put("name" , nickname);
                    obj.put("pin" , Integer.toString(pin));

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
                    server_response = readStream(urlConnection.getInputStream());
                    Log.e("Response", server_response.toString());
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

    public static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }

//    public class BackgroundTask extends AsyncTask<String, String, ArrayList>
//    {
//        @Override
//        protected ArrayList doInBackground(String... urlString) {
//
//            try
//            {
//                // set up URL connection
//                URL urlToRequest = new URL(urlString[0]);
//                HttpURLConnection urlConnection = (HttpURLConnection) urlToRequest.openConnection();
//                urlConnection.setDoOutput(true);
//                urlConnection.setRequestMethod("POST");
//                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//
//                // write out form parameters
//                String postParameters = "name="+nickname+"&pin="+pin;
//                urlConnection.setFixedLengthStreamingMode(postParameters.getBytes().length);
//                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
//                out.print(postParameters);
//                out.close();
//
//                // connect
//                urlConnection.connect();
//                urlConnection.getResponseCode();
//            }catch(MalformedURLException e)
//            {
//                e.printStackTrace();
//            }
//            catch(IOException e)
//            {
//                e.printStackTrace();
//            }
//
//        }
//
//        @Override
//        protected void onPreExecute()
//        {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected void onPostExecute(ArrayList result)
//        {
//
//
//        }
//
//        @Override
//        protected void onProgressUpdate(String... values) {
//            super.onProgressUpdate(values);
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

