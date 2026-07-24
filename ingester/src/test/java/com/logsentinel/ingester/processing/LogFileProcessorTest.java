package com.logsentinel.ingester.processing;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.contracts.LogLevel;
import com.logsentinel.ingester.file.LogFileContent;
import com.logsentinel.ingester.kafka.LogEventPublisher;
import com.logsentinel.ingester.mapping.LogEventMapper;
import com.logsentinel.ingester.parser.LogLineParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LogFileProcessorTest {

    @Test
    void parsesMapsAndPublishesEveryLineInOrder() {
        RecordingPublisher publisher = new RecordingPublisher();
        LogFileProcessor processor = processor(publisher);
        LogFileContent fileContent = new LogFileContent(
                Path.of("payment-service_2025-07-01_12-55-55.log"),
                List.of(
                        "2025-07-01 12:56:07,451 [http-worker-1] INFO com.example.PaymentService - Payment started",
                        "2025-07-01 12:56:08,125 [http-worker-1] ERROR com.example.GatewayClient — Payment gateway timeout"));

        List<Future<?>> results = processor.process(fileContent);

        assertEquals(2, results.size());
        assertEquals(2, publisher.events.size());

        LogEvent first = publisher.events.get(0);
        assertEquals("payment-service", first.component());
        assertEquals(Instant.parse("2025-07-01T12:56:07.451Z"), first.eventTime());
        assertEquals(LogLevel.INFO, first.level());
        assertEquals("Payment started", first.message());
        assertEquals("payment-service_2025-07-01_12-55-55.log", first.sourceFile());
        assertEquals(1, first.lineNumber());

        LogEvent second = publisher.events.get(1);
        assertEquals(LogLevel.ERROR, second.level());
        assertEquals("Payment gateway timeout", second.message());
        assertEquals(2, second.lineNumber());
    }

    @Test
    void stopsAndPropagatesWhenPublishingFails() {
        RuntimeException failure = new RuntimeException("Kafka unavailable");
        FailingPublisher publisher = new FailingPublisher(failure);
        LogFileProcessor processor = processor(publisher);
        LogFileContent fileContent = new LogFileContent(
                Path.of("payment-service_2025-07-01_12-55-55.log"),
                List.of(
                        "2025-07-01 12:56:07,451 [worker-1] INFO com.example.Service - first",
                        "2025-07-01 12:56:08,451 [worker-1] INFO com.example.Service - second",
                        "2025-07-01 12:56:09,451 [worker-1] INFO com.example.Service - third"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> processor.process(fileContent));

        assertEquals(failure, thrown);
        assertEquals(2, publisher.attemptedEvents.size());
        assertEquals(1, publisher.attemptedEvents.get(0).lineNumber());
        assertEquals(2, publisher.attemptedEvents.get(1).lineNumber());
    }

    private LogFileProcessor processor(LogEventPublisher publisher) {
        return new LogFileProcessor(
                new LogLineParser(),
                new LogEventMapper(ZoneOffset.UTC),
                publisher);
    }

    private static final class RecordingPublisher implements LogEventPublisher {

        private final List<LogEvent> events = new ArrayList<>();

        @Override
        public Future<?> publish(LogEvent event) {
            events.add(event);
            return CompletableFuture.completedFuture(null);
        }
    }

    private static final class FailingPublisher implements LogEventPublisher {

        private final RuntimeException failure;
        private final List<LogEvent> attemptedEvents = new ArrayList<>();

        private FailingPublisher(RuntimeException failure) {
            this.failure = failure;
        }

        @Override
        public Future<?> publish(LogEvent event) {
            attemptedEvents.add(event);
            if (attemptedEvents.size() == 2) {
                throw failure;
            }
            return CompletableFuture.completedFuture(null);
        }
    }
}
