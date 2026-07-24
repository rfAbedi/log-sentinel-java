package com.logsentinel.ingester;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.ingester.file.ExistingLogFileReader;
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

class OneShotIngesterTest {

    @TempDir
    Path directory;

    @Test
    void processesAndDeletesEveryExistingLogFile() throws Exception {
        Path secondFile = directory.resolve("service-b_2025-07-01_12-56-00.log");
        Path firstFile = directory.resolve("service-a_2025-07-01_12-55-00.log");
        Path ignoredFile = directory.resolve("notes.txt");

        Files.writeString(
                secondFile,
                "2025-07-01 12:56:08,125 [worker-2] ERROR com.example.Service - second");
        Files.writeString(
                firstFile,
                "2025-07-01 12:56:07,451 [worker-1] INFO com.example.Service - first");
        Files.writeString(ignoredFile, "ignored");

        RecordingPublisher publisher = new RecordingPublisher();
        OneShotIngester ingester = new OneShotIngester(
                new ExistingLogFileReader(directory),
                processor(publisher));

        int processedFiles = ingester.run();

        assertEquals(2, processedFiles);
        assertEquals(List.of("service-a", "service-b"), publisher.components());
        assertEquals(List.of("first", "second"), publisher.messages());
        assertFalse(Files.exists(firstFile));
        assertFalse(Files.exists(secondFile));
        assertTrue(Files.exists(ignoredFile));
    }

    @Test
    void exitsWithoutPublishingWhenDirectoryHasNoLogFiles() throws Exception {
        RecordingPublisher publisher = new RecordingPublisher();
        OneShotIngester ingester = new OneShotIngester(
                new ExistingLogFileReader(directory),
                processor(publisher));

        int processedFiles = ingester.run();

        assertEquals(0, processedFiles);
        assertEquals(List.of(), publisher.events);
    }

    @Test
    void keepsFileWhenPublishFutureFails() throws Exception {
        Path sourceFile = directory.resolve("service-a_2025-07-01_12-55-00.log");
        Files.writeString(
                sourceFile,
                "2025-07-01 12:56:07,451 [worker-1] ERROR com.example.Service - failed");

        RuntimeException kafkaFailure = new RuntimeException("Kafka unavailable");
        LogEventPublisher publisher = event -> CompletableFuture.failedFuture(kafkaFailure);
        OneShotIngester ingester = new OneShotIngester(
                new ExistingLogFileReader(directory),
                processor(publisher));

        ExecutionException thrown = assertThrows(ExecutionException.class, ingester::run);

        assertEquals(kafkaFailure, thrown.getCause());
        assertTrue(Files.exists(sourceFile));
    }

    @Test
    void keepsFileWhenPublisherThrowsSynchronously() throws Exception {
        Path sourceFile = directory.resolve("service-a_2025-07-01_12-55-00.log");
        Files.writeString(
                sourceFile,
                "2025-07-01 12:56:07,451 [worker-1] ERROR com.example.Service - failed");

        RuntimeException kafkaFailure = new RuntimeException("Kafka unavailable");
        LogEventPublisher publisher = event -> {
            throw kafkaFailure;
        };
        OneShotIngester ingester = new OneShotIngester(
                new ExistingLogFileReader(directory),
                processor(publisher));

        RuntimeException thrown = assertThrows(RuntimeException.class, ingester::run);

        assertEquals(kafkaFailure, thrown);
        assertTrue(Files.exists(sourceFile));
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

        private List<String> components() {
            return events.stream().map(LogEvent::component).toList();
        }

        private List<String> messages() {
            return events.stream().map(LogEvent::message).toList();
        }
    }
}
