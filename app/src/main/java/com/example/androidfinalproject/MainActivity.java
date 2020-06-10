package com.example.androidfinalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity{
    //AIzaSyDYnwAEn8T1dzJBKhw-vAwUMSX-gnYLrdM GOOGLE PLACES API KEY
    static MainActivity INSTANCE;
    Button imageButton;
    Bitmap imageBitmap;
    String landmarkName;
    double latitude;
    double longitude;
    String lat;
    String longi;
    String infoDoInBack;
    ArrayList<LandMarkObject> list;
    ListView listView;
    ArrayList<POI> poiArrayList;
    ArrayList<String> visitedLocations;
    Button mapLaunch;
    Boolean failed = false;
    Boolean listViewButtonCheck = false;
    JSONObject POIJSON;
    Button wikiButton;
    int LIST_POSITION;
    ArrayList<POI> savedInstanceState;
    String GOOGLEPOIREQ = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=1500&key=AIzaSyDYnwAEn8T1dzJBKhw-vAwUMSX-gnYLrdM";
    public static final String EXTRA_MAPSLAT = "com.example.application.example.MAPSLAT";
    public static final String EXTRA_MAPSLONG = "com.example.application.example.MAPSLONG";
    public static final String EXTRA_LMNAME = "com.example.application.example.LMNAME";
    public static final String EXTRA_LAT = "com.example.application.example.LAT";
    public static final String EXTRA_LONG = "com.example.application.example.LONG";
    static final int REQUEST_IMAGE_CAPTURE = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        INSTANCE=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageButton = findViewById(R.id.id_imageC);
        list = new ArrayList<>();
        listView = findViewById(R.id.id_poilistview);
        visitedLocations = new ArrayList<>();
        wikiButton = findViewById(R.id.id_WikiButton);
        mapLaunch = findViewById(R.id.id_mAMapbutton);
        loaddata();
        mapLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VisitedLocations.class);
                intent.putExtra("arrayList", visitedLocations);
                startActivity(intent);
            }
        });

        buildCloudVisionOptions();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        wikiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity2();
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        if(failed){
            Toast.makeText(this, "LOCATION NOT FOUND.", Toast.LENGTH_SHORT).show();
        }
    }
    public static MainActivity getActivityInstance(){
        return INSTANCE;
    }
    public String getLat(){
        return poiArrayList.get(LIST_POSITION).getLatitude();
    }
    public String getLong(){
        return poiArrayList.get(LIST_POSITION).getLongitude();
    }
    public String getNameMA(){
        return poiArrayList.get(LIST_POSITION).getName();
    }
    public double getLatLM(){
        return latitude;
    }
    public double getLongLM(){
        return longitude;
    }
    public boolean getLVACTCHECK(){
        return listViewButtonCheck;
    }
    public void buildCloudVisionOptions() {
        FirebaseVisionCloudDetectorOptions options =
                new FirebaseVisionCloudDetectorOptions.Builder()
                        .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                        .setMaxResults(15)
                        .build();
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
        poiArrayList = new ArrayList<>();
        list = new ArrayList<>();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            LandmarkRecog(imageBitmap);
        }
    }
    public class AsyncThread extends AsyncTask<String/*this one*/, Void, Void> {
        String lati;
        String longit;
        @Override
        protected Void doInBackground(String... params) {
            try{
                lati = params[0];
                longit = params[1];
                GOOGLEPOIREQ = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+lati+","+longit+"&radius=1500&key=AIzaSyDYnwAEn8T1dzJBKhw-vAwUMSX-gnYLrdM";
                Log.d("TAG", lati);
                Log.d("TAG", longit);
                Log.d("TAG", GOOGLEPOIREQ);
            }catch(Exception e){
                Log.d("TAG", "NAH");
            }
            try {
                URL POIsearch = new URL(GOOGLEPOIREQ);
                URLConnection con = POIsearch.openConnection();
                InputStream stream = con.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer sb = new StringBuffer();


                while((infoDoInBack = reader.readLine()) != null){
                    sb.append(infoDoInBack);
                }
                infoDoInBack = sb.toString();
                Log.d("TAGinfo", infoDoInBack);
            } catch (Exception e) {
                failed = true;
                Log.d("TAG","exception");
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            POIJSON = null;
            try {
                POIJSON = new JSONObject(infoDoInBack);
                sortJsonPOI(POIJSON);
                landMarkObj(list);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("TAG", e.toString());
            }
            Log.d("TAGPOI", POIJSON.toString());
        }
    }
    public void sortJsonPOI(JSONObject obj) throws JSONException {
        for(int x = 0 ; x <= obj.getJSONArray("results").length()-1 ; x++){
            poiArrayList.add(new POI(obj.getJSONArray("results").getJSONObject(x).getString("name"), obj.getJSONArray("results").getJSONObject(x).getJSONObject("geometry").getJSONObject("location").getString("lat"), obj.getJSONArray("results").getJSONObject(x).getJSONObject("geometry").getJSONObject("location").getString("lng"), obj.getJSONArray("results").getJSONObject(x).getString("vicinity"), (String)obj.getJSONArray("results").getJSONObject(x).getJSONArray("types").get(0)));
        }
        CustomAdaptor customAdaptor = new CustomAdaptor(this, R.layout.custom_adaptor, poiArrayList);
        listView.setAdapter(customAdaptor);
        Log.d("TAGJSON", obj.getJSONArray("results").getJSONObject(0).getString("name"));
        Log.d("TAGJSON", String.valueOf(obj.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lat")));
        //Log.d("TAGJSON", POI.getJSONArray("results").getJSONObject(0).getJSONObject("plus_code").getString("rating"));
    }
    private void LandmarkRecog(Bitmap imageBitmap){
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector();
        Task<List<FirebaseVisionCloudLandmark>> result = detector.detectInImage(firebaseVisionImage)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
                       Log.d("TAG", "SUCCESSFUL");
                       for (FirebaseVisionCloudLandmark landmark: firebaseVisionCloudLandmarks) {

                            landmarkName = landmark.getLandmark();
                            String entityId = landmark.getEntityId();
                            float confidence = landmark.getConfidence();
                            visitedLocations.add(landmarkName);

                            LandMarkObject object = new LandMarkObject(landmarkName, entityId, confidence);
                            list.add(object);
                            Log.d("TAGobjects", landmarkName);
                            // Multiple locations are possible, e.g., the location of the depicted
                            // landmark and the location the picture was taken.
                            for (FirebaseVisionLatLng loc: landmark.getLocations()) {
                                latitude = loc.getLatitude();
                                lat = Double.toString(latitude);
                                longitude = loc.getLongitude();
                                longi = Double.toString(longitude);
                            }
                        }
                        new AsyncThread().execute(Double.toString(latitude), Double.toString(longitude));

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "UNSUCCESSFUL");
                    }
                });

    }
    private void landMarkObj(ArrayList<LandMarkObject> objects){
        if(objects.size() != 0) {
            landmarkName = objects.get(0).landmarkName();
        }
        else{
            Toast.makeText(this, "PLEASE TAKE PICTURE AGAIN", Toast.LENGTH_LONG);
        }
    }
    public void openActivity2() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(poiArrayList);
        editor.putString("poiArraylist", json);
        editor.apply();


        Intent intent = new Intent(this, Activity2.class);
        intent.putExtra(EXTRA_LMNAME, landmarkName);
        startActivity(intent);
    }
    private void loaddata(){
        if(poiArrayList != null) {
            SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
            Gson gson = new Gson();
            String json = sharedPreferences.getString("poiArraylist", null);
            Type type = new TypeToken<ArrayList<POI>>() {
            }.getType();
            poiArrayList = gson.fromJson(json, type);
            CustomAdaptor customAdaptor = new CustomAdaptor(this, R.layout.custom_adaptor, poiArrayList);
            listView.setAdapter(customAdaptor);
        }
        if(poiArrayList == null){
            poiArrayList = new ArrayList<>();
        }
    }

    public class CustomAdaptor extends ArrayAdapter<POI>{
        Context parentContext;
        List<POI> list;
        int xmlResource;

        public CustomAdaptor(@NonNull Context context, int resource, @NonNull List<POI> objects) {
            super(context, resource, objects);
            parentContext = context;
            xmlResource = resource;
            list = objects;
        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)parentContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View adaptorView = layoutInflater.inflate(xmlResource, null);

            Button mapsB = adaptorView.findViewById(R.id.id_mapsbutton);
            TextView name = adaptorView.findViewById(R.id.id_lvName);
            TextView Rating = adaptorView.findViewById(R.id.id_ratings);
            TextView Vicinity = adaptorView.findViewById(R.id.id_vicinity);
            ImageView imageView = adaptorView.findViewById(R.id.id_imageView);
            mapsB.setFocusable(false);
            mapsB.setTag(position);

            mapsB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LIST_POSITION = (int) v.getTag();
                    SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(poiArrayList);
                    editor.putString("poiArraylist", json);
                    editor.apply();

                    listViewButtonCheck = true;
                    Intent intent = new Intent(MainActivity.this, MapsActivity2.class);
                    startActivity(intent);
                }
            });

            name.setText(list.get(position).getName());
            Vicinity.setText(list.get(position).getVicinity());
                if(list.get(position).getType().equals("lodging")){
                    imageView.setImageResource(R.drawable.ic_hotel_black_24dp);
                    Rating.setText("Hotel");
                }
                if(list.get(position).getType().equals("restaurant")){
                    imageView.setImageResource(R.drawable.ic_local_dining_black_24dp);
                    Rating.setText("Restaurant");
                }
                if(list.get(position).getType().equals("casino")){
                    imageView.setImageResource(R.drawable.casino);
                    Rating.setText("Casino");
                }
                if(list.get(position).getType().equals("night_club")){
                    imageView.setImageResource(R.drawable.nightclubplswork);
                    Rating.setText("Night Club");
                }
                if(list.get(position).getType().equals("travel_agency")){
                    imageView.setImageResource(R.drawable.ic_flight_black_24dp);
                    Rating.setText("Travel Agency");
                }
                if(list.get(position).getType().equals("airport")){
                    imageView.setImageResource(R.drawable.ic_flight_black_24dp);
                    Rating.setText("Airport");
                }
                if(list.get(position).getType().equals("tourist_attraction")){
                    imageView.setImageResource(R.drawable.touristattractionwork);
                    Rating.setText("Tourist Attraction");
                }
                if(list.get(position).getType().equals("park")){
                    imageView.setImageResource(R.drawable.park);
                    Rating.setText("Park");
                }
                if(Rating.getText().equals("")){
                    Rating.setText(list.get(position).getType());
                }
                Rating.setText(Rating.getText().toString().toUpperCase());
            return adaptorView;
        }
    }
    /*
    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putSerializable("Array", poiArrayList);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        poiArrayList = (ArrayList)savedInstanceState.getSerializable("Array");

        CustomAdaptor customAdaptor = new CustomAdaptor(this, R.layout.custom_adaptor, poiArrayList);
        listView.setAdapter(customAdaptor);
    }
    */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "DESTROYED");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("TAG", "PAUSED");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d("TAG", "RESUMED");
        CustomAdaptor customAdaptor = new CustomAdaptor(this, R.layout.custom_adaptor, poiArrayList);
        listView.setAdapter(customAdaptor);
    }
}

