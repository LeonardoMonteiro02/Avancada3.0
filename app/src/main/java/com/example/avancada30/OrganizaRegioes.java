package com.example.avancada30;

import static java.lang.System.nanoTime;

import java.util.List;
import java.util.Random;


public class OrganizaRegioes {

    List<Region> listaBD;
    List<Region> lista;
    private String name;
    private double latitude;
    private double longitude;
    private Random random = new Random();

    public OrganizaRegioes(List<Region> lista,List<Region> listaBD, String name, double latitude, double longitude) {
        this.lista = lista;
        this.listaBD = listaBD;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;

    }
    public OrganizaRegioes() {

    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void avaliaDados (){
        double distancia;
        boolean verificacao = false;
        for (Region region : listaBD) {
            if (region instanceof Region) {
                distancia = region.calculateDistance(region.getLatitude(), region.getLongitude(), getLatitude(), getLongitude());
                if (distancia < 30) {
                    verificacao = true;
                }
            }

        }
        if (verificacao == true) {
            Region ultimoObjeto = lista.get(lista.size() - 1);
            if (ultimoObjeto instanceof Region) {
                SubRegion newsubregion = new SubRegion(getName(), getLatitude(), getLongitude(), Math.abs(random.nextInt()), System.nanoTime(), ultimoObjeto);
                lista.add(newsubregion);
            } else if (ultimoObjeto instanceof SubRegion) {

                distancia = ultimoObjeto.calculateDistance(ultimoObjeto.getLatitude(), ultimoObjeto.getLongitude(), getLatitude(), getLongitude());
                if (distancia > 5) {
                    RestrictedRegion newrestrictregion = new RestrictedRegion(getName(), getLatitude(), getLongitude(), Math.abs(random.nextInt()), System.nanoTime(), true, ultimoObjeto);
                    lista.add(newrestrictregion);
                }
            } else if (ultimoObjeto instanceof RestrictedRegion) {
                distancia = ultimoObjeto.calculateDistance(ultimoObjeto.getLatitude(), ultimoObjeto.getLongitude(), getLatitude(), getLongitude());
                if (distancia > 5) {
                    SubRegion newsubregion = new SubRegion(getName(), getLatitude(), getLongitude(), Math.abs(random.nextInt()), System.nanoTime(), ultimoObjeto);
                    lista.add(newsubregion);
                }

            }

        }
        else {

            Region newsubregion = new Region(getName(), getLatitude(), getLongitude(), System.nanoTime(),Math.abs(random.nextInt()));
            lista.add(newsubregion);
        }
    }
}

