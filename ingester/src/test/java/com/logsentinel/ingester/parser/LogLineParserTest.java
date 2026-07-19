package com.logsentinel.ingester.parser;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.contracts.LogLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogLineParserTest {

    private final LogLineParser parser = new LogLineParser();

    @Test
    void parsesValidInfoLine() {
        LogEvent parsed = parser.parse(
                "2025-07-01 12:55:55,114 [main] INFO com.example.PaymentApplication - Application started");

        assertEquals(Instant.parse("2025-07-01T12:55:55.114Z"), parsed.eventTime());
        assertEquals("main", parsed.thread());
        assertEquals(LogLevel.INFO, parsed.level());
        assertEquals("com.example.PaymentApplication", parsed.logger());
        assertEquals("Application started", parsed.message());
    }

    @Test
    void parsesValidErrorLine() {
        LogEvent parsed = parser.parse(
                "2025-07-01 12:56:07,451 [http-worker-1] ERROR com.example.GatewayClient – Payment gateway timeout");

        assertEquals(Instant.parse("2025-07-01T12:56:07.451Z"), parsed.eventTime());
        assertEquals("http-worker-1", parsed.thread());
        assertEquals(LogLevel.ERROR, parsed.level());
        assertEquals("com.example.GatewayClient", parsed.logger());
        assertEquals("Payment gateway timeout", parsed.message());
    }

    @Test
    void preservesCompleteMessageWithSpacesAndPunctuation() {
        LogEvent parsed = parser.parse(
                "2025-07-01 12:56:08,008 [worker] WARN com.example.Service — Request failed: code=504, retry in 10 seconds!");

        assertEquals("Request failed: code=504, retry in 10 seconds!", parsed.message());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-", "–", "—"})
    void acceptsSupportedMessageSeparators(String separator) {
        LogEvent parsed = parser.parse(
                "2025-07-01 12:55:55,114 [main] DEBUG com.example.Service " + separator + " Debug details");

        assertEquals("Debug details", parsed.message());
    }

    @ParameterizedTest
    @EnumSource(LogLevel.class)
    void acceptsEverySupportedLogLevel(LogLevel level) {
        LogEvent parsed = parser.parse(
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
