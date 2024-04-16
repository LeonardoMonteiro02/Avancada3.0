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
    private String newName;
    private double newlatitude;
    private double newlongitude;
    private Semaphore semaphore;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();


    public ConsultDatabase(List<Region> regions, String locationName, double latitude, double longitude, Semaphore semaphore) {
        this.regions = regions;
        this.newName = locationName;
        this.newlatitude = latitude;
        this.newlongitude = longitude;
        this.semaphore = semaphore;

    }

    @Override
    public void run() {
        Log.d("Consulta Banco de Dados", "Thread Inicializada");
        Log.d("Consulta Banco de Dados", "Nova localização " + newName);
        consultarBanco();
    }

    private void consultarBanco() {
        DatabaseReference regioesRef = databaseReference.child("regioes");
        List<Region> regionsFromDatabase = new ArrayList<>();
        regioesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String name = childSnapshot.child("name").getValue(String.class);
                    double latitude = childSnapshot.child("latitude").getValue(Double.class);
                    double longitude = childSnapshot.child("longitude").getValue(Double.class);
                    Long timestamp = childSnapshot.child("timestamp").getValue(Long.class);
                    int user = Math.toIntExact(childSnapshot.child("user").getValue(Long.class));
                    String key = childSnapshot.getKey();
                    int Chave = Integer.parseInt(key);

                    // Verifica se é uma SubRegion ou RestrictedRegion
                    boolean isRestricted = childSnapshot.child("restricted").exists();
                    boolean isMainRegion = childSnapshot.child("mainRegion").exists();

                    Region region;
                    if (isRestricted && isMainRegion) {
                        Region mainRegion = getMainRegionFromChildSnapshot(childSnapshot.child("mainRegion"));
                        region = new RestrictedRegion(name, latitude, longitude, user, timestamp, true, mainRegion);
                    } else if (isMainRegion) {
                        Region mainRegion = getMainRegionFromChildSnapshot(childSnapshot.child("mainRegion"));
                        region = new SubRegion(name, latitude, longitude, user, timestamp, mainRegion);
                    } else {
                        region = new Region(name, latitude, longitude, timestamp, user);
                    }

                    regionsFromDatabase.add(region);
                }
                processarRegioes(regionsFromDatabase);
            }
            private Region getMainRegionFromChildSnapshot(DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                double latitude = snapshot.child("latitude").getValue(Double.class);
                double longitude = snapshot.child("longitude").getValue(Double.class);
                Long timestamp = snapshot.child("timestamp").getValue(Long.class);
                int user = Math.toIntExact(snapshot.child("user").getValue(Long.class));
                return new Region(name, latitude, longitude, timestamp, user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Consulta Banco de Dados", "Erro na leitura do Banco de Dados: " + error.getMessage());

            }
        });
    }

    private void processarRegioes(List<Region> regionsFromDatabase) {
        avaliaDados (regionsFromDatabase);
    }



    public void avaliaDados(List<Region> listaBD) {

        int indexRegiaoMenorQue30 = -1;
        for (int i = 0; i < listaBD.size(); i++) {
            if (listaBD.get(i).getClass().equals(Region.class)) {
                double distancia = listaBD.get(i).calculateDistance(listaBD.get(i).getLatitude(), listaBD.get(i).getLongitude(), newlatitude, newlongitude);
                Log.d("Consulta Banco de Dados", "Distância da nova localização em relação a região " + i + ": " + distancia + " metros.");
                if (distancia < 30) {
                    indexRegiaoMenorQue30 = i;
                    Log.d("Consulta Banco de Dados", "Proximidade de regiões encontrada.");
                    break; // Se encontrarmos uma região a menos de 30 metros, podemos sair do loop
                }
            }
        }
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (regions.isEmpty() && listaBD.isEmpty()) {
            Log.d("Consulta Banco de Dados", "Nenhuma região próxima encontrada. Iniciando adição de região.");
            semaphore.release();
            Log.d("Consulta Banco de Dados", "Semafaro Liberado 1");
            RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore);
            thread.start();
        } else if (regions.isEmpty() && !listaBD.isEmpty()) {
            semaphore.release();
            Log.d("Consulta Banco de Dados", "Semafaro Liberado 2");
            analise(indexRegiaoMenorQue30,listaBD);

        } else if (!regions.isEmpty() && listaBD.isEmpty()) {
            semaphore.release();
            Log.d("Consulta Banco de Dados", "Semafaro Liberado 3");
            RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore,true);
            thread.start();

        } else if (!regions.isEmpty() && !listaBD.isEmpty()) {
            semaphore.release();
            Log.d("Consulta Banco de Dados", "Semafaro Liberado 4");
            if (indexRegiaoMenorQue30 != -1){
                analise(indexRegiaoMenorQue30,listaBD);
            }
            else {
                RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore);
                thread.start();
            }

        }

    }


    private void analise(int indexRegiaoMenorQue30,List<Region> listaBD){

        if (indexRegiaoMenorQue30 != -1) {  // New Região é menor que 30?
            if (indexRegiaoMenorQue30 < listaBD.size() - 1) { // Menor região não é a ultima região da lista?
                int indexProximaRegiaoRegion = -1;
                for (int j = indexRegiaoMenorQue30 + 1; j < listaBD.size(); j++) {
                    if (listaBD.get(j).getClass().equals(Region.class)) { // encontra a proxima region da lista se tiver.
                        indexProximaRegiaoRegion = j;
                        break;
                    }
                }
                if (indexProximaRegiaoRegion != -1 && (indexProximaRegiaoRegion - 1) != indexRegiaoMenorQue30) { // Se for verdade verifica o tipo do ultimo elemento ante da proxima region
                    Region regiaoAnterior = listaBD.get(indexProximaRegiaoRegion - 1);
                    boolean avalia = false;
                    for (int j = indexRegiaoMenorQue30 + 1; j < indexProximaRegiaoRegion; j++) {
                        double distancia = listaBD.get(j).calculateDistance(listaBD.get(j).getLatitude(), listaBD.get(j).getLongitude(), newlatitude, newlongitude);
                        if (listaBD.get(j).getClass().equals(Region.class)) { // encontra a proxima region da lista se tiver.
                            if (distancia < 5) {
                                avalia = true;
                                break; // Se encontrarmos uma região a menos de 30 metros, podemos sair do loop
                            }
                        }
                    }
                    if (avalia == false) {

                        if (regiaoAnterior.getClass().equals(SubRegion.class)) {
                            double distancia = regiaoAnterior.calculateDistance(regiaoAnterior.getLatitude(), regiaoAnterior.getLongitude(), newlatitude, newlongitude);
                            Log.d("Consulta Banco de Dados", "Distância da nova localização em relação a Subregião " + distancia + " metros.");
                            if (distancia > 5) {
                                Log.d("Consulta Banco de Dados", "Iniciando atualização da região restrita.");
                                RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore, true, listaBD.get(indexRegiaoMenorQue30));
                                thread.start();
                            } else {
                                Log.d("Consulta Banco de Dados", "Distancia menor que 5 metros da Subregião.");
                            }
                        } else if (regiaoAnterior.getClass().equals(RestrictedRegion.class)) {
                            double distancia = regiaoAnterior.calculateDistance(regiaoAnterior.getLatitude(), regiaoAnterior.getLongitude(), newlatitude, newlongitude);
                            Log.d("Consulta Banco de Dados", "Distância da nova localização em relação a região restrita: " + distancia + " metros.");
                            if (distancia > 5) {
                                Log.d("Consulta Banco de Dados", "Iniciando atualização da Subregião.");
                                RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore, listaBD.get(indexRegiaoMenorQue30));
                                thread.start();
                            } else {
                                Log.d("Consulta Banco de Dados", "Distancia menor que 5 metros da Região restrita.");
                            }
                        }
                    }
                    else {
                        Log.d("Consulta Banco de Dados", "Distancia menor que 5 metros do intervalo de regiões.");
                    }
                }
                else {
                    Log.d("Consulta Banco de Dados", "Região encontrada é a última do Banco.");
                    boolean avalia = false;
                    for (int j = indexRegiaoMenorQue30 + 1; j < listaBD.size(); j++) {
                        double distancia = listaBD.get(j).calculateDistance(listaBD.get(j).getLatitude(), listaBD.get(j).getLongitude(), newlatitude, newlongitude);
                        if (listaBD.get(j).getClass().equals(Region.class)) { // encontra a proxima region da lista se tiver.
                            if (distancia < 5) {
                                avalia = true;
                                break; // Se encontrarmos uma região a menos de 30 metros, podemos sair do loop
                            }
                        }
                    }
                    if (avalia == false){
                        Region regiaoAnterior = listaBD.get(listaBD.size() - 1);
                        if (regiaoAnterior.getClass().equals(SubRegion.class)) {
                            double distancia = regiaoAnterior.calculateDistance(regiaoAnterior.getLatitude(), regiaoAnterior.getLongitude(), newlatitude, newlongitude);
                            Log.d("Consulta Banco de Dados", "Distância da nova localização em relação a Subregião: " + distancia + " metros.");
                            if (distancia > 5) {
                                Log.d("Consulta Banco de Dados", "Iniciando atualização da região restrita.");
                                RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore, true, listaBD.get(indexRegiaoMenorQue30));
                                thread.start();
                            } else {
                                Log.d("Consulta Banco de Dados", "Distancia menor que 5 metros da Subregião.");
                            }
                        } else if (regiaoAnterior.getClass().equals(RestrictedRegion.class)) {
                            double distancia = regiaoAnterior.calculateDistance(regiaoAnterior.getLatitude(), regiaoAnterior.getLongitude(), newlatitude, newlongitude);
                            Log.d("Consulta Banco de Dados", "Distância da nova localização em relação a região restrita: " + distancia + " metros.");
                            if (distancia > 5) {
                                Log.d("Consulta Banco de Dados", "Iniciando atualização da Subregião.");
                                RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore, listaBD.get(indexRegiaoMenorQue30));
                                thread.start();
                            } else {
                                Log.d("Consulta Banco de Dados", "Distancia menor que 5 metros da Região restrita.");
                            }
                        }
                    }
                    else{
                        Log.d("Consulta Banco de Dados", "Distancia menor que 5 metros de alguma das regiões.");
                    }
                }
            } else {
                Log.d("Consulta Banco de Dados", "Iniciando atualização da Subregião.");
                if (listaBD.get(indexRegiaoMenorQue30).getClass().equals(Region.class)) {
                    RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore, listaBD.get(indexRegiaoMenorQue30));
                    thread.start();
                } else if (listaBD.get(indexRegiaoMenorQue30).getClass().equals(SubRegion.class)) {
                    double distancia = listaBD.get(indexRegiaoMenorQue30).calculateDistance(listaBD.get(indexRegiaoMenorQue30).getLatitude(), listaBD.get(indexRegiaoMenorQue30).getLongitude(), newlatitude, newlongitude);
                    Log.d("Consulta Banco de Dados", "Distância da nova localização em relação a Subregião: " + distancia + " metros.");
                    if (distancia > 5) {
                        Log.d("Consulta Banco de Dados", "Iniciando atualização da região restrita.");
                        SubRegion subRegiao = (SubRegion) listaBD.get(indexRegiaoMenorQue30);
                        Region regiaoPrincipal = subRegiao.getMainRegion();
                        RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore, true, regiaoPrincipal);
                        thread.start();
                    }
                    else{
                        Log.d("Consulta Banco de Dados", "Distancia menor que 5 metros da Subregião.");
                    }
                } else if (listaBD.get(indexRegiaoMenorQue30).getClass().equals(RestrictedRegion.class)) {
                    double distancia = listaBD.get(indexRegiaoMenorQue30).calculateDistance(listaBD.get(indexRegiaoMenorQue30).getLatitude(), listaBD.get(indexRegiaoMenorQue30).getLongitude(), newlatitude, newlongitude);
                    Log.d("Consulta Banco de Dados", "Distância da nova localização em relação a região restrita: " + distancia + " metros.");
                    if (distancia > 5) {
                        Log.d("Consulta Banco de Dados", "Iniciando atualização da Subregião.");
                        RestrictedRegion restrictedRegion = (RestrictedRegion) listaBD.get(indexRegiaoMenorQue30);
                        Region regiaoPrincipal = restrictedRegion.getMainRegion();
                        RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore, regiaoPrincipal);
                        thread.start();
                    }
                    else{
                        Log.d("Consulta Banco de Dados", "Distancia menor que 5 metros da Restrita região.");
                    }
                }
            }

        } else {
            Log.d("Consulta Banco de Dados", "Nenhuma região próxima encontrada. Iniciando adição de região.");
            RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore);
            thread.start();
        }
    }
}

