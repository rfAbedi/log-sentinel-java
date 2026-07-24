package com.logsentinel.ingester;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.ZoneId;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IngesterSettingsTest {

    @Test
    void usesLocalDefaults() {
        IngesterSettings settings = IngesterSettings.from(Map.of());

        assertEquals(Path.of("logs"), settings.logDirectory());
        assertEquals("localhost:9092", settings.kafkaBootstrapServers());
        assertEquals("log-events", settings.kafkaTopic());
        assertEquals(ZoneId.of("UTC"), settings.sourceTimeZone());
    }

    @Test
    void readsEnvironmentOverrides() {
        IngesterSettings settings = IngesterSettings.from(Map.of(
                "LOG_DIRECTORY", "/var/log/log-sentinel",
                "KAFKA_BOOTSTRAP_SERVERS", "kafka:29092",
                "KAFKA_TOPIC", "application-logs",
                "SOURCE_TIME_ZONE", "Europe/Zurich"));

        assertEquals(Path.of("/var/log/log-sentinel"), settings.logDirectory());
        assertEquals("kafka:29092", settings.kafkaBootstrapServers());
        assertEquals("application-logs", settings.kafkaTopic());
        assertEquals(ZoneId.of("Europe/Zurich"), settings.sourceTimeZone());
    }
}
