package com.example.ticktracker.sighting;

import java.time.LocalDateTime;

import com.example.ticktracker.model.Sighting;

public record SightingDto(String id, LocalDateTime date, String location, String species,
        String latinName, String region, Double latitude, Double longitude,
        String source, String imageUrl) {

    public static SightingDto from(Sighting s) {
        String imageUrl = s.getImagePath() == null ? null : "/uploads/" + s.getImagePath();
        return new SightingDto(s.getId(), s.getDate(), s.getLocation(), s.getSpecies(),
                s.getLatinName(), s.getRegion(), s.getLatitude(), s.getLongitude(),
                s.getSource().name(), imageUrl);
    }
}