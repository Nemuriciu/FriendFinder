package com.example.vlad.friendfinder;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static android.location.LocationManager.GPS_PROVIDER;

public class LocationActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final String S1 = "se afla la ";
    public static final String S2 = "m de tine";

    private String username;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location myLocation;
    private Location friendLocation = new Location(GPS_PROVIDER);
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        username = getIntent().getStringExtra("User");
        friendLocation.setLatitude(Double.parseDouble(getIntent().getStringExtra("Latitude")));
        friendLocation.setLongitude(Double.parseDouble(getIntent().getStringExtra("Longitude")));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng location = new LatLng(friendLocation.getLatitude(), friendLocation.getLongitude());
        map = googleMap;

        /* Get my current location */
        buildGoogleApiClient();

        TextView tv = findViewById(R.id.user);
        tv.setText(username);
        tv = findViewById(R.id.address);

        /* Create Map with Marker */
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(location.latitude, location.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses != null) {
            if (addresses.size() > 0) {
                String street = addresses.get(0).getThoroughfare();
                String city = addresses.get(0).getLocality();
                String country = addresses.get(0).getCountryName();

                if(street != null && city != null && country != null) {
                    String address = street + "\n" + city + ", " + country;
                    tv.setText(address);
                }
            }
        }

        googleMap.addMarker(new MarkerOptions().position(location).title(username));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(12));
    }

    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getBaseContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection Suspended", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;

        map.addMarker(new MarkerOptions().position(
                new LatLng(myLocation.getLatitude(), myLocation.getLongitude())).title("Me"));

        long distance = (long)myLocation.distanceTo(friendLocation);
        String dist = S1 + distance + S2;
        ((TextView) findViewById(R.id.distance)).setText(dist);
    }
}
