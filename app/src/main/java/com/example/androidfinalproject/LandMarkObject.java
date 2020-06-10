package com.example.androidfinalproject;

public class LandMarkObject extends MainActivity{
    private String landmarkName;
    private String entityIDname;
    private float confidence;
    public LandMarkObject(String landmarkName, String entityIDname, float confidence){
        this.landmarkName = landmarkName;
        this.entityIDname = entityIDname;
        this.confidence = confidence;
    }
    public String entityIDname() {
        return entityIDname;
    }

    public float confidence() {
        return confidence;
    }

    public String landmarkName() {
        return landmarkName;
    }
}
