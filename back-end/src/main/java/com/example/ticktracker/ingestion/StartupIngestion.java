package com.example.ticktracker.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.ticktracker.repository.SightingRepository;

@Component
@Order(2)
public class StartupIngestion implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupIngestion.class);

    private final SightingRepository sightings;
    private final IngestionService ingestion;

    public StartupIngestion(SightingRepository sightings, IngestionService ingestion) {
        this.sightings = sightings;
        this.ingestion = ingestion;
    }

    @Override
    public void run(String... args) {
        if (sightings.count() > 0) {
            log.info("Database already has {} sightings, skipping startup ingestion", sightings.count());
            return;
        }
        try {
            ingestion.refresh();
        } catch (Exception e) {
            log.error("Startup ingestion failed, the app will start with an empty database", e);
        }
    }
}