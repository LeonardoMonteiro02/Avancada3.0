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

    public ConsultDatabase(List<Region> regions, String locationName, double latitude, double longitude, Semaphore semaphore) {
        this.regions = regions;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.semaphore = semaphore;
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
                semaphore.release();
            }
        });
    }

    private void processarRegioes(List<Region> regionsFromDatabase) {
        boolean regionExists = false;
        for (Region region : regionsFromDatabase) {
            if (region.getName().equals(locationName)) {
                regionExists = true;
                break;
            }
        }

        if (!regionExists) {
            boolean tooClose = checkRegionProximity(latitude, longitude, regionsFromDatabase);
            if (!tooClose) {
                RegionUpdaterThread thread = new RegionUpdaterThread(regions, locationName, latitude, longitude, semaphore);
                semaphore.release();
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Log.d("Consulta Banco de Dados ", "A nova região está muito próxima de outra região do Banco");
            }
        } else {
            Log.d("Consulta Banco de Dados", "Esta região já está na lista do Banco de Dados");
        }
        Log.d("Consulta Banco de Dados", "Thread Finalizada");
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
}
