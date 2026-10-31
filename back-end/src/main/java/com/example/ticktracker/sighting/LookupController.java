package com.example.ticktracker.sighting;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticktracker.model.CityLookup;
import com.example.ticktracker.repository.CityLookupRepository;
import com.example.ticktracker.repository.SightingRepository;

@RestController
@RequestMapping("/api")
public class LookupController {

    private final CityLookupRepository cities;
    private final SightingRepository sightings;

    public LookupController(CityLookupRepository cities, SightingRepository sightings) {
        this.cities = cities;
        this.sightings = sightings;
    }

    @GetMapping("/cities")
    public List<CityLookup> cities() {
        return cities.findAll();
    }

    @GetMapping("/species")
    public List<String> species() {
        return sightings.findDistinctSpecies();
    }
}