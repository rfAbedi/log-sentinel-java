package com.logsentinel.ingester.parser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Set;
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

    private static final Set<String> SUPPORTED_LEVELS = Set.of(
            "TRACE", "DEBUG", "INFO", "WARN", "ERROR");

    public ParsedLogLine parse(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Log line must not be null or blank");
        }

        Matcher matcher = LINE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Malformed log line: expected timestamp, thread, level, logger, separator, and message");
        }

        String level = matcher.group("level");
        if (!SUPPORTED_LEVELS.contains(level)) {
            throw new IllegalArgumentException("Unsupported log level: " + level);
        }

        LocalDateTime timestamp;
        try {
            timestamp = LocalDateTime.parse(matcher.group("timestamp"), TIMESTAMP_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Malformed log timestamp: " + matcher.group(1), exception);
        }

        return new ParsedLogLine(
                timestamp,
                matcher.group("thread"),
                level,
                matcher.group("logger"),
                matcher.group("message"));
    }
}
