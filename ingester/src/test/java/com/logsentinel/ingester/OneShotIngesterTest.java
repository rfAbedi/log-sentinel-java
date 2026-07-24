package com.logsentinel.ingester;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.ingester.file.ExistingLogFileReader;
import com.logsentinel.ingester.kafka.LogEventPublisher;
import com.logsentinel.ingester.mapping.LogEventMapper;
import com.logsentinel.ingester.parser.LogLineParser;
import com.logsentinel.ingester.processing.LogFileProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OneShotIngesterTest {

    @TempDir
    Path directory;

    @Test
    void processesEveryExistingLogFileOnce() throws IOException {
        Files.writeString(
                directory.resolve("service-b_2025-07-01_12-56-00.log"),
                "2025-07-01 12:56:08,125 [worker-2] ERROR com.example.Service - second");
        Files.writeString(
                directory.resolve("service-a_2025-07-01_12-55-00.log"),
                "2025-07-01 12:56:07,451 [worker-1] INFO com.example.Service - first");
        Files.writeString(directory.resolve("notes.txt"), "ignored");

        RecordingPublisher publisher = new RecordingPublisher();
        OneShotIngester ingester = new OneShotIngester(
                new ExistingLogFileReader(directory),
                processor(publisher));

        int processedFiles = ingester.run();

        assertEquals(2, processedFiles);
        assertEquals(List.of("service-a", "service-b"), publisher.components());
        assertEquals(List.of("first", "second"), publisher.messages());
    }

    @Test
    void exitsWithoutPublishingWhenDirectoryHasNoLogFiles() throws IOException {
        RecordingPublisher publisher = new RecordingPublisher();
        OneShotIngester ingester = new OneShotIngester(
                new ExistingLogFileReader(directory),
                processor(publisher));

        int processedFiles = ingester.run();

        assertEquals(0, processedFiles);
        assertEquals(List.of(), publisher.events);
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
