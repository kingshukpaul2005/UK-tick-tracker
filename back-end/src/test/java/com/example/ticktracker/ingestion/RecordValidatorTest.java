package com.example.ticktracker.ingestion;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import com.example.ticktracker.model.CityLookup;

class RecordValidatorTest {

    private final RecordValidator validator = new RecordValidator();
    private final Map<String, CityLookup> cities = Map.of("London", mock(CityLookup.class));

    private ElancoRecord record(String id, String date, String city, String species) {
        return new ElancoRecord(id, date, city, species, "Ixodes apronophorus");
    }

    @Test
    void acceptsValidRecord() {
        assertTrue(validator.validate(record("abc", "2022-08-01T06:40:31", "London", "Marsh tick"), cities).isEmpty());
    }

    @Test
    void rejectsNullRecord() {
        assertEquals(Optional.of("record is null"), validator.validate(null, cities));
    }

    @Test
    void rejectsMissingId() {
        assertEquals(Optional.of("missing id"),
                validator.validate(record(" ", "2022-08-01T06:40:31", "London", "Marsh tick"), cities));
    }

    @Test
    void rejectsMissingSpecies() {
        assertEquals(Optional.of("missing species"),
                validator.validate(record("abc", "2022-08-01T06:40:31", "London", null), cities));
    }

    @Test
    void rejectsInvalidDate() {
        Optional<String> result = validator.validate(record("abc", "not-a-date", "London", "Marsh tick"), cities);
        assertTrue(result.isPresent() && result.get().startsWith("invalid date"));
    }

    @Test
    void rejectsFutureDate() {
        String future = LocalDateTime.now().plusDays(2).toString();
        assertEquals(Optional.of("date is in the future"),
                validator.validate(record("abc", future, "London", "Marsh tick"), cities));
    }

    @Test
    void rejectsUnknownCity() {
        Optional<String> result = validator.validate(record("abc", "2022-08-01T06:40:31", "Atlantis", "Marsh tick"),
                cities);
        assertTrue(result.isPresent() && result.get().startsWith("unknown city"));
    }
}