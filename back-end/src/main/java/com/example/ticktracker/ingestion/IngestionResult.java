package com.example.ticktracker.ingestion;

public record IngestionResult(String dataSource, int received, int saved,
        int duplicatesSkipped, int rejected, String message) {
}