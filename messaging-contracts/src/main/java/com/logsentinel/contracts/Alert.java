package com.logsentinel.contracts;

import java.time.Instant;

public record Alert(
        String sourceEventId,
        String ruleId,
        String component,
        Instant triggeredAt,
        String message,
        String level
) {
}
