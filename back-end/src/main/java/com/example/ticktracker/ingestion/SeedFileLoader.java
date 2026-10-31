package com.example.ticktracker.ingestion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.util.List;

@Component
public class SeedFileLoader {

    private final JsonMapper mapper = JsonMapper.builder().build();
    private final String path;

    public SeedFileLoader(@Value("${elanco.seed-file}") String path) {
        this.path = path;
    }

    public List<ElancoRecord> load() {
        try {
            return mapper.readValue(new File(path), new TypeReference<List<ElancoRecord>>() { });
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot read seed file: " + path, e);
        }
    }
}