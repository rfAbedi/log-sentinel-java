package com.logsentinel.contracts.kafka;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.contracts.LogLevel;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogEventJsonCodecTest {

    @Test
    void roundTripsLogEvent() {
        LogEvent original = new LogEvent(
                "event-123",
                "payment-service",
                Instant.parse("2025-07-01T12:56:07.451Z"),
                LogLevel.ERROR,
                "http-worker-1",
                "com.example.GatewayClient",
                "Payment gateway timeout",
                "payment-service_2025-07-01_12-55-55.log",
                7);

        byte[] serialized = new LogEventSerializer().serialize("log-events", original);
        LogEvent deserialized = new LogEventDeserializer().deserialize("log-events", serialized);

        assertEquals(original, deserialized);
    }
}
