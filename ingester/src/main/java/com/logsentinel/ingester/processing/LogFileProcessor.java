package com.logsentinel.ingester.processing;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.ingester.file.LogFileContent;
import com.logsentinel.ingester.kafka.LogEventPublisher;
import com.logsentinel.ingester.mapping.LogEventMapper;
import com.logsentinel.ingester.parser.LogLineParser;
import com.logsentinel.ingester.parser.ParsedLogLine;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public final class LogFileProcessor {

    private final LogLineParser lineParser;
    private final LogEventMapper eventMapper;
    private final LogEventPublisher publisher;

    public LogFileProcessor(
            LogLineParser lineParser,
            LogEventMapper eventMapper,
            LogEventPublisher publisher) {
        this.lineParser = lineParser;
        this.eventMapper = eventMapper;
        this.publisher = publisher;
    }

    public List<Future<?>> process(LogFileContent fileContent) {
        List<Future<?>> publishResults = new ArrayList<>();

        for (int index = 0; index < fileContent.lines().size(); index++) {
            long lineNumber = index + 1L;
            ParsedLogLine parsedLine;

            try {
                parsedLine = lineParser.parse(fileContent.lines().get(index));
            } catch (IllegalArgumentException exception) {
                System.err.printf(
                        "Skipping malformed log line %s:%d: %s%n",
                        fileContent.path(),
                        lineNumber,
                        exception.getMessage());
                continue;
            }

            LogEvent event = eventMapper.map(parsedLine, fileContent.path(), lineNumber);
            publishResults.add(publisher.publish(event));
        }

        return List.copyOf(publishResults);
    }
}
