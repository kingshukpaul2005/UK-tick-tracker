package com.example.ticktracker.stats;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService service;

    public StatsController(StatsService service) {
        this.service = service;
    }

    @GetMapping("/by-region")
    public List<RegionCount> byRegion(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String species) {
        return service.byRegion(from, to, species);
    }

    @GetMapping("/trends")
    public List<TrendPoint> trends(
            @RequestParam(defaultValue = "month") String granularity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String species) {
        return service.trends(granularity, from, to, city, region, species);
    }

    @GetMapping("/seasonal")
    public List<TrendPoint> seasonal(@RequestParam(required = false) String city,
            @RequestParam(required = false) String species) {
        return service.seasonal(city, species);
    }
}