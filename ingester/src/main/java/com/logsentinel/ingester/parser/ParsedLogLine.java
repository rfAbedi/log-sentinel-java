package com.logsentinel.ingester.parser;

import java.time.LocalDateTime;

public record ParsedLogLine(
        LocalDateTime timestamp,
        String threadName,
        String level,
        String loggerName,
        String message
) {
}
