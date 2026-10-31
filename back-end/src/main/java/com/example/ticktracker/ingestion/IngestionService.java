package com.example.ticktracker.ingestion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.ticktracker.model.CityLookup;
import com.example.ticktracker.model.Sighting;
import com.example.ticktracker.repository.CityLookupRepository;
import com.example.ticktracker.repository.SightingRepository;

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final ElancoClient client;
    private final SeedFileLoader seedLoader;
    private final RecordValidator validator;
    private final SightingRepository sightings;
    private final CityLookupRepository cityRepo;

    public IngestionService(ElancoClient client, SeedFileLoader seedLoader, RecordValidator validator,
            SightingRepository sightings, CityLookupRepository cityRepo) {
        this.client = client;
        this.seedLoader = seedLoader;
        this.validator = validator;
        this.sightings = sightings;
        this.cityRepo = cityRepo;
    }

    public IngestionResult refresh() {
        List<ElancoRecord> records;
        String dataSource;
        String message;
        try {
            records = client.fetchAll();
            dataSource = "ELANCO_API";
            message = "Fetched from the Elanco API";
        } catch (ElancoApiException e) {
            log.error("Elanco API failed, using local seed file: {}", e.getMessage());
            records = seedLoader.load();
            dataSource = "SEED_FILE";
            message = e.getMessage() + ". Loaded the local seed file instead.";
        }
        return store(records, dataSource, message);
    }

    private IngestionResult store(List<ElancoRecord> records, String dataSource, String message) {
        Map<String, CityLookup> cities = cityRepo.findAll().stream()
                .collect(Collectors.toMap(CityLookup::getCity, Function.identity()));
        Set<String> knownIds = new HashSet<>(sightings.findAllIds());

        List<Sighting> toSave = new ArrayList<>();
        int duplicates = 0;
        int rejected = 0;
        Set<String> seenKeys = new HashSet<>();
        int suspected = 0;

        for (ElancoRecord r : records) {
            Optional<String> problem = validator.validate(r, cities);
            if (problem.isPresent()) {
                rejected++;
                log.warn("Rejected record id={} reason={}", r == null ? null : r.id(), problem.get());
                continue;
            }
            String id = r.id().trim();
            if (!knownIds.add(id)) { // add() returns false if the id was already there
                duplicates++;
                continue;
            }
            String key = validator.parseDate(r.date()).toLocalDate() + "|" + r.location().trim() + "|"
                    + r.species().trim();
            if (!seenKeys.add(key)) {
                suspected++;
                log.warn("Suspected duplicate (same day, city and species) kept: id={}", id);
            }
            CityLookup city = cities.get(r.location().trim());
            toSave.add(new Sighting(id, validator.parseDate(r.date()), city.getCity(),
                    r.species().trim(), blankToNull(r.latinName()), city.getRegion(),
                    city.getLat(), city.getLng(), Sighting.Source.API));
        }
        sightings.saveAll(toSave);

        IngestionResult result = new IngestionResult(dataSource, records.size(), toSave.size(),
                duplicates, rejected, message);
        log.info("Ingestion finished: {} (suspected duplicates: {})", result, suspected);
        return result;
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}