package com.example.ticktracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sighting", indexes = {
        @Index(name = "idx_sighting_date", columnList = "sighting_date"),
        @Index(name = "idx_sighting_location", columnList = "location")
})
public class Sighting {

    @Id
    @Column(length = 40)
    private String id;                 // Elanco's id, or a generated UUID for user reports

    @Column(name = "sighting_date", nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private String location;           // city name

    @Column(nullable = false)
    private String species;

    private String latinName;
    private String region;
    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Source source;             // API or USER

    private String imagePath;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Source { API, USER }

    protected Sighting() { }           // JPA needs a no-arg constructor

    public Sighting(String id, LocalDateTime date, String location, String species,
                    String latinName, String region, Double latitude, Double longitude, Source source) {
        this.id = id;
        this.date = date;
        this.location = location;
        this.species = species;
        this.latinName = latinName;
        this.region = region;
        this.latitude = latitude;
        this.longitude = longitude;
        this.source = source;
    }

    public String getId() { return id; }
    public LocalDateTime getDate() { return date; }
    public String getLocation() { return location; }
    public String getSpecies() { return species; }
    public String getLatinName() { return latinName; }
    public String getRegion() { return region; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Source getSource() { return source; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}