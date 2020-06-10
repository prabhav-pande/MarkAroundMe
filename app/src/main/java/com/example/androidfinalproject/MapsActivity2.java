package com.example.androidfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.graphics.Color;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private GoogleMap mMap;
    Button homePage;
    String latitude;
    String longitude;
    double myLat;
    double myLong;
    double mapsLat;
    double mapsLong;
    double LatLM;
    double LongLM;
    String title;
    Button getDirect;
    MarkerOptions placeLM, placeGoTo;

    LatLng POI;
    LatLng user;
    private Polyline currentPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        //Intent intent = getIntent();
            latitude = MainActivity.getActivityInstance().getLat();
            longitude = MainActivity.getActivityInstance().getLong();
            title = MainActivity.getActivityInstance().getNameMA();
            mapsLat = Double.parseDouble(latitude);
            mapsLong = Double.parseDouble(longitude);
            LatLM = MainActivity.getActivityInstance().getLatLM();
            LongLM = MainActivity.getActivityInstance().getLongLM();
            getDirect = findViewById(R.id.id_directions);


            // Obtain the SupportMapFragment and get notified when the map is ready to be used.


            getDirect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new FetchURL(MapsActivity2.this).execute(getUrl(placeLM.getPosition(), placeGoTo.getPosition(), "walking"), "walking");
                }
            });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        // Add a marker in Sydney and move the camera
            POI = new LatLng(mapsLat, mapsLong);
            user = new LatLng(LatLM, LongLM);
            placeLM = new MarkerOptions().position(new LatLng(mapsLat, mapsLong)).title("YOU");
            placeGoTo = new MarkerOptions().position(new LatLng(LatLM, LongLM)).title(title);
            mMap.addMarker(new MarkerOptions().position(POI).title(title));
            mMap.addMarker(new MarkerOptions().position(user).title("YOU"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(POI, 10F));
    }
    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
