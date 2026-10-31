package com.example.ticktracker.model;

import jakarta.persistence.*;

@Entity
@Table(name = "city_lookup")
public class CityLookup {

    @Id
    private String city;
    private String region;
    private double lat;
    private double lng;

    protected CityLookup() { }

    public CityLookup(String city, String region, double lat, double lng) {
        this.city = city;
        this.region = region;
        this.lat = lat;
        this.lng = lng;
    }

    public String getCity() { return city; }
    public String getRegion() { return region; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
}