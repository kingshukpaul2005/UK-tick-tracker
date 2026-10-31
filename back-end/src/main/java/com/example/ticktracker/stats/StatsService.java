package com.example.ticktracker.stats;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.example.ticktracker.exception.BadRequestException;
import com.example.ticktracker.repository.SightingRepository;

@Service
public class StatsService {

    private final SightingRepository repo;

    public StatsService(SightingRepository repo) {
        this.repo = repo;
    }

    public List<RegionCount> byRegion(LocalDate from, LocalDate to, String species) {
        checkRange(from, to);
        return repo.countByRegion(start(from), end(to), clean(species));
    }

    public List<TrendPoint> trends(String granularity, LocalDate from, LocalDate to,
            String city, String region, String species) {
        String g = granularity == null || granularity.isBlank() ? "month" : granularity.trim().toLowerCase();
        if (!g.equals("week") && !g.equals("month")) {
            throw new BadRequestException("granularity must be 'week' or 'month'");
        }
        checkRange(from, to);

        List<LocalDateTime> dates = repo.findDates(start(from), end(to), clean(city), clean(region), clean(species));

        Map<String, Long> buckets = new TreeMap<>(); // TreeMap keeps keys sorted
        for (LocalDateTime d : dates) {
            String key = g.equals("week")
                    ? d.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()
                    : YearMonth.from(d).toString();
            buckets.merge(key, 1L, Long::sum);
        }

        List<TrendPoint> points = new ArrayList<>();
        buckets.forEach((period, count) -> points.add(new TrendPoint(period, count)));
        return points;
    }

    public List<TrendPoint> seasonal(String city, String species) {
        List<LocalDateTime> dates = repo.findDates(null, null, clean(city), null, clean(species));
        long[] counts = new long[12];
        for (LocalDateTime d : dates) {
            counts[d.getMonthValue() - 1]++;
        }
        List<TrendPoint> months = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            months.add(new TrendPoint(Month.of(i + 1).getDisplayName(TextStyle.SHORT, Locale.UK), counts[i]));
        }
        return months;
    }

    private void checkRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("'from' must not be after 'to'");
        }
    }

    private LocalDateTime start(LocalDate d) {
        return d == null ? null : d.atStartOfDay();
    }

    private LocalDateTime end(LocalDate d) {
        return d == null ? null : d.atTime(LocalTime.MAX);
    }

    private String clean(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}