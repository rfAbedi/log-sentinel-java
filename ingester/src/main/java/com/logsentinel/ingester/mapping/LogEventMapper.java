package com.logsentinel.ingester.mapping;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.contracts.LogLevel;
import com.logsentinel.ingester.parser.LogFilenameParser;
import com.logsentinel.ingester.parser.ParsedLogLine;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
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
        Instant eventTime = parsedLine.timestamp().atZone(sourceLogZone).toInstant();

        return new LogEvent(
                eventId(filename, lineNumber, eventTime, level, parsedLine),
                filenameParser.extractComponent(filename),
                eventTime,
                level,
                parsedLine.threadName(),
                parsedLine.loggerName(),
                parsedLine.message(),
                filename,
                lineNumber);
    }

    private String eventId(
            String filename,
            long lineNumber,
            Instant eventTime,
            LogLevel level,
            ParsedLogLine parsedLine) {
        String identity = String.join(
                "\u001f",
                filename,
                Long.toString(lineNumber),
                eventTime.toString(),
                parsedLine.threadName(),
                level.name(),
                parsedLine.loggerName(),
                parsedLine.message());

        return UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
