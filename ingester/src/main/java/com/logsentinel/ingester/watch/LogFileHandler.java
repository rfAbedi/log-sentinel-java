package com.logsentinel.ingester.watch;

import java.nio.file.Path;

@FunctionalInterface
public interface LogFileHandler {

    void handle(Path path) throws Exception;
}
