package com.logsentinel.ingester.mapping;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.contracts.LogLevel;
import com.logsentinel.ingester.parser.ParsedLogLine;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class LogEventMapperTest {

    private static final ParsedLogLine PARSED_LINE = new ParsedLogLine(
            LocalDateTime.of(2025, 7, 1, 12, 56, 7, 451_000_000),
            "http-worker-1",
            "ERROR",
            "com.example.GatewayClient",
            "Payment gateway timeout");

    @Test
    void mapsParsedLineAndSourceMetadataToLogEvent() {
        LogEvent event = new LogEventMapper(ZoneOffset.UTC).map(
                PARSED_LINE,
                Path.of("/var/log/payment-service_2025-07-01_12-55-55.log"),
                3);

        UUID.fromString(event.eventId());
        assertEquals("payment-service", event.component());
        assertEquals(Instant.parse("2025-07-01T12:56:07.451Z"), event.eventTime());
        assertEquals(LogLevel.ERROR, event.level());
        assertEquals("http-worker-1", event.thread());
        assertEquals("com.example.GatewayClient", event.logger());
        assertEquals("Payment gateway timeout", event.message());
        assertEquals("payment-service_2025-07-01_12-55-55.log", event.sourceFile());
        assertEquals(3L, event.lineNumber());
    }

    @Test
    void appliesConfiguredSourceTimezone() {
        LogEvent event = new LogEventMapper(ZoneOffset.ofHoursMinutes(3, 30)).map(
                PARSED_LINE,
                Path.of("payment-service_2025-07-01_12-55-55.log"),
                1);

        assertEquals(Instant.parse("2025-07-01T09:26:07.451Z"), event.eventTime());
    }

    @Test
    void generatesTheSameEventIdWhenTheSameLineIsReprocessed() {
        LogEventMapper mapper = new LogEventMapper(ZoneOffset.UTC);
        Path sourceFile = Path.of("payment-service_2025-07-01_12-55-55.log");

        LogEvent first = mapper.map(PARSED_LINE, sourceFile, 1);
        LogEvent second = mapper.map(PARSED_LINE, sourceFile, 1);

        assertEquals(first.eventId(), second.eventId());
    }

    @Test
    void generatesDifferentEventIdsForDifferentSourceLines() {
        LogEventMapper mapper = new LogEventMapper(ZoneOffset.UTC);
        Path sourceFile = Path.of("payment-service_2025-07-01_12-55-55.log");

        LogEvent first = mapper.map(PARSED_LINE, sourceFile, 1);
        LogEvent second = mapper.map(PARSED_LINE, sourceFile, 2);

        assertNotEquals(first.eventId(), second.eventId());
    }
}
