package com.example.ticktracker.sighting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.ticktracker.exception.BadRequestException;
import com.example.ticktracker.model.Sighting;
import com.example.ticktracker.repository.SightingRepository;

@Service
public class SightingService {

    private static final int MAX_PAGE_SIZE = 1000;

    private final SightingRepository repo;

    public SightingService(SightingRepository repo) {
        this.repo = repo;
    }

    public PageResponse<SightingDto> search(LocalDate from, LocalDate to, String city,
                                            String region, String species, int page, int size) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("'from' must not be after 'to'");
        }
        if (page < 0) {
            throw new BadRequestException("page must be 0 or more");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("size must be between 1 and " + MAX_PAGE_SIZE);
        }

        LocalDateTime fromDate = from == null ? null : from.atStartOfDay();
        LocalDateTime toDate = to == null ? null : to.atTime(LocalTime.MAX);   // include the whole last day
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));

        Page<Sighting> result = repo.search(fromDate, toDate, clean(city), clean(region), clean(species), pageable);

        return new PageResponse<>(
                result.getContent().stream().map(SightingDto::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    private String clean(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}