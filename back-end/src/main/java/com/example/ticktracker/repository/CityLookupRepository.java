package com.example.ticktracker.repository;

import com.example.ticktracker.model.CityLookup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityLookupRepository extends JpaRepository<CityLookup, String> {
}