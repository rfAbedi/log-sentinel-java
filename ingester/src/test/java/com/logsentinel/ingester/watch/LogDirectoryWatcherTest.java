package com.logsentinel.ingester.watch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogDirectoryWatcherTest {

    @TempDir
    Path directory;

    @Test
    void handlesNewLogFilesAndIgnoresOtherFiles() throws Exception {
        CountDownLatch handled = new CountDownLatch(1);
        AtomicReference<Path> handledPath = new AtomicReference<>();

        try (LogDirectoryWatcher watcher = new LogDirectoryWatcher(directory, path -> {
            handledPath.set(path);
            handled.countDown();
        })) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<?> watcherTask = executor.submit(() -> {
                try {
                    watcher.run();
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            });

            Path ignored = directory.resolve("notes.txt");
            Path logFile = directory.resolve("payment-service_2025-07-01_12-55-55.log");
            Files.writeString(ignored, "ignored");
            Files.writeString(logFile, "log");

            assertTrue(handled.await(3, TimeUnit.SECONDS));
            assertEquals(logFile, handledPath.get());

            watcher.close();
            watcherTask.get(3, TimeUnit.SECONDS);
            executor.shutdownNow();
        }
    }
}
