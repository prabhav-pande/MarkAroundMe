package com.example.androidfinalproject;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Activity2 extends AppCompatActivity {
     String lmName;
     TextView lm;
     TextView WikiT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);
        lm = findViewById(R.id.id_twoLM);
        WikiT = findViewById(R.id.txtWikiData);

        Intent intent = getIntent();
        lmName = intent.getStringExtra(MainActivity.EXTRA_LMNAME);
        lm.setText(lmName);


        String WIKIPEDIA_URL = "https://en.wikipedia.org/w/api.php?action=query&titles="+lmName+"&prop=revisions&rvprop=content&format=json&prop=extracts";

        if(lmName != null){
            FetchWikiDataAsync fetchWikiDataAsync = new FetchWikiDataAsync();
            fetchWikiDataAsync.execute(WIKIPEDIA_URL);
        }





    }
    private class FetchWikiDataAsync extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String[] params) {
            try {
                String sURL = params[0];

                URL url = new URL(sURL);        // Convert String URL to java.net.URL
                // Connection: to Wikipedia API
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream,                  "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                String wikiData = stringBuilder.toString();

                // Parse JSON Data
                String formattedData = parseJSONData(wikiData);

                return formattedData;

            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String formattedData)
        {
            super.onPostExecute(formattedData);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // HTML Data
                WikiT.setText(Html.fromHtml
                        (formattedData,Html.FROM_HTML_MODE_LEGACY));
            }
            else {
                // HTML Data
                WikiT.setText(Html.fromHtml(formattedData));
            }
        }
    }

    private String parseJSONData(String wikiData) {
        try {
            // Convert String JSON (wikiData) to JSON Object
            JSONObject rootJSON = new JSONObject(wikiData);
            JSONObject query = rootJSON.getJSONObject("query");
            JSONObject pages = query.getJSONObject("pages");
            JSONObject number = pages.getJSONObject(pages.keys().next());
            String formattedData = number.getString("extract");

            return formattedData;
        }
        catch (JSONException json) {
            json.printStackTrace();
        }
        return null;
    }
}


