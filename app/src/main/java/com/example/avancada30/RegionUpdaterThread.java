/**
 * Classe responsável por atualizar a lista de regiões com base em novos dados de localização.
 *
 * Esta classe implementa uma thread que executa a lógica para adicionar uma nova região à lista de regiões.
 * Ao receber uma nova localização, adquire a permissão de um semáforo antes de acessar a lista de regiões.
 * Verifica se a região já existe na lista. Se não existir, verifica se a nova região está a menos de 30 metros de distância de outras regiões na lista.
 * Se a nova região não estiver muito próxima, cria um objeto Region com os dados da localização e o adiciona à lista de regiões.
 * Registra mensagens no log para indicar as ações realizadas ou situações encontradas.
 * Libera a permissão do semáforo após acessar a lista de regiões.
 * Utiliza uma classe GeoCalculator para calcular a distância entre a nova região e as regiões existentes na lista.
 *
 * Autor: Leonardo Monteiro
 * Data: 05/04/2024
 */


package com.example.avancada30;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.biblioteca.GeoCalculator;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class RegionUpdaterThread extends Thread {
    private List<Region> regions;
    private String locationName;
    private double latitude;
    private double longitude;

    private Semaphore semaphore;
    Random random = new Random();
    Context contesto;

    private DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();

    public RegionUpdaterThread(Context context, List<Region> regions, String locationName, double latitude, double longitude, Semaphore semaphore) {
        this.regions = regions;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.semaphore = semaphore;
        this.contesto = context;
    }

    /**
     * Executa a lógica para adicionar uma nova região à lista de regiões.
     * Adquire a permissão do semáforo antes de acessar a lista.
     * Verifica se a região já existe na lista. Se não existir, verifica se a nova região está a menos de 30 metros de distância de outras regiões na lista.
     * Se não estiver muito próxima, cria um objeto Region com os dados da localização e o adiciona à lista de regiões.
     * Registra mensagens no log para indicar as ações realizadas ou situações encontradas.
     * Finalmente, libera a permissão do semáforo após acessar a lista.
     */
    @Override
    public void run() {

            // Adquira a permissão do semáforo antes de acessar a lista
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        avaliaDados();
        semaphore.release();

    }



    public void avaliaDados (){
        double distancia;
        boolean verificacao = false;
        for (Region region : regions) {
            if (region instanceof Region) {
                distancia = region.calculateDistance(region.getLatitude(), region.getLongitude(), latitude, longitude);
                Log.d("Consulta Lista", "Distancia é igual:  " + distancia);
                if (distancia < 30) {
                    verificacao = true;
                }
            }

        }
        if (verificacao == true) {
            Region ultimoObjeto = regions.get(regions.size() - 1);
            if (ultimoObjeto instanceof Region) {
                Log.d("Consulta Lista", "Nome localização " + locationName);
                SubRegion newsubregion = new SubRegion(locationName, latitude, longitude, Math.abs(random.nextInt()), System.nanoTime(), ultimoObjeto);
                regions.add(newsubregion);
                Log.d("Consulta Na Lista", "SubRegião Salva na Lista");
                Log.d("Consulta Na Lista", "Tamanho da Lista: " + regions.size());
            } else if (ultimoObjeto instanceof SubRegion) {

                distancia = ultimoObjeto.calculateDistance(ultimoObjeto.getLatitude(), ultimoObjeto.getLongitude(), latitude, longitude);
                if (distancia > 5) {
                    Log.d("Consulta Lista", "Nome localização " + locationName);
                    RestrictedRegion newrestrictregion = new RestrictedRegion(locationName, latitude, longitude, Math.abs(random.nextInt()), System.nanoTime(), true, ultimoObjeto);
                    regions.add(newrestrictregion);
                    Log.d("Consulta Na Lista", "Região Restrita Salva na Lista");
                    Log.d("Consulta Na Lista", "Tamanho da Lista: " + regions.size());
                }
                else {
                    Log.d("Consulta Na Lista", "A nova Região restrita está muito próxima da SubRegião  da Lista");
                }
            } else if (ultimoObjeto instanceof RestrictedRegion) {
                distancia = ultimoObjeto.calculateDistance(ultimoObjeto.getLatitude(), ultimoObjeto.getLongitude(), latitude, longitude);
                if (distancia > 5) {
                    Log.d("Consulta Lista", "Nome localização " + locationName);
                    SubRegion newsubregion = new SubRegion(locationName, latitude, longitude, Math.abs(random.nextInt()), System.nanoTime(), ultimoObjeto);
                    regions.add(newsubregion);
                    Log.d("Consulta Na Lista", "SubRegião Salva na Lista");
                    Log.d("Consulta Na Lista", "Tamanho da Lista: " + regions.size());
                }
                else {
                    Log.d("Consulta Na Lista", "A nova Subregião está muito próxima da região restita da Lista");
                }

            }

        }
        else {
            Log.d("Consulta Lista", "Nome localização " + locationName);
            Region newsubregion = new Region(locationName, latitude, longitude, System.nanoTime(),Math.abs(random.nextInt()));
            regions.add(newsubregion);
            Log.d("Consulta Na Lista", "Região Salva na Lista");
            Log.d("Consulta Na Lista", "Tamanho da Lista: " + regions.size());
        }
    }

}