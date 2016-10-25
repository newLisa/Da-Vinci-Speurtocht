package nl.davinci.davinciquest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback ,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Button markerButton, speurtochtButton;
    FloatingActionButton qrButton;
    int markerCount = 1;
    ArrayList markerLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //ask for location permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mapFragment.getMapAsync(this);

        AddButtonOnClickListeners();
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
        markerButton = (Button) findViewById(R.id.currentLocMarkerButton);
        markerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SetMarkerAtCurrentLocation();
            }
        });

        qrButton = (FloatingActionButton) findViewById(R.id.floatingQRbutton);
        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(getApplicationContext(),QRScanActivity.class);
                startActivity(i);
            }
        });

        speurtochtButton = (Button) findViewById(R.id.getSpeurtochButton);
        speurtochtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                GetSpeurtocht();
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

    public void GetSpeurtocht()
    {
        GetSpeurtochtJsonData gs = new GetSpeurtochtJsonData();
        gs.execute();
    }

    public void PlaceMarkers()
    {
        for (int i = 0; i < markerLocations.size(); i++)
        {
            MarkerOptions options = new MarkerOptions();

            options.position((LatLng)markerLocations.get(i));
            options.title("marker #" + (i+1));
            options.snippet("speurtocht 1");
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

            ArrayList locations = new ArrayList<ArrayList>();

            try
            {
                URL url = new URL("http://www.intro.dvc-icta.nl/SpeurtochtApi/web/marker");

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
                        LatLng latlng = new LatLng(Double.parseDouble(jo.getString("latitude").toString()),Double.parseDouble(jo.getString("longitude").toString()));

                        locations.add(latlng);
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
}
