package com.echo.waste.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HealthControllerTest {

    private DataSource createHealthyDataSource() {
        Connection connection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("isValid".equals(method.getName())) return true;
                    if ("close".equals(method.getName())) return null;
                    return null;
                });

        return (DataSource) Proxy.newProxyInstance(
                DataSource.class.getClassLoader(),
                new Class<?>[]{DataSource.class},
                (proxy, method, args) -> {
                    if ("getConnection".equals(method.getName())) return connection;
                    return null;
                });
    }

    private DataSource createFailingDataSource() {
        return (DataSource) Proxy.newProxyInstance(
                DataSource.class.getClassLoader(),
                new Class<?>[]{DataSource.class},
                (proxy, method, args) -> {
                    if ("getConnection".equals(method.getName())) {
                        throw new SQLException("Connection refused");
                    }
                    return null;
                });
    }

    @Test
    @DisplayName("Should return 200 OK and database UP when database connection is valid")
    void testHealthHealthy() {
        HealthController controller = new HealthController(createHealthyDataSource());
        ResponseEntity<Map<String, Object>> response = controller.health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "ok");
        assertThat(response.getBody()).containsEntry("application", "echo-waste");
        assertThat(response.getBody()).containsEntry("database", "UP");
    }

    @Test
    @DisplayName("Should return 503 SERVICE_UNAVAILABLE when database connection fails")
    void testHealthDegradedWhenDatabaseDown() {
        HealthController controller = new HealthController(createFailingDataSource());
        ResponseEntity<Map<String, Object>> response = controller.health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).containsEntry("status", "degraded");
        assertThat(response.getBody()).containsEntry("database", "DOWN");
    }
}
