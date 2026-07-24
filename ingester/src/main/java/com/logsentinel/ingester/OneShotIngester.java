package com.logsentinel.ingester;

import com.logsentinel.ingester.file.ExistingLogFileReader;
import com.logsentinel.ingester.file.LogFileContent;
import com.logsentinel.ingester.processing.LogFileProcessor;

import java.io.IOException;
import java.util.List;

public final class OneShotIngester {

    private final ExistingLogFileReader fileReader;
    private final LogFileProcessor fileProcessor;

    public OneShotIngester(
            ExistingLogFileReader fileReader,
            LogFileProcessor fileProcessor) {
        this.fileReader = fileReader;
        this.fileProcessor = fileProcessor;
    }

    public int run() throws IOException {
        List<LogFileContent> files = fileReader.readExisting();
        files.forEach(fileProcessor::process);
        return files.size();
    }
}
