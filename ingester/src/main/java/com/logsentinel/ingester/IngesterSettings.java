package com.logsentinel.ingester;

import java.nio.file.Path;
import java.time.ZoneId;
import java.util.Map;

public record IngesterSettings(
        Path logDirectory,
        String kafkaBootstrapServers,
        String kafkaTopic,
        ZoneId sourceTimeZone) {

    public static IngesterSettings from(Map<String, String> environment) {
        return new IngesterSettings(
                Path.of(environment.getOrDefault("LOG_DIRECTORY", "logs")),
                environment.getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
                environment.getOrDefault("KAFKA_TOPIC", "log-events"),
                ZoneId.of(environment.getOrDefault("SOURCE_TIME_ZONE", "UTC")));
    }
}
