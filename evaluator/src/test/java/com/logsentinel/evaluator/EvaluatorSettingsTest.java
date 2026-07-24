package com.logsentinel.evaluator;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EvaluatorSettingsTest {

    @Test
    void usesLocalDefaults() {
        EvaluatorSettings settings = EvaluatorSettings.from(Map.of());

        assertEquals("localhost:9092", settings.kafkaBootstrapServers());
        assertEquals("log-events", settings.kafkaTopic());
        assertEquals("log-sentinel-evaluator", settings.kafkaGroupId());
        assertEquals(Path.of("config/rules.yaml"), settings.rulesFile());
        assertEquals(
                "jdbc:postgresql://localhost:5432/log_sentinel",
                settings.databaseUrl());
        assertEquals("log_sentinel", settings.databaseUser());
        assertEquals("log_sentinel", settings.databasePassword());
        assertEquals(Duration.ofSeconds(1), settings.pollTimeout());
    }

    @Test
    void readsEnvironmentOverrides() {
        EvaluatorSettings settings = EvaluatorSettings.from(Map.of(
                "KAFKA_BOOTSTRAP_SERVERS", "kafka:29092",
                "KAFKA_TOPIC", "application-logs",
                "KAFKA_GROUP_ID", "application-log-evaluator",
                "RULES_FILE", "/etc/log-sentinel/rules.yaml",
                "LOG_SENTINEL_DB_URL", "jdbc:postgresql://postgres:5432/logs",
                "LOG_SENTINEL_DB_USER", "evaluator",
                "LOG_SENTINEL_DB_PASSWORD", "secret",
                "KAFKA_POLL_TIMEOUT_MS", "2500"));

        assertEquals("kafka:29092", settings.kafkaBootstrapServers());
        assertEquals("application-logs", settings.kafkaTopic());
        assertEquals("application-log-evaluator", settings.kafkaGroupId());
        assertEquals(Path.of("/etc/log-sentinel/rules.yaml"), settings.rulesFile());
        assertEquals("jdbc:postgresql://postgres:5432/logs", settings.databaseUrl());
        assertEquals("evaluator", settings.databaseUser());
        assertEquals("secret", settings.databasePassword());
        assertEquals(Duration.ofMillis(2500), settings.pollTimeout());
    }
}
