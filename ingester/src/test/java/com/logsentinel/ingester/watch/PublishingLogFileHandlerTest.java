package com.logsentinel.ingester.watch;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.ingester.kafka.LogEventPublisher;
import com.logsentinel.ingester.mapping.LogEventMapper;
import com.logsentinel.ingester.parser.LogLineParser;
import com.logsentinel.ingester.processing.LogFileProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublishingLogFileHandlerTest {

    @TempDir
    Path directory;

    @Test
    void publishesTheFileAndDeletesItAfterSuccess() throws Exception {
        Path file = writeLogFile();
        RecordingPublisher publisher = new RecordingPublisher();
        PublishingLogFileHandler handler = new PublishingLogFileHandler(processor(publisher));

        handler.handle(file);

        assertEquals(1, publisher.events.size());
        assertEquals("Payment gateway timeout", publisher.events.getFirst().message());
        assertFalse(Files.exists(file));
    }

    @Test
    void keepsTheFileWhenPublishingFails() throws Exception {
        Path file = writeLogFile();
        RuntimeException kafkaFailure = new RuntimeException("Kafka unavailable");
        LogEventPublisher publisher = event -> CompletableFuture.failedFuture(kafkaFailure);
        PublishingLogFileHandler handler = new PublishingLogFileHandler(processor(publisher));

        ExecutionException thrown = assertThrows(ExecutionException.class, () -> handler.handle(file));

        assertEquals(kafkaFailure, thrown.getCause());
        assertTrue(Files.exists(file));
    }

    private Path writeLogFile() throws Exception {
        Path file = directory.resolve("payment-service_2025-07-01_12-55-55.log");
        Files.writeString(
                file,
                "2025-07-01 12:56:07,451 [http-worker-1] ERROR com.example.GatewayClient - Payment gateway timeout");
        return file;
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
}
