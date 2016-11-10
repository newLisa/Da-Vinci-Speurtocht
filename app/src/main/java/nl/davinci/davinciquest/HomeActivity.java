package nl.davinci.davinciquest;

import android.app.AlertDialog;
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
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class HomeActivity extends AppCompatActivity {

    String nickname;
    String m_Text;
    int pin, user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        SetButtonOnClickListeners();

        //get the nickname and pin from memory if they are not there, return the default ones
        nickname = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("NickName", "Anonymous");
        pin = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("PIN", Integer.toString(0000)));
        user_id = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("user_id", 0);
        //show the nickname dialog when the user has not yet set a nickname
        if (nickname.equals("Anonymous"))
        {
            ShowNickNameDialog();
        }

        GetSpeurTochtList gsl = new GetSpeurTochtList();
        gsl.execute("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/speurtocht");

        GetActiveSpeurTochtList agsl = new GetActiveSpeurTochtList();
        agsl.execute("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/koppeltochtuser/activetochten/" + Integer.toString(user_id));
    }

    @Override
    public void onRestart() {
        super.onRestart();
        GetActiveSpeurTochtList agsl = new GetActiveSpeurTochtList();
        agsl.execute("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/koppeltochtuser/activetochten/" + Integer.toString(user_id));
    }

    public void SetButtonOnClickListeners()
    {
        //setup all the main menu buttons
        Button clearBut = (Button) findViewById(R.id.ClearButton);
        clearBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.clear();
                editor.commit();
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

        EditText et = (EditText) findViewById(R.id.editText);
        et.clearFocus();
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

    //shows the dialog where the user can enter his nickname
    void ShowNickNameDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.prompt_nickname);

// Set up the input edittext
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
//set filter for input length, max = 25
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(25);
        input.setFilters(filterArray);

        builder.setView(input);
        builder.setCancelable(false);

// Set up the OK and Cancel buttons
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
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(HomeActivity.this);
                    builder2.setTitle(R.string.error_nickname_invalid);
                    builder2.setNegativeButton(R.string.error_retry, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ShowNickNameDialog();
                        }
                    });

                    builder2.show();
                }
            }
        });

        builder.show();
    }

    //generates a 4 number pin for the user
    void GeneratePIN()
    {
        //TODO move generate pin to API
        int min = 1000;
        int max = 9999;

        Random r = new Random();
        pin = r.nextInt(max - min + 1) + min;

        PostUserData pud = new PostUserData();
        pud.execute("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/user/");
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("PIN", Integer.toString(pin)).commit();
    }

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
        if (id == R.id.action_showNamePin) {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setTitle(nickname);
            builder.setMessage("pin:" + Integer.toString(pin));
            builder.setNegativeButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    //turn the response from the server into a readable string
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

    //this class sends the user data to the API in a JSON object as a POST request
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
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

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
                    user_id = Integer.parseInt(server_response);
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("user_id", Integer.parseInt(server_response)).commit();
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

    //this class get the speurtochten to diplay in the home menu
    public class GetSpeurTochtList extends AsyncTask<String, String, ArrayList>
    {
        @Override
        protected ArrayList doInBackground(String... urlString) {
            ArrayList items = new ArrayList();

            try
            {
                URL url = new URL(urlString[0]);

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
                        items.add(jo.getString("naam"));
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
            return items;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final ArrayList result)
        {
            final ListView speurtochtListView = (ListView) findViewById(R.id.home_speurtocht_list);
            speurtochtListView.setAdapter(new ArrayAdapter(HomeActivity.this,android.R.layout.simple_list_item_1,result));
            speurtochtListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id)
                {
                    //crete newkoppel user-tocht entry

                    String main = speurtochtListView.getItemAtPosition(position).toString();

                    //open kaart met int position in de api
                    Intent i = new Intent(getApplicationContext(),MapsActivity.class);
                    i.putExtra("id", position + 1);
                    i.putExtra("user_id", user_id);
                    startActivity(i);
                }
            });
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    //this class get the speurtochten to diplay in the home menu
    public class GetActiveSpeurTochtList extends AsyncTask<String, String, ArrayList>
    {
        @Override
        protected ArrayList doInBackground(String... urlString) {
            ArrayList items = new ArrayList();

            try
            {
                URL url = new URL(urlString[0]);

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
                        items.add(jo.getString("naam"));
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
            return items;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final ArrayList result)
        {
            final ListView speurtochtListView = (ListView) findViewById(R.id.activeTochtenList);
            speurtochtListView.setAdapter(new ArrayAdapter(HomeActivity.this,android.R.layout.simple_list_item_1,result));
            speurtochtListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id)
                {

                }
            });
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }


}