package com.example.vincent.mapstest;

import android.content.pm.PackageManager;
import android.location.Location;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback ,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Button markerButton;
    int markerCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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

        markerButton = (Button) findViewById(R.id.markerButton);
        markerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SetMarkerAtCurrentLocation();
            }
        });


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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            mMap.setMyLocationEnabled(true);
        } else
        {
            // Show rationale and request permission.
        }
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
    public  void onConnected(Bundle connectionHint)
    {

    }

    @Override
    public void onConnectionSuspended(int cause)
    {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result)
    {

    }
}

