package com.logsentinel.ingester.watch;

import com.logsentinel.ingester.file.LogFileContent;
import com.logsentinel.ingester.processing.LogFileProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class PublishingLogFileHandler implements LogFileHandler {

    private final LogFileProcessor fileProcessor;

    public PublishingLogFileHandler(LogFileProcessor fileProcessor) {
        this.fileProcessor = fileProcessor;
    }

    @Override
    public void handle(Path path) throws IOException, InterruptedException, ExecutionException {
        LogFileContent fileContent = new LogFileContent(
                path,
                Files.readAllLines(path, StandardCharsets.UTF_8));

        waitForAll(fileProcessor.process(fileContent));
        Files.delete(path);
    }

    private void waitForAll(List<Future<?>> publishResults)
            throws InterruptedException, ExecutionException {
        try {
            for (Future<?> publishResult : publishResults) {
                publishResult.get();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw exception;
        }
    }
}
