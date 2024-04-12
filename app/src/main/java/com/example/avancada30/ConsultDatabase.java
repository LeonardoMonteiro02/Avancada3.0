/**
 * Esta classe representa uma thread responsável por consultar o banco de dados Firebase para obter informações sobre as regiões armazenadas.
 * Ela implementa a lógica para verificar se uma nova região a ser adicionada já existe no banco de dados e se está muito próxima de outras regiões existentes.
 * Se a nova região não existir no banco de dados e não estiver muito próxima de outras regiões, inicia uma nova thread para atualizar as regiões.
 *
 * Principais funcionalidades:
 * - Consulta o banco de dados Firebase para obter informações sobre as regiões armazenadas.
 * - Verifica se uma nova região a ser adicionada já existe no banco de dados e se está muito próxima de outras regiões existentes.
 * - Inicia uma nova thread para atualizar as regiões, se necessário.
 * - Registra mensagens de log para monitorar o status da consulta ao banco de dados.
 *
 * Autor: Leonardo Monteiro
 * Data: 05/04/2024
 */


package com.example.avancada30;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

import com.example.biblioteca.GeoCalculator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ConsultDatabase extends Thread {
    private List<Region> regions;
    private String locationName;
    private double latitude;
    private double longitude;
    private Semaphore semaphore;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private Context contesto;

    public ConsultDatabase(Context contesto, List<Region> regions, String locationName, double latitude, double longitude, Semaphore semaphore) {
        this.regions = regions;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.semaphore = semaphore;
        this.contesto = contesto;
    }

    @Override
    public void run() {

        consultarBanco();
    }

    private void consultarBanco() {
        DatabaseReference regioesRef = databaseReference.child("regioes");
        List<Region> regionsFromDatabase = new ArrayList<>();
        regioesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Log.d("Consulta Banco de Dados", "Inicializada");
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String name = childSnapshot.child("name").getValue(String.class);
                    double latitude = childSnapshot.child("latitude").getValue(Double.class);
                    double longitude = childSnapshot.child("longitude").getValue(Double.class);
                    Long timestamp = childSnapshot.child("timestamp").getValue(Long.class);
                    int user = Math.toIntExact(childSnapshot.child("user").getValue(Long.class));
                    String key = childSnapshot.getKey();
                    int Chave = Integer.parseInt(key);

                    Region region = new Region(name, latitude, longitude, timestamp, user);
                    regionsFromDatabase.add(region);
                }
                processarRegioes(regionsFromDatabase);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Consulta Banco de Dados", "Erro na leitura do Banco de Dados: " + error.getMessage());

            }
        });
    }

    private void processarRegioes(List<Region> regionsFromDatabase) {
        Log.d("Consulta Banco de Dados", "Nome localização " + locationName);
        avaliaDados (regionsFromDatabase);
    }

    private boolean checkRegionProximity(double latitude, double longitude, List<Region> regions) {
        GeoCalculator cal = new GeoCalculator();
        for (Region region : regions) {
            double distance = cal.calculateDistance(region.getLatitude(), region.getLongitude(), latitude, longitude);
            if (distance < 30) {
                return true;
            }
        }
        return false;
    }

    public void avaliaDados (List<Region> listaBD){
        double distancia;
        boolean verificacao = false;
        for (Region region : listaBD) {
            if (region instanceof Region) {
                distancia = region.calculateDistance(region.getLatitude(), region.getLongitude(), latitude, longitude);
                if (distancia < 30) {
                    verificacao = true;
                }
            }

        }
        if (verificacao == true) {
            Region ultimoObjeto = listaBD.get(listaBD.size() - 1);
            if (ultimoObjeto instanceof Region) {
                RegionUpdaterThread thread = new RegionUpdaterThread(contesto,regions, locationName, latitude, longitude, semaphore);
                thread.start();
            } else if (ultimoObjeto instanceof SubRegion) {
                distancia = ultimoObjeto.calculateDistance(ultimoObjeto.getLatitude(), ultimoObjeto.getLongitude(), latitude, longitude);
                if (distancia > 5) {
                    RegionUpdaterThread thread = new RegionUpdaterThread(contesto,regions, locationName, latitude, longitude, semaphore);
                    thread.start();
                }
            } else if (ultimoObjeto instanceof RestrictedRegion) {
                distancia = ultimoObjeto.calculateDistance(ultimoObjeto.getLatitude(), ultimoObjeto.getLongitude(), latitude, longitude);
                if (distancia > 5) {
                    RegionUpdaterThread thread = new RegionUpdaterThread(contesto,regions, locationName, latitude, longitude, semaphore);
                    thread.start();
                }

            }

        }
        else {

            RegionUpdaterThread thread = new RegionUpdaterThread(contesto,regions, locationName, latitude, longitude, semaphore);
            thread.start();
        }
    }
}