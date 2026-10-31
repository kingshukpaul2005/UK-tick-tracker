package com.example.ticktracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ticktracker.model.Sighting;

public interface SightingRepository extends JpaRepository<Sighting, String> {

    @Query("select s.id from Sighting s")
    List<String> findAllIds();
}