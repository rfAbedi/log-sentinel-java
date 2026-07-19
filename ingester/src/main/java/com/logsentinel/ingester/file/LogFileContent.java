package com.logsentinel.ingester.file;

import java.nio.file.Path;
import java.util.List;

public record LogFileContent(Path path, List<String> lines) {

    public LogFileContent {
        lines = List.copyOf(lines);
    }
}
