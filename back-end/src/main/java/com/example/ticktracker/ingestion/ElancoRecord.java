package com.example.ticktracker.ingestion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ElancoRecord(String id, String date, String location, String species, String latinName) {
}