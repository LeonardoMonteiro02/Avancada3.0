package com.example.biblioteca;

public class SubRegion extends Region {
    private Region mainRegion;

    public SubRegion(String name, double latitude, double longitude, int user, long timestamp, Region mainRegion) {
        super(name, latitude, longitude, timestamp, user);
        this.mainRegion = mainRegion;
    }

    public Region getMainRegion() {
        return mainRegion;
    }
    public void setMainRegion(Region mainRegion){
        this.mainRegion = mainRegion;
    }
    @Override
    public String toString() {
        return "SubRegion{" +
                "name='" + getName() + '\'' +
                ", latitude=" + getLatitude() +
                ", longitude=" + getLongitude() +
                ", timestamp=" + getTimestamp() +
                ", user=" + getuser() +
                ", mainRegion=" + mainRegion.toString() +
                '}';
    }
}

