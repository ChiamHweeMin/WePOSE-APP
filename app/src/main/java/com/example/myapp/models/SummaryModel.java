package com.example.myapp.models;

public class SummaryModel {
    private String date;
    private Integer slouchCount;
    private String elapsedTime;
    public SummaryModel(String date, String elapsedTime, Integer slouchCount) {
        this.date = date;
        this.elapsedTime = elapsedTime;
        this.slouchCount = slouchCount;
    }

    public String getDate() {
        return date;
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    public Integer getSlouchCount() {
        return slouchCount;
    }
}
