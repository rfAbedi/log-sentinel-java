package com.logsentinel.ingester.mapping;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.contracts.LogLevel;
import com.logsentinel.ingester.parser.LogFilenameParser;
import com.logsentinel.ingester.parser.ParsedLogLine;

import java.nio.file.Path;
import java.time.ZoneId;
import java.util.UUID;

public final class LogEventMapper {

    private final ZoneId sourceLogZone;
    private final LogFilenameParser filenameParser = new LogFilenameParser();

    public LogEventMapper(ZoneId sourceLogZone) {
        this.sourceLogZone = sourceLogZone;
    }

    public LogEvent map(ParsedLogLine parsedLine, Path sourceFile, long lineNumber) {

        String filename = sourceFile.getFileName().toString();
        LogLevel level = LogLevel.valueOf(parsedLine.level());

        return new LogEvent(
                UUID.randomUUID().toString(),
                filenameParser.extractComponent(filename),
                parsedLine.timestamp().atZone(sourceLogZone).toInstant(),
                level,
                parsedLine.threadName(),
                parsedLine.loggerName(),
                parsedLine.message(),
                filename,
                lineNumber);
    }
}
