package com.example.avancada30;

import android.content.Context;
import android.util.Log;

import com.example.biblioteca.Region;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.concurrent.Semaphore;

public class FirebaseDataSaver extends Thread {

    private static final String TAG = "FirebaseDataSaver";
    private DatabaseReference referencia = FirebaseDatabase.getInstance().getReference();
    private Context context;
    private List<Region> regions;
    private Semaphore semaphore;
    private   int i = 0;
    private volatile boolean running = true; // Flag para controlar a execução do loop

    public FirebaseDataSaver(Context context, List<Region> regions, Semaphore semaphore) {
        this.context = context;
        this.regions = regions;
        this.semaphore = semaphore;
    }
    public FirebaseDataSaver(){}

    public Context getContexto() {
        return context;
    }
    public void setContexto(Context context) {
        this.context = context;
    }

    public List<Region> getRegion() {
        return regions;
    }
    public void setRegion(List<Region> regions) {
        this.regions = regions;
    }
    public Semaphore getsemaphore() {
        return semaphore;
    }
    public void setsemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        while (running) { // Executar o loop enquanto a flag running for true
            try {
                semaphore.acquire(); // Acquire semaphore before saving

                if (regions.isEmpty()) {
                    semaphore.release(); // Release semaphore after saving
                    synchronized (regions) {
                        Log.d(TAG, "Aguardando lista");
                        regions.wait(); // Aguardar até que a lista não esteja mais vazia
                    }
                }
                else {

                    saveData();

                    semaphore.release(); // Release semaphore after saving
                    Log.d(TAG, "Semafaro Liberado.");
                }

            } catch (InterruptedException e) {
                Log.e(TAG, "InterruptedException: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void stopThread() {
        running = false; // Método para parar o loop
    }

    private void saveData() {
        String encryptedJson;
        DatabaseReference regiao = referencia.child("regioes");


        for (Region region : regions) {
            encryptedJson = JsonConverter.objectToJsonEncrypted(region);
            regiao.child(String.valueOf(i)).setValue(encryptedJson);
            i++;
        }
        i=0;
        regions.clear(); // Clear list after successful saving
        Log.d(TAG, "Dados Salvos no Servidor!");
    }
}
