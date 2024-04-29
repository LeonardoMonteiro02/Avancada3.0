package com.example.biblioteca;

public class RestrictedRegion extends Region {
    private boolean restricted;
    private Region mainRegion;
    private static final double R = 6371000; // Raio da Terra em metros

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

    @Override
    public Boolean calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        if (distance > 5) {
            return true;
        }
        else{
            return false;
        }
    }
}
