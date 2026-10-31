package com.example.ticktracker.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ticktracker.model.Sighting;
import com.example.ticktracker.stats.RegionCount;

public interface SightingRepository extends JpaRepository<Sighting, String> {
    Optional<Sighting> findFirstBySpecies(String species);

    @Query("select s.id from Sighting s")
    List<String> findAllIds();

    @Query("""
            select s from Sighting s
            where (:fromDate is null or s.date >= :fromDate)
              and (:toDate is null or s.date <= :toDate)
              and (:city is null or s.location = :city)
              and (:region is null or s.region = :region)
              and (:species is null or s.species = :species)
            """)
    Page<Sighting> search(@Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("city") String city,
            @Param("region") String region,
            @Param("species") String species,
            Pageable pageable);

    @Query("select distinct s.species from Sighting s order by s.species")
    List<String> findDistinctSpecies();

    @Query("""
            select new com.example.ticktracker.stats.RegionCount(s.region, count(s))
            from Sighting s
            where (:fromDate is null or s.date >= :fromDate)
              and (:toDate is null or s.date <= :toDate)
              and (:species is null or s.species = :species)
            group by s.region
            order by count(s) desc
            """)
    List<RegionCount> countByRegion(@Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("species") String species);

    @Query("""
            select s.date from Sighting s
            where (:fromDate is null or s.date >= :fromDate)
              and (:toDate is null or s.date <= :toDate)
              and (:city is null or s.location = :city)
              and (:region is null or s.region = :region)
              and (:species is null or s.species = :species)
            """)
    List<LocalDateTime> findDates(@Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("city") String city,
            @Param("region") String region,
            @Param("species") String species);
}