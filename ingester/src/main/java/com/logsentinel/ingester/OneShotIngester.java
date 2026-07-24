package com.logsentinel.ingester;

import com.logsentinel.ingester.file.ExistingLogFileReader;
import com.logsentinel.ingester.file.LogFileContent;
import com.logsentinel.ingester.processing.LogFileProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class OneShotIngester {

    private final ExistingLogFileReader fileReader;
    private final LogFileProcessor fileProcessor;

    public OneShotIngester(
            ExistingLogFileReader fileReader,
            LogFileProcessor fileProcessor) {
        this.fileReader = fileReader;
        this.fileProcessor = fileProcessor;
    }

    public int run() throws IOException, InterruptedException, ExecutionException {
        List<LogFileContent> files = fileReader.readExisting();
        int processedFiles = 0;

        for (LogFileContent file : files) {
            waitForAll(fileProcessor.process(file));
            Files.delete(file.path());
            processedFiles++;
        }

        return processedFiles;
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
