package com.logsentinel.ingester.watch;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Objects;

public final class LogDirectoryWatcher implements AutoCloseable {

    private final Path directory;
    private final LogFileHandler fileHandler;
    private final WatchService watchService;

    private volatile boolean closed;

    public LogDirectoryWatcher(Path directory, LogFileHandler fileHandler) throws IOException {
        this.directory = Objects.requireNonNull(directory, "directory");
        this.fileHandler = Objects.requireNonNull(fileHandler, "fileHandler");
        this.watchService = directory.getFileSystem().newWatchService();

        directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
    }

    public void run() throws Exception {
        try {
            while (!closed) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    Path path = directory.resolve((Path) event.context());
                    if (isLogFile(path)) {
                        fileHandler.handle(path);
                    }
                }

                if (!key.reset()) {
                    return;
                }
            }
        } catch (ClosedWatchServiceException ignored) {
            // Closing the watcher is the normal shutdown path.
        }
    }

    private boolean isLogFile(Path path) {
        return Files.isRegularFile(path)
                && path.getFileName().toString().endsWith(".log");
    }

    @Override
    public void close() throws IOException {
        closed = true;
        watchService.close();
    }
}
