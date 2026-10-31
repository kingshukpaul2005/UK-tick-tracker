package com.example.ticktracker.sighting;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/sightings")
public class SightingReportController {

    private final SightingReportService service;

    public SightingReportController(SightingReportService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SightingDto create(@RequestParam(required = false) String date,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) MultipartFile image) {
        return service.create(date, time, city, species, image);
    }
}