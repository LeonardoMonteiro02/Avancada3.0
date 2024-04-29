package com.example.biblioteca;

public class SubRegion extends Region {
    private Region mainRegion;
    private static final double R = 6371000; // Raio da Terra em metros

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

