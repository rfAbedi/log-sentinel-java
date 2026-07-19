package com.logsentinel.ingester.file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class ExistingLogFileReader {

    private final Path directory;

    public ExistingLogFileReader(Path directory) {
        this.directory = directory;
    }

    public List<LogFileContent> readExisting() throws IOException {

        List<Path> logFiles;
        try (Stream<Path> entries = Files.list(directory)) {
            logFiles = entries
                    .filter(Files::isRegularFile)
                    .filter(this::hasLogExtension)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }

        List<LogFileContent> contents = new ArrayList<>(logFiles.size());
        for (Path logFile : logFiles) {
            contents.add(new LogFileContent(
                    logFile,
                    Files.readAllLines(logFile, StandardCharsets.UTF_8)));
        }
        return List.copyOf(contents);
    }

    private boolean hasLogExtension(Path path) {
        return path.getFileName().toString().endsWith(".log");
    }
}
