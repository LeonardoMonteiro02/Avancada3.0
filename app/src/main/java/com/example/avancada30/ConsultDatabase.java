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
import com.example.biblioteca.Region;
import com.example.biblioteca.RestrictedRegion;
import com.example.biblioteca.SubRegion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

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
                    try {
                        JSONObject encryptedData = new JSONObject(childSnapshot.getValue(String.class));

                        if (encryptedData.has("restricted") && encryptedData.has("mainRegion")) {
                            String encryptedRestricted = encryptedData.getString("restricted");
                            boolean restricted = Boolean.parseBoolean(CriptografiaAES.descriptografar(encryptedRestricted));

                            JSONObject encryptedMainRegion = encryptedData.getJSONObject("mainRegion");

                            // Reconstruir o objeto da região principal
                            String encryptedMainRegionLatitude = encryptedMainRegion.getString("latitude");
                            String encryptedMainRegionLongitude = encryptedMainRegion.getString("longitude");
                            String encryptedMainRegionName = encryptedMainRegion.getString("name");
                            String encryptedMainRegionTimestamp = encryptedMainRegion.getString("timestamp");
                            String encryptedMainRegionUser = encryptedMainRegion.getString("user");

                            String encryptedLatitude = encryptedData.getString("latitude");
                            String encryptedLongitude = encryptedData.getString("longitude");
                            String encryptedName = encryptedData.getString("name");
                            String encryptedTimestamp = encryptedData.getString("timestamp");
                            String encryptedUser = encryptedData.getString("user");

                            // Descriptografar os valores dos atributos
                            String latitude = CriptografiaAES.descriptografar(encryptedLatitude);
                            String longitude = CriptografiaAES.descriptografar(encryptedLongitude);
                            String name = CriptografiaAES.descriptografar(encryptedName);
                            Long timestamp = Long.valueOf(CriptografiaAES.descriptografar(encryptedTimestamp));
                            int user = Integer.parseInt(CriptografiaAES.descriptografar(encryptedUser));

                            // Descriptografar os valores dos atributos da região principal
                            String mainRegionLatitude = CriptografiaAES.descriptografar(encryptedMainRegionLatitude);
                            String mainRegionLongitude = CriptografiaAES.descriptografar(encryptedMainRegionLongitude);
                            String mainRegionName = CriptografiaAES.descriptografar(encryptedMainRegionName);
                            Long mainRegionTimestamp = Long.valueOf(CriptografiaAES.descriptografar(encryptedMainRegionTimestamp));
                            int mainRegionUser = Integer.parseInt(CriptografiaAES.descriptografar(encryptedMainRegionUser));

                            // Construir o objeto da região principal
                            Region mainRegion = new Region(mainRegionName, Double.parseDouble(mainRegionLatitude), Double.parseDouble(mainRegionLongitude), mainRegionTimestamp, mainRegionUser);

                            // Construir o objeto RestrictedRegion com os valores descriptografados
                            RestrictedRegion restrictedRegion = new RestrictedRegion(name, Double.parseDouble(latitude), Double.parseDouble(longitude), user, timestamp, restricted, mainRegion);

                            // Adicionar a região restrita reconstruída à lista de regiões
                            regionsFromDatabase.add(restrictedRegion);
                        }

                        else if (encryptedData.has("mainRegion")) {
                            JSONObject encryptedMainRegion = encryptedData.getJSONObject("mainRegion");

                            // Reconstruir o objeto da região principal
                            String encryptedMainRegionLatitude = encryptedMainRegion.getString("latitude");
                            String encryptedMainRegionLongitude = encryptedMainRegion.getString("longitude");
                            String encryptedMainRegionName = encryptedMainRegion.getString("name");
                            String encryptedMainRegionTimestamp = encryptedMainRegion.getString("timestamp");
                            String encryptedMainRegionUser = encryptedMainRegion.getString("user");

                            // Descriptografar os valores dos atributos da região principal
                            String mainRegionLatitude = CriptografiaAES.descriptografar(encryptedMainRegionLatitude);
                            String mainRegionLongitude = CriptografiaAES.descriptografar(encryptedMainRegionLongitude);
                            String mainRegionName = CriptografiaAES.descriptografar(encryptedMainRegionName);
                            Long mainRegionTimestamp = Long.valueOf(CriptografiaAES.descriptografar(encryptedMainRegionTimestamp));
                            int mainRegionUser = Integer.parseInt(CriptografiaAES.descriptografar(encryptedMainRegionUser));

                            String encryptedLatitude = encryptedData.getString("latitude");
                            String encryptedLongitude = encryptedData.getString("longitude");
                            String encryptedName = encryptedData.getString("name");
                            String encryptedTimestamp = encryptedData.getString("timestamp");
                            String encryptedUser = encryptedData.getString("user");

                            // Descriptografar os valores dos atributos
                            String latitude = CriptografiaAES.descriptografar(encryptedLatitude);
                            String longitude = CriptografiaAES.descriptografar(encryptedLongitude);
                            String name = CriptografiaAES.descriptografar(encryptedName);
                            Long timestamp = Long.valueOf(CriptografiaAES.descriptografar(encryptedTimestamp));
                            int user = Integer.parseInt(CriptografiaAES.descriptografar(encryptedUser));

                            // Construir o objeto da região principal
                            Region mainRegion = new Region(mainRegionName, Double.parseDouble(mainRegionLatitude), Double.parseDouble(mainRegionLongitude), mainRegionTimestamp, mainRegionUser);

                            // Construir o objeto SubRegion com os valores descriptografados
                            SubRegion subRegion = new SubRegion(name, Double.parseDouble(latitude), Double.parseDouble(longitude), user, timestamp, mainRegion);

                            // Adicionar a sub-região reconstruída à lista de regiões
                            regionsFromDatabase.add(subRegion);
                        }

                        // Verificar se é uma Região simples
                        else if (encryptedData.has("latitude") && encryptedData.has("longitude") &&
                                encryptedData.has("name") && encryptedData.has("timestamp") &&
                                encryptedData.has("user")) {

                            String encryptedLatitude = encryptedData.getString("latitude");
                            String encryptedLongitude = encryptedData.getString("longitude");
                            String encryptedName = encryptedData.getString("name");
                            String encryptedTimestamp = encryptedData.getString("timestamp");
                            String encryptedUser = encryptedData.getString("user");

                            // Descriptografar os valores dos atributos
                            String latitude = CriptografiaAES.descriptografar(encryptedLatitude);
                            String longitude = CriptografiaAES.descriptografar(encryptedLongitude);
                            String name = CriptografiaAES.descriptografar(encryptedName);
                            Long timestamp = Long.valueOf(CriptografiaAES.descriptografar(encryptedTimestamp));
                            int user = Integer.parseInt(CriptografiaAES.descriptografar(encryptedUser));

                            // Construir o objeto Region com os valores descriptografados
                            Region region = new Region(name, Double.parseDouble(latitude), Double.parseDouble(longitude), timestamp, user);

                            // Adicionar a região reconstruída à lista de regiões
                            regionsFromDatabase.add(region);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        avaliaDados (regionsFromDatabase);
    }



    public void avaliaDados(List<Region> listaBD) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(regions.isEmpty() && listaBD.isEmpty()){
            Log.d("Consulta Banco de Dados", " Lista e Banco Vazios ");
            semaphore.release();
            RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore,true);
            thread.start();
        } else if(!regions.isEmpty() && listaBD.isEmpty()){
            Log.d("Consulta Banco de Dados", " Lista Cheia e Banco Vazio ");
            semaphore.release();
            RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore,true);
            thread.start();
        }else if (regions.isEmpty() && !listaBD.isEmpty()){
            Log.d("Consulta Banco de Dados", " Lista Vazia e Banco Cheio ");
            semaphore.release();
            verificaBanco(listaBD);

        }else if (!regions.isEmpty() && !listaBD.isEmpty()) {
            Log.d("Consulta Banco de Dados", " Lista e Banco Cheios ");
            semaphore.release();
            boolean verifica = false;
            for (int i = 0; i < listaBD.size(); i++) {
                if (listaBD.get(i).getClass().equals(Region.class)) { //Verifica se alguma região(Region) do banco esta a mesnos de 30 metros de distancia do novo dado
                    double distancia = listaBD.get(i).calculateDistance(listaBD.get(i).getLatitude(), listaBD.get(i).getLongitude(), newlatitude, newlongitude);
                    if (distancia < 30) {
                        verifica = true;
                        break; // Se encontrarmos uma região a menos de 30 metros, podemos sair do loop
                    }
                }
            }
            if (verifica == true){
                verificaBanco(listaBD);
            }else{ // passa somente os dados.
                RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore,false);
                thread.start();
            }
        }
    }

    private void verificaBanco(List<Region> listaBD) {
        int indexRegiaoMenorQue30 = -1;
        for (int i = 0; i < listaBD.size(); i++) {
            if ("Region".equals(nomeSimplesUltimoElemento(listaBD, i))) {
                double distancia = listaBD.get(i).calculateDistance(listaBD.get(i).getLatitude(), listaBD.get(i).getLongitude(), newlatitude, newlongitude);
                if (distancia < 30) {
                    indexRegiaoMenorQue30 = i;
                    break;
                }
            }
        }

        if (indexRegiaoMenorQue30 != -1) {
            if (indexRegiaoMenorQue30 == listaBD.size() - 1) {
                Log.d("Consulta Banco de Dados", " Adicionando SubRegion (Último elemento do banco)");
                RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore, listaBD.get(indexRegiaoMenorQue30));
                thread.start();
            } else {
                boolean avalia = false;
                int posUltimoElementoAssociadoaRegion = -1;
                for (int i = indexRegiaoMenorQue30 +1; i < listaBD.size(); i++) {
                    if (("SubRegion".equals(nomeSimplesUltimoElemento(listaBD, i))) || ("RestrictedRegion".equals(nomeSimplesUltimoElemento(listaBD, i)))) {
                        double distancia = listaBD.get(i).calculateDistance(listaBD.get(i).getLatitude(), listaBD.get(i).getLongitude(), newlatitude, newlongitude);
                        if (distancia < 5) {
                            avalia = true;
                            break;
                        }
                    } else {
                        posUltimoElementoAssociadoaRegion = i - 1;
                        break;
                    }
                }
                if (avalia) {
                    Log.d("Consulta Banco de Dados", " Nova região não pode ser inserida (Distância menor que 5 metros detectada)");
                } else if ((posUltimoElementoAssociadoaRegion != -1) && (!avalia)) {
                    Log.d("Consulta Banco de Dados", " Encontrou uma Region após indexRegiaoMenorQue30 e nenhum elemento SubRegion ou RestrictedRegion associado a indexRegiaoMenorQue30 está a menos de 5 metros de distância da nova região 1");
                    verificaTipo(listaBD, posUltimoElementoAssociadoaRegion);
                } else if ((posUltimoElementoAssociadoaRegion == -1) && (!avalia)) {
                    Log.d("Consulta Banco de Dados", " Não encontrou uma Region após indexRegiaoMenorQue30 e nenhum elemento SubRegion ou RestrictedRegion associado a indexRegiaoMenorQue30 está a menos de 5 metros de distância da nova região 2");
                    verificaTipo(listaBD, listaBD.size() - 1);
                }
            }
        } else {
            Log.d("Consulta Banco de Dados", " Nenhuma região do banco está a menos de 30 metros de distância do novo dado");
            RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore);
            thread.start();
        }
    }

    private void verificaTipo(List<Region> listaBD, int index) {
        if ("SubRegion".equals(nomeSimplesUltimoElemento(listaBD, index))) {
            Log.d("Consulta Banco de Dados", " Adicionando RestrictedRegion");
            SubRegion subregion = (SubRegion)listaBD.get(index);
            Region mainRegion = subregion.getMainRegion();
            RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore, true, mainRegion);
            thread.start();
        } else {
            Log.d("Consulta Banco de Dados", " Adicionando SubRegion");
            RestrictedRegion restrictedRegion = (RestrictedRegion) listaBD.get(index);
            Region mainRegion = restrictedRegion.getMainRegion();
            RegionUpdaterThread thread = new RegionUpdaterThread(regions, newName, newlatitude, newlongitude, semaphore, mainRegion);
            thread.start();
        }
    }

    public static String nomeSimplesUltimoElemento(List<?> listaBD, int index) {
        if (listaBD == null || listaBD.isEmpty()) {
            return null;
        } else {
            Object ultimoElemento = listaBD.get(index);
            return ultimoElemento.getClass().getSimpleName();
        }
    }




}

