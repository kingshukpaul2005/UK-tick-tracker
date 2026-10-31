package com.example.ticktracker.repository;

import com.example.ticktracker.model.Sighting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SightingRepository extends JpaRepository<Sighting, String> {
}