package com.example.androidfinalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class VisitedLocations extends AppCompatActivity {

    ArrayList<String> recieved;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visited_locations);
        recieved = new ArrayList<>();
        listView = findViewById(R.id.id_visitedLV);

        Intent intent = getIntent();
        recieved = intent.getStringArrayListExtra("arrayList");
        if(recieved != null) {
            CustomAdaptorVL customAdaptor = new CustomAdaptorVL(this, R.layout.customvl_adaptor, recieved);
            listView.setAdapter(customAdaptor);
        }

    }
    //Custom Adaptor Set Up
    public class CustomAdaptorVL extends ArrayAdapter<String> {
        Context parentContext;
        List<String> list;
        int xmlResource;

        public CustomAdaptorVL(@NonNull Context context, int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
            parentContext = context;
            xmlResource = resource;
            list = objects;
        }


        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)parentContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View adaptorView = layoutInflater.inflate(xmlResource, null);
            Button wikiData = adaptorView.findViewById(R.id.id_Wikivl);
            TextView name = adaptorView.findViewById(R.id.id_vlName);

            wikiData.setFocusable(false);


            wikiData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(VisitedLocations.this, Activity2.class);
                    intent.putExtra(MainActivity.EXTRA_LMNAME, list.get(position));
                    startActivity(intent);
                }
            });


            name.setText(list.get(position));

            return adaptorView;
        }
    }
}
