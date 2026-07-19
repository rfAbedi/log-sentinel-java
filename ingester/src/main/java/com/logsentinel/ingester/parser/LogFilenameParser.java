package com.logsentinel.ingester.parser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LogFilenameParser {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("uuuu-MM-dd_HH-mm-ss")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final Pattern FILENAME_PATTERN = Pattern.compile(
            "^(?<component>[A-Za-z0-9][A-Za-z0-9._-]*)_(?<timestamp>\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2})\\.log$");

    public String extractComponent(String filename) {

        Matcher matcher = FILENAME_PATTERN.matcher(filename);
        matcher.matches();

        LocalDateTime.parse(matcher.group("timestamp"), TIMESTAMP_FORMATTER);

        return matcher.group("component");
    }
}
