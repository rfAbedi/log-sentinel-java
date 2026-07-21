package com.logsentinel.evaluator.alert;

import java.time.Instant;

public record Alert(
        String ruleId,
        String component,
        Instant triggeredAt,
        String message,
        String level
) {
}
