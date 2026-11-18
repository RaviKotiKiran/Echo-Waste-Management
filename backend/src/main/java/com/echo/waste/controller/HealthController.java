package com.echo.waste.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("status", "ok");
        details.put("application", "echo-waste");

        boolean databaseAvailable;
        try (Connection connection = dataSource.getConnection()) {
            databaseAvailable = connection.isValid(2);
        } catch (SQLException exception) {
            log.warn("Database health probe failed", exception);
            databaseAvailable = false;
        }

        details.put("database", databaseAvailable ? "UP" : "DOWN");

        if (!databaseAvailable) {
            details.put("status", "degraded");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(details);
        }

        return ResponseEntity.ok(details);
    }
}
