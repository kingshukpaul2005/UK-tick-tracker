package com.example.ticktracker.ingestion;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.ticktracker.model.CityLookup;

@Component
public class RecordValidator {

    /** Returns the reason a record is invalid, or empty if it is fine. */
    public Optional<String> validate(ElancoRecord r, Map<String, CityLookup> cities) {
        if (r == null) return Optional.of("record is null");
        if (isBlank(r.id())) return Optional.of("missing id");
        if (r.id().trim().length() > 40) return Optional.of("id too long");
        if (isBlank(r.date())) return Optional.of("missing date");
        if (isBlank(r.location())) return Optional.of("missing location");
        if (isBlank(r.species())) return Optional.of("missing species");

        LocalDateTime parsed;
        try {
            parsed = parseDate(r.date());
        } catch (DateTimeParseException e) {
            return Optional.of("invalid date '" + r.date() + "'");
        }
        if (parsed.isAfter(LocalDateTime.now())) return Optional.of("date is in the future");
        if (!cities.containsKey(r.location().trim())) {
            return Optional.of("unknown city '" + r.location() + "'");
        }
        return Optional.empty();
    }

    public LocalDateTime parseDate(String value) {
        return LocalDateTime.parse(value.trim());
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}