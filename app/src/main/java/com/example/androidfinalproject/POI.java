package com.example.androidfinalproject;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class POI implements Parcelable {
    private String name;
    private String latitude;
    private String longitude;
    private String vicinity;
    private String type;
    public POI(String name, String latitude, String longitude, String vicinity, String type){
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.vicinity = vicinity;
        this.type = type;
    }

    protected POI(Parcel in) {
        name = in.readString();
        latitude = in.readString();
        longitude = in.readString();
        vicinity = in.readString();
        type = in.readString();
    }


    public static final Creator<POI> CREATOR = new Creator<POI>() {
        @Override
        public POI createFromParcel(Parcel in) {
            return new POI(in);
        }

        @Override
        public POI[] newArray(int size) {
            return new POI[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getLatitude() {
        if(latitude.length() >= 15){
            latitude.substring(0, 10);
        }
        return latitude;
    }

    public String getLongitude() {
        if(longitude.length() >= 15){
            longitude.substring(0,10);
        }
        return longitude;
    }
    public String getVicinity(){
        return vicinity;
    }
    public String getType() {
        if(type.contains("_")){
            type = type.replaceAll("_", " ");
        }
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeString(vicinity);
        dest.writeString(type);

    }
}
