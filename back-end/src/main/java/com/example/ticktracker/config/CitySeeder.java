package com.example.ticktracker.config;

import com.example.ticktracker.model.CityLookup;
import com.example.ticktracker.repository.CityLookupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CitySeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CitySeeder.class);
    private final CityLookupRepository repo;

    public CitySeeder(CityLookupRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) {
            log.info("City lookup already seeded ({} cities)", repo.count());
            return;
        }
        repo.saveAll(List.of(
                new CityLookup("London", "London", 51.5074, -0.1278),
                new CityLookup("Manchester", "North West", 53.4808, -2.2426),
                new CityLookup("Liverpool", "North West", 53.4084, -2.9916),
                new CityLookup("Leeds", "Yorkshire and the Humber", 53.8008, -1.5491),
                new CityLookup("Sheffield", "Yorkshire and the Humber", 53.3811, -1.4701),
                new CityLookup("Newcastle", "North East", 54.9783, -1.6178),
                new CityLookup("Nottingham", "East Midlands", 52.9548, -1.1581),
                new CityLookup("Leicester", "East Midlands", 52.6369, -1.1398),
                new CityLookup("Birmingham", "West Midlands", 52.4862, -1.8904),
                new CityLookup("Bristol", "South West", 51.4545, -2.5879),
                new CityLookup("Southampton", "South East", 50.9097, -1.4044),
                new CityLookup("Cardiff", "Wales", 51.4816, -3.1791),
                new CityLookup("Glasgow", "Scotland", 55.8642, -4.2518),
                new CityLookup("Edinburgh", "Scotland", 55.9533, -3.1883)
        ));
        log.info("Seeded {} cities", repo.count());
    }
}