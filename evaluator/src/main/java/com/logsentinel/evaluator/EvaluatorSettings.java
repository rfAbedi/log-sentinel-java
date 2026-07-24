package com.logsentinel.evaluator;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

public record EvaluatorSettings(
        String kafkaBootstrapServers,
        String kafkaTopic,
        String kafkaGroupId,
        Path rulesFile,
        String databaseUrl,
        String databaseUser,
        String databasePassword,
        Duration pollTimeout) {

    public static EvaluatorSettings from(Map<String, String> environment) {
        return new EvaluatorSettings(
                environment.getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
                environment.getOrDefault("KAFKA_TOPIC", "log-events"),
                environment.getOrDefault("KAFKA_GROUP_ID", "log-sentinel-evaluator"),
                Path.of(environment.getOrDefault("RULES_FILE", "config/rules.yaml")),
                environment.getOrDefault(
                        "LOG_SENTINEL_DB_URL",
                        "jdbc:postgresql://localhost:5432/log_sentinel"),
                environment.getOrDefault("LOG_SENTINEL_DB_USER", "log_sentinel"),
                environment.getOrDefault("LOG_SENTINEL_DB_PASSWORD", "log_sentinel"),
                Duration.ofMillis(Long.parseLong(
                        environment.getOrDefault("KAFKA_POLL_TIMEOUT_MS", "1000"))));
    }
}
