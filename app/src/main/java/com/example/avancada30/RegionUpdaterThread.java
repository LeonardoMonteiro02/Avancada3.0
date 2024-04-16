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

import com.example.biblioteca.Region;
import com.example.biblioteca.RestrictedRegion;
import com.example.biblioteca.SubRegion;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.biblioteca.GeoCalculator;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class RegionUpdaterThread extends Thread {
    private List<Region> regions;
    private String newName;
    private double newlatitude;
    private double newlongitude;

    private Semaphore semaphore;
    private Random random = new Random();
    private boolean restricted = false;
    private Region mainRegion = null;
    private RestrictedRegion restrictedRegion = null;
    private Region region = null;
    private  SubRegion subRegion = null;
    private boolean bancovasio = false;

    private DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();

    public RegionUpdaterThread( List<Region> regions, String locationName, double latitude, double longitude, Semaphore semaphore, boolean restricted, Region mainRegion) {
        this.regions = regions;
        this.newName = locationName;
        this.newlatitude = latitude;
        this.newlongitude = longitude;
        this.semaphore = semaphore;
        this.restricted = restricted;
        this.mainRegion = mainRegion;
        this.restrictedRegion = new RestrictedRegion(locationName, latitude, longitude, Math.abs(random.nextInt()), System.nanoTime(), restricted, mainRegion);

    }
    public RegionUpdaterThread( List<Region> regions, String locationName, double latitude, double longitude, Semaphore semaphore, Region mainRegion) {
        this.regions = regions;
        this.newName = locationName;
        this.newlatitude = latitude;
        this.newlongitude = longitude;
        this.semaphore = semaphore;
        this.mainRegion = mainRegion;
        this.subRegion = new SubRegion(locationName, latitude, longitude, Math.abs(random.nextInt()), System.nanoTime(), mainRegion);

    }
    public RegionUpdaterThread( List<Region> regions, String locationName, double latitude, double longitude, Semaphore semaphore) {
        this.regions = regions;
        this.newName = locationName;
        this.newlatitude = latitude;
        this.newlongitude = longitude;
        this.semaphore = semaphore;
        this.region = new Region(locationName, latitude, longitude, System.nanoTime(), Math.abs(random.nextInt()));
    }
    public RegionUpdaterThread( List<Region> regions, String locationName, double latitude, double longitude, Semaphore semaphore,boolean bancovasio) {
        this.regions = regions;
        this.newName = locationName;
        this.newlatitude = latitude;
        this.newlongitude = longitude;
        this.semaphore = semaphore;
        this.bancovasio = bancovasio;

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


    }



    public void avaliaDados() {

        if (regions.isEmpty() && bancovasio == true) {

            if (region != null) {
                regions.add(region);
                Log.d("Consulta Lista", "Região adicionada: " + region.getName());
                region=null;
            }
            semaphore.release();
            Log.d("Consulta Lista", "Semafaro Liberado 1 ");
        } else if (regions.isEmpty() && bancovasio == false) {
            if (restrictedRegion != null) {
                regions.add(restrictedRegion);
                Log.d("Consulta Lista", "Região restrita adicionada: " + restrictedRegion.getName());
                restrictedRegion = null;
            }
            if (region != null) {
                regions.add(region);
                Log.d("Consulta Lista", "Região adicionada: " + region.getName());
                region=null;
            }
            if (subRegion != null) {
                regions.add(subRegion);
                Log.d("Consulta Lista", "Sub-região adicionada: " + subRegion.getName());
                subRegion = null;
            }
            semaphore.release();
            Log.d("Consulta Lista", "Semafaro Liberado 2");

        } else if (!regions.isEmpty() && bancovasio == true) {
            consultaLista(regions);
            semaphore.release();
            Log.d("Consulta Lista", "Semafaro Liberado 3");
        } else if (!regions.isEmpty() && bancovasio == false) {
            if (restrictedRegion != null) {
                regions.add(restrictedRegion);
                Log.d("Consulta Lista", "Região restrita adicionada: " + restrictedRegion.getName());
                restrictedRegion = null;
            }
            if (region != null) {
                regions.add(region);
                Log.d("Consulta Lista", "Região adicionada: " + region.getName());
                region=null;
            }
            if (subRegion != null) {
                regions.add(subRegion);
                Log.d("Consulta Lista", "Sub-região adicionada: " + subRegion.getName());
                subRegion = null;
            }
            if (subRegion != null && region != null && restrictedRegion != null){
                consultaLista(regions);
            }
            semaphore.release();
            Log.d("Consulta Lista", "Semafaro Liberado 4 ");
        }
    }


    private void consultaLista(List<Region> lista){
        int indexRegiaoMenorQue30 = -1;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getClass().equals(Region.class)) {
                double distancia = lista.get(i).calculateDistance(lista.get(i).getLatitude(), lista.get(i).getLongitude(), newlatitude, newlongitude);
                Log.d("Consulta Lista", "Distância da nova localização em relação a região " + i + ": " + distancia + " metros.");
                if (distancia < 30) {
                    indexRegiaoMenorQue30 = i;
                    Log.d("Consulta Lista", "Proximidade de regiões encontrada.");
                    break; // Se encontrarmos uma região a menos de 30 metros, podemos sair do loop
                }
            }
        }

        if (indexRegiaoMenorQue30 != -1) {  // New Região é menor que 30?
            if (indexRegiaoMenorQue30 < lista.size() - 1) { // Menor região não é a ultima região da lista?
                int indexProximaRegiaoRegion = -1;
                for (int j = indexRegiaoMenorQue30 + 1; j < lista.size(); j++) {
                    if (lista.get(j).getClass().equals(Region.class)) { // encontra a proxima region da lista se tiver.
                        indexProximaRegiaoRegion = j;
                        break;
                    }
                }
                if (indexProximaRegiaoRegion != -1 && (indexProximaRegiaoRegion - 1) != indexRegiaoMenorQue30) { // Se for verdade verifica o tipo do ultimo elemento ante da proxima region
                    Region regiaoAnterior = lista.get(indexProximaRegiaoRegion - 1);
                    boolean avalia = false;
                    for (int j = indexRegiaoMenorQue30 + 1; j < indexProximaRegiaoRegion; j++) {
                        double distancia = lista.get(j).calculateDistance(lista.get(j).getLatitude(), lista.get(j).getLongitude(), newlatitude, newlongitude);
                        if (lista.get(j).getClass().equals(Region.class)) { // encontra a proxima region da lista se tiver.
                            if (distancia < 5) {
                                avalia = true;
                                break; // Se encontrarmos uma região a menos de 30 metros, podemos sair do loop
                            }
                        }
                    }
                    if (avalia == false) {

                        if (regiaoAnterior.getClass().equals(SubRegion.class)) {
                            double distancia = regiaoAnterior.calculateDistance(regiaoAnterior.getLatitude(), regiaoAnterior.getLongitude(), newlatitude, newlongitude);
                            Log.d("Consulta Lista", "Distância da nova localização em relação a Subregião: " + distancia + " metros.");
                            if (distancia > 5) {
                                Log.d("Consulta Lista", "Iniciando atualização da região restrita.");
                                RestrictedRegion restrictedRegion = new RestrictedRegion(newName, newlatitude, newlongitude, Math.abs(random.nextInt()),System.nanoTime(),true, lista.get(indexRegiaoMenorQue30));
                                lista.add(restrictedRegion);
                            } else {
                                Log.d("Consulta Lista", "Distancia menor que 5 metros da Subregião.");
                            }
                        } else if (regiaoAnterior.getClass().equals(RestrictedRegion.class)) {
                            double distancia = regiaoAnterior.calculateDistance(regiaoAnterior.getLatitude(), regiaoAnterior.getLongitude(), newlatitude, newlongitude);
                            Log.d("Consulta Lista", "Distância da nova localização em relação a região restrita: " + distancia + " metros.");
                            if (distancia > 5) {
                                Log.d("Consulta Lista", "Iniciando atualização da Subregião.");
                                SubRegion subRegion = new SubRegion(newName, newlatitude, newlongitude, Math.abs(random.nextInt()),System.nanoTime(), lista.get(indexRegiaoMenorQue30));
                                lista.add(subRegion);
                            } else {
                                Log.d("Consulta Lista", "Distancia menor que 5 metros da Região restrita.");
                            }
                        }
                    }
                    else {
                        Log.d("Consulta Lista", "Distancia menor que 5 metros do intervalo de regiões.");
                    }
                }
                else {
                    Log.d("Consulta Lista", "Região encontrada é a última da Lista.");
                    boolean avalia = false;
                    for (int j = indexRegiaoMenorQue30 + 1; j < lista.size(); j++) {
                        double distancia = lista.get(j).calculateDistance(lista.get(j).getLatitude(), lista.get(j).getLongitude(), newlatitude, newlongitude);

                        if (distancia < 5) {
                            avalia = true;
                            break; // Se encontrarmos uma região a menos de 30 metros, podemos sair do loop
                        }

                    }
                    if (avalia == false){
                        Region regiaoAnterior = lista.get(lista.size() - 1);
                        if (regiaoAnterior.getClass().equals(SubRegion.class)) {
                            double distancia = regiaoAnterior.calculateDistance(regiaoAnterior.getLatitude(), regiaoAnterior.getLongitude(), newlatitude, newlongitude);
                            Log.d("Consulta Lista", "Distância da nova localização em relação a Subregião: " + distancia + " metros.");
                            if (distancia > 5) {
                                Log.d("Consulta Lista", "Iniciando atualização da região restrita.");
                                RestrictedRegion restrictedRegion = new RestrictedRegion(newName, newlatitude, newlongitude, Math.abs(random.nextInt()),System.nanoTime(),true, lista.get(indexRegiaoMenorQue30));
                                lista.add(restrictedRegion);
                            } else {
                                Log.d("Consulta Lista", "Distancia menor que 5 metros da Subregião.");
                            }
                        } else if (regiaoAnterior.getClass().equals(RestrictedRegion.class)) {
                            double distancia = regiaoAnterior.calculateDistance(regiaoAnterior.getLatitude(), regiaoAnterior.getLongitude(), newlatitude, newlongitude);
                            Log.d("Consulta Lista", "Distância da nova localização em relação a região restrita: " + distancia + " metros.");
                            if (distancia > 5) {
                                Log.d("Consulta Lista", "Iniciando atualização da Subregião.");
                                SubRegion subRegion = new SubRegion(newName, newlatitude, newlongitude, Math.abs(random.nextInt()),System.nanoTime(), lista.get(indexRegiaoMenorQue30));
                                lista.add(subRegion);
                            } else {
                                Log.d("Consulta Lista", "Distancia menor que 5 metros da Região restrita.");
                            }
                        }
                    }
                    else{
                        Log.d("Consulta Lista", "Distancia menor que 5 metros de alguma das regiões.");
                    }
                }
            } else {
                Log.d("Consulta Lista", "Iniciando atualização da Subregião.");
                if (lista.get(indexRegiaoMenorQue30).getClass().equals(Region.class)) {
                    SubRegion subRegion = new SubRegion(newName, newlatitude, newlongitude, Math.abs(random.nextInt()),System.nanoTime(), lista.get(indexRegiaoMenorQue30));
                    lista.add(subRegion);
                } else if (lista.get(indexRegiaoMenorQue30).getClass().equals(SubRegion.class)) {
                    double distancia = lista.get(indexRegiaoMenorQue30).calculateDistance(lista.get(indexRegiaoMenorQue30).getLatitude(), lista.get(indexRegiaoMenorQue30).getLongitude(), newlatitude, newlongitude);
                    Log.d("Consulta Lista", "Distância da nova localização em relação a Subregião: " + distancia + " metros.");
                    if (distancia > 5) {
                        Log.d("Consulta Lista", "Iniciando atualização da região restrita.");
                        SubRegion subRegiao = (SubRegion) lista.get(indexRegiaoMenorQue30);
                        Region regiaoPrincipal = subRegiao.getMainRegion();
                        RestrictedRegion restrictedRegion = new RestrictedRegion(newName, newlatitude, newlongitude, Math.abs(random.nextInt()),System.nanoTime(),true, regiaoPrincipal);
                        lista.add(restrictedRegion);
                    }
                    else{
                        Log.d("Consulta Lista", "Distancia menor que 5 metros da Subregião.");
                    }
                } else if (lista.get(indexRegiaoMenorQue30).getClass().equals(RestrictedRegion.class)) {
                    double distancia = lista.get(indexRegiaoMenorQue30).calculateDistance(lista.get(indexRegiaoMenorQue30).getLatitude(), lista.get(indexRegiaoMenorQue30).getLongitude(), newlatitude, newlongitude);
                    Log.d("Consulta Lista", "Distância da nova localização em relação a região restrita: " + distancia + " metros.");
                    if (distancia > 5) {
                        Log.d("Consulta Lista", "Iniciando atualização da Subregião.");
                        RestrictedRegion restrictedRegion = (RestrictedRegion) lista.get(indexRegiaoMenorQue30);
                        Region regiaoPrincipal = restrictedRegion.getMainRegion();
                        SubRegion subRegion = new SubRegion(newName, newlatitude, newlongitude, Math.abs(random.nextInt()),System.nanoTime(), regiaoPrincipal);
                        lista.add(subRegion);
                    }
                    else{
                        Log.d("Consulta Lista", "Distancia menor que 5 metros da Restrita região.");
                    }
                }
            }

        } else {
            Log.d("Consulta Lista", "Nenhuma região próxima encontrada. Iniciando adição de região.");
            Region newregion = new Region(newName, newlatitude, newlongitude,System.nanoTime(), Math.abs(random.nextInt()));
            lista.add(newregion);
        }

    }

}