package com.example.ticktracker.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ticktracker.model.Sighting;

public interface SightingRepository extends JpaRepository<Sighting, String> {

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
}