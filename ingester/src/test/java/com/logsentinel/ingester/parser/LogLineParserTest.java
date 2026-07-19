package com.logsentinel.ingester.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogLineParserTest {

    private final LogLineParser parser = new LogLineParser();

    @Test
    void parsesValidInfoLine() {
        ParsedLogLine parsed = parser.parse(
                "2025-07-01 12:55:55,114 [main] INFO com.example.PaymentApplication - Application started");

        assertEquals(LocalDateTime.of(2025, 7, 1, 12, 55, 55, 114_000_000), parsed.timestamp());
        assertEquals("main", parsed.threadName());
        assertEquals("INFO", parsed.level());
        assertEquals("com.example.PaymentApplication", parsed.loggerName());
        assertEquals("Application started", parsed.message());
    }

    @Test
    void parsesValidErrorLine() {
        ParsedLogLine parsed = parser.parse(
                "2025-07-01 12:56:07,451 [http-worker-1] ERROR com.example.GatewayClient – Payment gateway timeout");

        assertEquals(LocalDateTime.of(2025, 7, 1, 12, 56, 7, 451_000_000), parsed.timestamp());
        assertEquals("http-worker-1", parsed.threadName());
        assertEquals("ERROR", parsed.level());
        assertEquals("com.example.GatewayClient", parsed.loggerName());
        assertEquals("Payment gateway timeout", parsed.message());
    }

    @Test
    void preservesCompleteMessageWithSpacesAndPunctuation() {
        ParsedLogLine parsed = parser.parse(
                "2025-07-01 12:56:08,008 [worker] WARN com.example.Service — Request failed: code=504, retry in 10 seconds!");

        assertEquals("Request failed: code=504, retry in 10 seconds!", parsed.message());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-", "–", "—"})
    void acceptsSupportedMessageSeparators(String separator) {
        ParsedLogLine parsed = parser.parse(
                "2025-07-01 12:55:55,114 [main] DEBUG com.example.Service " + separator + " Debug details");

        assertEquals("Debug details", parsed.message());
    }

    @ParameterizedTest
    @ValueSource(strings = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR"})
    void acceptsEverySupportedLogLevel(String level) {
        ParsedLogLine parsed = parser.parse(
                "2025-07-01 12:55:55,114 [main] " + level + " com.example.Service - Message");

        assertEquals(level, parsed.level());
    }

    @Test
    void rejectsUnsupportedLogLevel() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(
                "2025-07-01 12:55:55,114 [main] FATAL com.example.Service - Failed"));

        assertTrue(exception.getMessage().contains("Unsupported log level"));
    }

    @Test
    void rejectsMalformedTimestamp() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(
                "2025-02-30 12:55:55,114 [main] INFO com.example.Service - Started"));

        assertTrue(exception.getMessage().contains("Malformed log timestamp"));
    }

    @Test
    void rejectsMissingThreadSection() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(
                "2025-07-01 12:55:55,114 INFO com.example.Service - Started"));
    }

    @Test
    void rejectsMissingMessageSeparator() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(
                "2025-07-01 12:55:55,114 [main] INFO com.example.Service Started"));
    }
}
