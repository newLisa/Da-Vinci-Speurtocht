package nl.davinci.davinciquest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

import nl.davinci.davinciquest.Controllers.QuestController;
import nl.davinci.davinciquest.Entity.Marker;
import nl.davinci.davinciquest.Entity.Quest;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback ,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Button markerButton, speurtochtButton;
    FloatingActionButton qrButton, startButton;
    int markerCount = 1;
    ArrayList<Marker> markerLocations;
    int speurtochtId, user_id;
    Quest quest = new Quest();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mapFragment.getMapAsync(this);

        AddButtonOnClickListeners();


        if(speurtochtId > 0)
        {
            GetSpeurtochtJsonData gs = new GetSpeurtochtJsonData();
            gs.execute("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/koppeltochtlocatie/" + speurtochtId);
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public  void onConnected(Bundle connectionHint)
    {
        //draw the circle around the current location
        LatLng currentLocation = GetCurrentLocation();
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(currentLocation.latitude, currentLocation.longitude))
                .radius(500)
                .strokeColor(Color.RED)
                .fillColor(Color.TRANSPARENT));

        //use Handler to start zoom function after 3 seconds
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ZoomCameraToCurrentPosition();
            }
        }, 3000);
    }

    public void AddButtonOnClickListeners()
    {
//        markerButton = (Button) findViewById(R.id.currentLocMarkerButton);
//        markerButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view)
//            {
//                SetMarkerAtCurrentLocation();
//            }
//        });
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
            }
        });
//        speurtochtButton = (Button) findViewById(R.id.getSpeurtochButton);
//        speurtochtButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view)
//            {
//                GetSpeurtocht();
//            }
//        });
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
        for (int i = 0; i < markerLocations.size(); i++)
        {
            MarkerOptions options = new MarkerOptions();

            LatLng markerPos = new LatLng(markerLocations.get(i).getLatitude(),markerLocations.get(i).getLongitude());
            options.position(markerPos);
            options.title(markerLocations.get(i).getName());
            options.snippet(markerLocations.get(i).getInfo());
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.da_vinci_logo));
            mMap.addMarker(options);
        }
    }

    //Sets  marker at the users current location
    public void SetMarkerAtCurrentLocation()
    {
        //check permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
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
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.da_vinci_logo));
            mMap.addMarker(options);
            markerCount++;
        }
    }

    public LatLng GetCurrentLocation()
    {
        LatLng pos = null;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            pos = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        }
        return pos;
    }

    protected void onStart()
    {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop()
    {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int cause)
    {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result)
    {

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
        }

        @Override
        protected void onPostExecute(ArrayList locations)
        {
            markerLocations = locations;
            PlaceMarkers();
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
}
