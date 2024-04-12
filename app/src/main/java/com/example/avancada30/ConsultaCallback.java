package com.example.avancada30;

import java.util.List;

public interface ConsultaCallback {
    void onRegionsLoaded(List<Region> regions);
    void onCancelled();
}

