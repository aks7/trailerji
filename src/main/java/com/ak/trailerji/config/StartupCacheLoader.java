package com.ak.trailerji.config;

import com.ak.trailerji.service.TrailerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupCacheLoader implements CommandLineRunner {
    private final TrailerService trailerService;

    public StartupCacheLoader(TrailerService trailerService) {
        this.trailerService = trailerService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Load cache on startup (fetch 100 trailers)
        trailerService.fetchAndStoreTrailers(100);
    }
}