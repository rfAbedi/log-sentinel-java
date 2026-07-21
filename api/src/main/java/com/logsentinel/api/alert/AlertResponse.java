package com.logsentinel.api.alert;

import java.time.Instant;

public record AlertResponse(
        Long id,
        String ruleId,
        String component,
        Instant triggeredAt,
        String message,
        String level
) {

    public static AlertResponse from(AlertEntity alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getRuleId(),
                alert.getComponent(),
                alert.getTriggeredAt(),
                alert.getMessage(),
                alert.getLevel());
    }
}
