package com.logsentinel.contracts;

import java.time.Instant;

public record LogEvent(
        String eventId,
        String component,
        Instant eventTime,
        LogLevel level,
        String thread,
        String logger,
        String message,
        String sourceFile,
        long lineNumber
) {
}