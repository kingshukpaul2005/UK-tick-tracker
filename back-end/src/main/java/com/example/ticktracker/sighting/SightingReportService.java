package com.example.ticktracker.sighting;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.ticktracker.exception.InvalidSightingException;
import com.example.ticktracker.model.CityLookup;
import com.example.ticktracker.model.Sighting;
import com.example.ticktracker.repository.CityLookupRepository;
import com.example.ticktracker.repository.SightingRepository;

@Service
public class SightingReportService {

    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    private final SightingRepository sightings;
    private final CityLookupRepository cities;
    private final Path uploadDir;

    public SightingReportService(SightingRepository sightings, CityLookupRepository cities,
                                 @Value("${app.upload-dir}") String uploadDir) {
        this.sightings = sightings;
        this.cities = cities;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath();
    }

    public SightingDto create(String date, String time, String city, String species, MultipartFile image) {
        Map<String, String> errors = new LinkedHashMap<>();

        CityLookup cityRow = null;
        if (isBlank(city)) {
            errors.put("city", "City is required");
        } else {
            cityRow = cities.findById(city.trim()).orElse(null);
            if (cityRow == null) errors.put("city", "Unknown city: " + city.trim());
        }

        Sighting speciesExample = null;
        if (isBlank(species)) {
            errors.put("species", "Species is required");
        } else {
            speciesExample = sightings.findFirstBySpecies(species.trim()).orElse(null);
            if (speciesExample == null) errors.put("species", "Unknown species: " + species.trim());
        }

        LocalDateTime when = parseWhen(date, time, errors);

        byte[] imageBytes = null;
        String extension = null;
        if (image != null && !image.isEmpty()) {
            if (image.getSize() > MAX_IMAGE_BYTES) {
                errors.put("image", "Image must be 5 MB or smaller");
            } else {
                try {
                    imageBytes = image.getBytes();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                extension = detectExtension(imageBytes);
                if (extension == null) errors.put("image", "Image must be a JPEG, PNG or WebP file");
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidSightingException(errors);
        }

        String imageName = null;
        if (imageBytes != null) {
            imageName = UUID.randomUUID() + "." + extension;     // our own name, never the user's filename
            try {
                Files.createDirectories(uploadDir);
                Files.write(uploadDir.resolve(imageName), imageBytes);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        Sighting sighting = new Sighting(UUID.randomUUID().toString(), when, cityRow.getCity(),
                speciesExample.getSpecies(), speciesExample.getLatinName(), cityRow.getRegion(),
                cityRow.getLat(), cityRow.getLng(), Sighting.Source.USER);
        if (imageName != null) {
            sighting.setImagePath(imageName);
        }
        return SightingDto.from(sightings.save(sighting));
    }

    private LocalDateTime parseWhen(String date, String time, Map<String, String> errors) {
        if (isBlank(date)) {
            errors.put("date", "Date is required");
            return null;
        }
        LocalDate d;
        try {
            d = LocalDate.parse(date.trim());
        } catch (DateTimeParseException e) {
            errors.put("date", "Date must be in the format yyyy-MM-dd");
            return null;
        }
        LocalTime t = LocalTime.MIDNIGHT;                         // time is optional
        if (!isBlank(time)) {
            try {
                t = LocalTime.parse(time.trim());
            } catch (DateTimeParseException e) {
                errors.put("time", "Time must be in the format HH:mm");
                return null;
            }
        }
        LocalDateTime when = LocalDateTime.of(d, t);
        if (when.isAfter(LocalDateTime.now())) {
            errors.put("date", "A sighting cannot be in the future");
            return null;
        }
        return when;
    }

    /** Checks the file's first bytes (magic numbers), because the filename and content type can be faked. */
    private String detectExtension(byte[] b) {
        if (b.length > 12) {
            if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "jpg";
            if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return "png";
            if (b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "webp";
        }
        return null;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}