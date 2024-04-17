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
        this.region = new Region(locationName, latitude, longitude, System.nanoTime(), Math.abs(random.nextInt()));

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

        if (regions.isEmpty() && bancovasio == true && region != null) {
            Log.d("Consulta Lista", " Lista e Banco Vazia ");
            regions.add(region);
            Log.d("Consulta Lista", "Região adicionada: " + region.getName());
            semaphore.release();

        } else if (regions.isEmpty() && bancovasio == false) {
            Log.d("Consulta Lista", " Lista Vazia e Banco Cheio ");
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

        } else if (!regions.isEmpty() && bancovasio == true) {
            Log.d("Consulta Lista", " Lista Cheia e Banco Vazio ");
            verificaLista(regions);
            semaphore.release();

        } else if (!regions.isEmpty() && bancovasio == false) {
            Log.d("Consulta Lista", " Lista Cheia e Banco Cheio ");

            if (restrictedRegion != null && region != null && subRegion != null && bancovasio == false) {
                verificaLista(regions);
            }
            if (region != null) {
                regions.add(region);
                region=null;
            }
            if (subRegion != null) {
                regions.add(subRegion);
                subRegion = null;
            }
            semaphore.release();
        }
    }

    private void verificaLista(List<Region> lista) {
        int indexRegiaoMenorQue30 = -1;
        for (int i = 0; i < lista.size(); i++) {
            if ("Region".equals(nomeSimplesUltimoElemento(lista, i))) {
                double distancia = lista.get(i).calculateDistance(lista.get(i).getLatitude(), lista.get(i).getLongitude(), newlatitude, newlongitude);
                Log.d("Consulta Lista", "Distância da região " + i + " : " + distancia + " metros.");
                if (distancia < 30) {
                    indexRegiaoMenorQue30 = i;
                    break;
                }
            }
        }

        if (indexRegiaoMenorQue30 != -1) {
            if (indexRegiaoMenorQue30 == lista.size() - 1) {
                Log.d("Consulta Lista", "Adicionando SubRegion (Último elemento da lista)");
                SubRegion newSubRegion = new SubRegion(newName, newlatitude, newlongitude, Math.abs(random.nextInt()), System.nanoTime(), lista.get(indexRegiaoMenorQue30));
                lista.add(newSubRegion);
            } else {

                boolean avalia = false;
                int posUltimoElementoAssociadoaRegion = -1;
                for (int i = indexRegiaoMenorQue30 +1; i < lista.size(); i++) {
                    if (("SubRegion".equals(nomeSimplesUltimoElemento(lista, i))) || ("RestrictedRegion".equals(nomeSimplesUltimoElemento(lista, i)))) {
                        double distancia = lista.get(i).calculateDistance(lista.get(i).getLatitude(), lista.get(i).getLongitude(), newlatitude, newlongitude);
                        Log.d("Consulta Lista", "Distância do elemento após região " + indexRegiaoMenorQue30 + " : " + distancia + " metros.");
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
                    Log.d("Consulta Lista", "Nova região não pode ser inserida (Distância menor que 5 metros detectada)");
                } else if ((posUltimoElementoAssociadoaRegion != -1) && (!avalia)) {
                    Log.d("Consulta Lista", "Encontrou uma Region após indexRegiaoMenorQue30 e nenhum elemento SubRegion ou RestrictedRegion associado a indexRegiaoMenorQue30 está a menos de 5 metros de distância da nova região 1");
                    verificaTipo(lista, posUltimoElementoAssociadoaRegion);
                } else if ((posUltimoElementoAssociadoaRegion == -1) && (!avalia)) {
                    Log.d("Consulta Lista", "Não encontrou uma Region após indexRegiaoMenorQue30 e nenhum elemento SubRegion ou RestrictedRegion associado a indexRegiaoMenorQue30 está a menos de 5 metros de distância da nova região 2");
                    verificaTipo(lista, lista.size() - 1);
                }
            }
        } else {
            Log.d("Consulta Lista", "Nenhuma região da lista está a menos de 30 metros de distância do novo dado");
            Region newRegion = new Region(newName, newlatitude, newlongitude, System.nanoTime(), Math.abs(random.nextInt()));
            lista.add(newRegion);
        }
    }

    private void verificaTipo(List<Region> lista, int index) {
        if ("SubRegion".equals(nomeSimplesUltimoElemento(lista, index))) {
            Log.d("Consulta Lista", "Adicionando RestrictedRegion");
            SubRegion subregion = (SubRegion)lista.get(index);
            Region mainRegion = subregion.getMainRegion();
            RestrictedRegion restrictedRegion = new RestrictedRegion(newName, newlatitude, newlongitude, Math.abs(random.nextInt()), System.nanoTime(), true, mainRegion);
            lista.add(restrictedRegion);
        } else {
            Log.d("Consulta Lista", "Adicionando SubRegion");
            RestrictedRegion restrictedRegion = (RestrictedRegion) lista.get(index);
            Region mainRegion = restrictedRegion.getMainRegion();
            SubRegion subRegion = new SubRegion(newName, newlatitude, newlongitude, Math.abs(random.nextInt()), System.nanoTime(), mainRegion);
            lista.add(subRegion);
        }
    }

    public static String nomeSimplesUltimoElemento(List<?> lista, int index) {
        if (lista == null || lista.isEmpty()) {
            return null;
        } else {
            Object ultimoElemento = lista.get(index);
            return ultimoElemento.getClass().getSimpleName();
        }
    }


}