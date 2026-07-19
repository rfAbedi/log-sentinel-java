package com.logsentinel.ingester.parser;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.contracts.LogLevel;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LogLineParser {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss,SSS")
            .withResolverStyle(ResolverStyle.STRICT);

            
    private static final Pattern LINE_PATTERN = Pattern.compile(
            "^(?<timestamp>\\S+ \\S+)\\s*"+
            "\\[(?<thread>[^]]+)\\]\\s*" +
            "(?<level>\\S+)\\s*" +
            "(?<logger>\\S+)\\s*" +
            "[\\-–—]\\s*" +
            "(?<message>.*)$"
        );

    public LogEvent parse(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Log line must not be null or blank");
        }

        Matcher matcher = LINE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Malformed log line: expected timestamp, thread, level, logger, separator, and message");
        }

        LogLevel level;
        try {
            level = LogLevel.valueOf(matcher.group("level"));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported log level: " + matcher.group(3), exception);
        }

        LocalDateTime timestamp;
        try {
            timestamp = LocalDateTime.parse(matcher.group("timestamp"), TIMESTAMP_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Malformed log timestamp: " + matcher.group(1), exception);
        }

        return new LogEvent(
                null,
                null,
                timestamp.toInstant(ZoneOffset.UTC),
                level,
                matcher.group(2),
                matcher.group(4),
                matcher.group(5),
                null,
                0);
    }
}
