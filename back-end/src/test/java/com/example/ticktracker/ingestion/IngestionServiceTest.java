package com.example.ticktracker.ingestion;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.ticktracker.model.CityLookup;
import com.example.ticktracker.repository.CityLookupRepository;
import com.example.ticktracker.repository.SightingRepository;

class IngestionServiceTest {

    private ElancoClient client;
    private SeedFileLoader seedLoader;
    private IngestionService service;

    @BeforeEach
    void setUp() {
        client = mock(ElancoClient.class);
        seedLoader = mock(SeedFileLoader.class);
        SightingRepository sightings = mock(SightingRepository.class); // empty database
        CityLookupRepository cityRepo = mock(CityLookupRepository.class);

        CityLookup london = mock(CityLookup.class);
        when(london.getCity()).thenReturn("London");
        when(london.getRegion()).thenReturn("London");
        when(cityRepo.findAll()).thenReturn(List.of(london));

        service = new IngestionService(client, seedLoader, new RecordValidator(), sightings, cityRepo);
    }

    private ElancoRecord record(String id, String city) {
        return new ElancoRecord(id, "2022-08-01T06:40:31", city, "Marsh tick", "Ixodes apronophorus");
    }

    @Test
    void skipsDuplicateIds() {
        when(client.fetchAll()).thenReturn(List.of(record("a", "London"), record("a", "London")));

        IngestionResult result = service.refresh();

        assertEquals("ELANCO_API", result.dataSource());
        assertEquals(2, result.received());
        assertEquals(1, result.saved());
        assertEquals(1, result.duplicatesSkipped());
    }

    @Test
    void rejectsBadRecordsAndKeepsGoodOnes() {
        when(client.fetchAll())
                .thenReturn(List.of(record("a", "London"), record("b", "Atlantis"), record(null, "London")));

        IngestionResult result = service.refresh();

        assertEquals(1, result.saved());
        assertEquals(2, result.rejected());
    }

    @Test
    void fallsBackToSeedFileWhenApiFails() {
        when(client.fetchAll()).thenThrow(new ElancoApiException("Elanco API unavailable", null));
        when(seedLoader.load()).thenReturn(List.of(record("a", "London")));

        IngestionResult result = service.refresh();

        assertEquals("SEED_FILE", result.dataSource());
        assertEquals(1, result.saved());
    }
}