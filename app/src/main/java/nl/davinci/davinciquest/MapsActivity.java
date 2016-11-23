package nl.davinci.davinciquest;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;

import nl.davinci.davinciquest.Controllers.LocationUserController;
import nl.davinci.davinciquest.Controllers.QuestController;
import nl.davinci.davinciquest.Controllers.QuestUserController;
import nl.davinci.davinciquest.Entity.LocationUser;
import nl.davinci.davinciquest.Entity.Marker;
import nl.davinci.davinciquest.Entity.Quest;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback ,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener/*, LocationListener*/
{
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Button answerButton;
    FloatingActionButton qrButton, startButton;
    int markerCount = 1;
    float maxDistanceVisibleMarker = 100;
    ArrayList<Marker> markerLocations;
    int speurtochtId, user_id;
    Quest quest = new Quest();
    ArrayList<Quest> userQuestList = new ArrayList<>();
    QuestUserController questUserController = new QuestUserController();
    TextView questionText;
    RadioGroup answerRadioGroup;
    RadioButton answerRadio1, answerRadio2, answerRadio3, answerRadio4;
    String correctAnswer;
    Boolean started = false;
    ArrayList<LocationUser> locationUserList = new ArrayList();
    ProgressDialog pd;
    protected LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //ask for location permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Bundle extras = getIntent().getExtras();
        speurtochtId = extras.getInt("id");
        GetQuestData(speurtochtId);
        user_id = extras.getInt("user_id");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).addApi(AppIndex.API).build();

        mapFragment.getMapAsync(this);

        AddButtonOnClickListeners();
        userQuestList = questUserController.getQuestByUserId(user_id);
        for (int i = 0; i < userQuestList.size(); i++)   {
            if (userQuestList.get(i).getId() == quest.getId())  {
                started = true;
                startButton.hide();
            }
        }
        if (speurtochtId > 0) {
            GetSpeurtochtJsonData gs = new GetSpeurtochtJsonData();
            gs.execute("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/koppeltochtlocatie/" + speurtochtId);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final com.google.android.gms.maps.model.Marker marker) {
                LocationUserController locationUserController = new LocationUserController();
                locationUserList = locationUserController.getLocationUserArray(user_id, quest.getId());

                Marker markerEntity = new Marker();
                markerEntity = (Marker) marker.getTag();
                int answered = 0;

                for (int i = 0; i < locationUserList.size(); i++)
                {
                    if (locationUserList.get(i).getLocation_id() == markerEntity.getId())
                    {
                        answered = locationUserList.get(i).getAnswered();
                        if (answered == 1)
                        {
                            break;
                        }
                    }
                }

                if (started && answered != 1)
                {
                    final Dialog dialog = new Dialog(MapsActivity.this);
                    dialog.setContentView(R.layout.custom_marker_dialog);
                    dialog.setTitle(marker.getTitle());

                    ImageView img = (ImageView) dialog.findViewById(R.id.custom_dialog_image);
                    img.setImageResource(R.drawable.paardenbloem);

                    TextView infoTextView = (TextView) dialog.findViewById(R.id.custom_dialog_info);
                    infoTextView.setText(marker.getSnippet());

                    questionText = (TextView) dialog.findViewById(R.id.QuestionText);
                    answerRadio1 = (RadioButton) dialog.findViewById(R.id.answerRadio1);
                    answerRadio2 = (RadioButton) dialog.findViewById(R.id.answerRadio2);
                    answerRadio3 = (RadioButton) dialog.findViewById(R.id.answerRadio3);
                    answerRadio4 = (RadioButton) dialog.findViewById(R.id.answerRadio4);

                    answerButton = (Button) dialog.findViewById(R.id.answerButton);
                    answerButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Marker currentLocation = (Marker) marker.getTag();
                            answerRadioGroup = (RadioGroup) dialog.findViewById(R.id.answerRadioGroup);
                            int selectedRadiobuttonId = answerRadioGroup.getCheckedRadioButtonId();
                            RadioButton selectedRadioButton = (RadioButton) answerRadioGroup.findViewById(selectedRadiobuttonId);
                            String answer = selectedRadioButton.getText().toString();
                            LocationUser locationUser = new LocationUser();
                            locationUser.setUser_id(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("user_id", 0));
                            locationUser.setLocation_id(currentLocation.getId());
                            locationUser.setQuest_id(quest.getId());
                            LocationUserController locationUserController = new LocationUserController();

                            if (correctAnswer.equals(answer)) {
                                locationUser.setAnswered_correct("true");
                                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.greenmarkersmall));
                            }
                            else {
                                locationUser.setAnswered_correct("false");
                                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.redmarkersmall));
                            }
                            locationUser.setAnswered("true");
                            locationUserController.postLocationUser(locationUser);
                            dialog.cancel();
                        }
                    });

                    Marker m = (Marker) marker.getTag();
                    if (m != null) {
                        int vraagId = m.getVraag_id();
                        GetQuestion getq = new GetQuestion();
                        getq.execute("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/vraag/" + Integer.toString(vraagId));
                    }

                    dialog.show();
                }
                return true;
            }
        });


    }

    void DrawPolygon()
    {
        // Instantiates a new Polygon object and adds points to define a rectangle
        PolygonOptions rectOptions = new PolygonOptions()
                .add(new LatLng(51.80185467344209, 4.680642485618591),
                        new LatLng(51.799180878825474, 4.678325057029724),
                        new LatLng(51.79726334535511, 4.677445292472839),
                        new LatLng(51.79665953765794, 4.679537415504456),
                        new LatLng(51.797814064006644, 4.685030579566956),
                        new LatLng(51.80013629759001,4.685245156288147));
        rectOptions.strokeColor(Color.RED);

        // Get back the mutable Polygon
        Polygon polygon = mMap.addPolygon(rectOptions);
    }

    @Override
    public  void onConnected(Bundle connectionHint)
    {

    }

    public void AddButtonOnClickListeners()
    {
        qrButton = (FloatingActionButton) findViewById(R.id.floatingQRbutton);
        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(getApplicationContext(),QRScanActivity.class);
                startActivity(i);
            }
        });

        startButton = (FloatingActionButton) findViewById(R.id.floatingStartButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                PostKoppelTochtUser pktu = new PostKoppelTochtUser();
                pktu.execute("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/koppeltochtuser/");
                mMap.clear();
                started = true;
                startButton.hide();
                PlaceMarkers();
            }
        });
    }

    public void ZoomCameraToCurrentPosition()
    {
        LatLng currentPos = GetCurrentLocation();
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentPos)      // Sets the center of the map to location user
                .zoom(16)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to north
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    //Gets all data from the quest trough the quest controller and puts it in a global variable
    public void GetQuestData(int questId)
    {
        QuestController questController = new QuestController();
        quest = questController.getQuest(questId);
    }

    public void PlaceMarkers()
    {
        mMap.clear();
        DrawPolygon();
        LocationUserController locationUserController = new LocationUserController();
        locationUserList = locationUserController.getLocationUserArray(user_id, quest.getId());
        if (markerLocations.size() > 0)
        {
            for (int i = 0; i < markerLocations.size(); i++)
            {
                MarkerOptions options = new MarkerOptions();
                LatLng markerPos = new LatLng(markerLocations.get(i).getLatitude(),markerLocations.get(i).getLongitude());
                options.position(markerPos);
                options.title(markerLocations.get(i).getName());
                options.snippet(markerLocations.get(i).getInfo());
                if (started)
                {
                    boolean found = false;
                    for (int r = 0; r < locationUserList.size(); r++)
                    {
                        if (locationUserList.get(r).getLocation_id() == markerLocations.get(i).getId())
                        {
                            if (locationUserList.get(r).getAnswered_correct() == 1)
                            {
                                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.greenmarkersmall));
                                found = true;
                                break;
                            }
                            else
                            {
                                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.redmarkersmall));
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found)
                    {
                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.orangemarkersmall));
                    }
                }
                else
                {
                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.greymarkersmall));
                }

                LatLng currentPos = GetCurrentLocation();
                float[] result = new float[1];
                Location.distanceBetween(currentPos.latitude,currentPos.longitude,markerPos.latitude,markerPos.longitude,result);
                if (result[0] > maxDistanceVisibleMarker && started)
                {
                    options.visible(false);
                }

                Marker markerEntity = new Marker();
                markerEntity.setVraag_id(markerLocations.get(i).getVraag_id());
                markerEntity.setId(markerLocations.get(i).getId());
                markerEntity.setInfo(markerLocations.get(i).getInfo());
                markerEntity.setLatitude(markerLocations.get(i).getLatitude());
                markerEntity.setLongitude(markerLocations.get(i).getLongitude());
                markerEntity.setName(markerLocations.get(i).getName());

                com.google.android.gms.maps.model.Marker m = mMap.addMarker(options);
                m.setTag(markerEntity);

                markerEntity.setMapMarker(m);
                markerLocations.set(i, markerEntity);


            }
            pd.dismiss();
            ZoomCameraToCurrentPosition();
            ScheduledExecutorService schedule;
            schedule().scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {

                }
            },1 ,3L, 5);

            /*new Timer().sc(new TimerTask() {
                @Override
                public void run() {
                    LatLng currentPos = GetCurrentLocation();
                    for (int i = 0; i < markerLocations.size(); i++)
                    {

                        float[] result = new float[1];
                        Location.distanceBetween(currentPos.latitude,currentPos.longitude,markerLocations.get(i).getLatitude(),markerLocations.get(i).getLongitude(),result);
                        if (result[0] > maxDistanceVisibleMarker && started)
                        {
                            markerLocations.get(i).getMapMarker().setVisible(false);
                        }
                        else
                        {
                            markerLocations.get(i).getMapMarker().setVisible(true);
                        }
                    }
                }
            }, 0, 10000);*/
        }
        else
        {
            pd.dismiss();
        }


    }
    /*@Override
    public void onLocationChanged(Location location)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("IT FUCKING WORKS!!!!!");
        builder.show();

        for (int i = 0; i < markerLocations.size(); i++)
        {
            LatLng currentPos = GetCurrentLocation();
            float[] result = new float[1];
            Location.distanceBetween(currentPos.latitude,currentPos.longitude,markerLocations.get(i).getLatitude(),markerLocations.get(i).getLongitude(),result);
            if (result[0] > maxDistanceVisibleMarker && started)
            {
                markerLocations.get(i).getMapMarker().setVisible(false);
            }
            else
            {
                markerLocations.get(i).getMapMarker().setVisible(true);
            }
        }
    }*/

    //Sets  marker at the users current location
    public void SetMarkerAtCurrentLocation()
    {
        //check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            //store last known location
            Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            //create a new marker
            MarkerOptions options = new MarkerOptions();
            LatLng pos = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
            options.position(pos);
            options.title("marker #" + markerCount);
            options.draggable(true);
            options.snippet("dit is een snippet");
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.redmarkersmall));
            mMap.addMarker(options);
            markerCount++;
        }
    }

    public LatLng GetCurrentLocation()
    {
        LatLng pos = null;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            pos = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        }
        return pos;
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(mGoogleApiClient, getIndexApiAction());
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mGoogleApiClient, getIndexApiAction());
    }

    @Override
    public void onConnectionSuspended(int cause)
    {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result)
    {
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    public class GetSpeurtochtJsonData extends AsyncTask<String, String, ArrayList>
    {
        @Override
        protected ArrayList doInBackground(String... urlString) {

            ArrayList locations = new ArrayList<Marker>();

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
                        Marker marker = new Marker();
                        marker.setId(Integer.parseInt(jo.getString("id")));
                        marker.setLatitude(Double.parseDouble(jo.getString("latitude")));
                        marker.setLongitude(Double.parseDouble(jo.getString("longitude")));
                        marker.setName(jo.getString("name"));
                        marker.setInfo(jo.getString("info"));
                        marker.setVraag_id(Integer.parseInt(jo.getString("question_id")));

                        locations.add(marker);
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

            return locations;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pd = ProgressDialog.show(MapsActivity.this, "Loading", "Please wait...");

        }

        @Override
        protected void onPostExecute(ArrayList locations)
        {
            markerLocations = locations;

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PlaceMarkers();
                }
            }, 500);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    public class PostKoppelTochtUser extends AsyncTask<String , Void ,String> {
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
                    obj.put("tocht_id" , Integer.toString(speurtochtId));
                    obj.put("user_id" , Integer.toString(user_id));
                    //obj.put("started_bool", "1");
                    //obj.put("finished_bool", "0");

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

    public class GetQuestion extends AsyncTask<String, String, ArrayList<String>>
    {
        @Override
        protected  ArrayList<String> doInBackground(String... urlString) {

            ArrayList<String> questionData = new ArrayList<>();

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
                    JSONObject jo = new JSONObject(next);

                    questionData.add(jo.getString("vraag"));
                    questionData.add(jo.getString("answer_1"));
                    questionData.add(jo.getString("answer_2"));
                    questionData.add(jo.getString("answer_3"));
                    questionData.add(jo.getString("answer_4"));
                    questionData.add(jo.getString("correct_answer"));
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

            return questionData;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute( ArrayList<String> questionData)
        {
            questionText.setText((String)questionData.get(0));
            answerRadio1.setText((String) questionData.get(1));
            answerRadio2.setText((String) questionData.get(2));
            answerRadio3.setText((String) questionData.get(3));
            answerRadio4.setText((String) questionData.get(4));
            correctAnswer = questionData.get(5);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
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

    public class checkMarkerDistanceAndHideIfYouAreTooFarAway extends AsyncTask<ArrayList<Marker>, String, Void>
    {
        @Override
        protected Void doInBackground(ArrayList<Marker>... markers) {
            LocationManager locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);

            return null;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pd = ProgressDialog.show(MapsActivity.this, "Loading", "Please wait...");

        }


        protected void onPostExecute()
        {

        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

}


