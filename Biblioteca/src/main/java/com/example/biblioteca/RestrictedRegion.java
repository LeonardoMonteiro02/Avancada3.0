package com.example.biblioteca;

public class RestrictedRegion extends Region {
    private boolean restricted;
    private Region mainRegion;

    public RestrictedRegion(String name, double latitude, double longitude, int user, long timestamp, boolean restricted, Region mainRegion) {
        super(name, latitude, longitude, timestamp, user);
        this.restricted = restricted;
        this.mainRegion = mainRegion;
    }

    public boolean getRestricted() {
        return restricted;
    }
    public void setRestricted(Boolean restricted){
        this.restricted = restricted;
    }
    public void setMainRegion(Region mainRegion){
        this.mainRegion = mainRegion;
    }
    public Region getMainRegion(){
        return mainRegion;
    }
}
