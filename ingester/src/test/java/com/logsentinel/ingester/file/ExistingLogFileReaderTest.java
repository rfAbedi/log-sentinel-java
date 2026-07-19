package com.logsentinel.ingester.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExistingLogFileReaderTest {

    @TempDir
    Path directory;

    @Test
    void readsExistingLogFilesInFilenameOrder() throws IOException {
        Files.writeString(directory.resolve("service-b_2025-07-01_12-56-00.log"), "line b");
        Files.writeString(directory.resolve("service-a_2025-07-01_12-55-00.log"), "first\nsecond\n");
        Files.writeString(directory.resolve("notes.txt"), "not a log");

        List<LogFileContent> files = new ExistingLogFileReader(directory).readExisting();

        assertEquals(List.of(
                "service-a_2025-07-01_12-55-00.log",
                "service-b_2025-07-01_12-56-00.log"), fileNames(files));
        assertEquals(List.of("first", "second"), files.get(0).lines());
        assertEquals(List.of("line b"), files.get(1).lines());
    }

    @Test
    void ignoresUppercaseExtensionAndNestedLogFiles() throws IOException {
        Files.writeString(directory.resolve("service.LOG"), "uppercase extension");
        Path nestedDirectory = Files.createDirectory(directory.resolve("nested"));
        Files.writeString(nestedDirectory.resolve("nested.log"), "nested line");

        List<LogFileContent> files = new ExistingLogFileReader(directory).readExisting();

        assertTrue(files.isEmpty());
    }

    @Test
    void returnsEmptyListForEmptyDirectory() throws IOException {
        List<LogFileContent> files = new ExistingLogFileReader(directory).readExisting();

        assertTrue(files.isEmpty());
    }

    private List<String> fileNames(List<LogFileContent> files) {
        return files.stream()
                .map(file -> file.path().getFileName().toString())
                .toList();
    }
}
