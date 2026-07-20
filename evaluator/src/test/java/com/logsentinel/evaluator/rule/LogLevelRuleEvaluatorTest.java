package com.logsentinel.evaluator.rule;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.contracts.LogLevel;
import com.logsentinel.evaluator.rule.config.LogLevelRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogLevelRuleEvaluatorTest {

    private final LogLevelRuleEvaluator evaluator = new LogLevelRuleEvaluator();
    private final LogLevelRuleConfiguration rule =
            new LogLevelRuleConfiguration("gateway-errors", "gateway", LogLevel.ERROR);

    @Test
    void matchesEventWithConfiguredComponentAndLevel() {
        LogEvent event = event("gateway", LogLevel.ERROR);

        assertTrue(evaluator.matches(rule, event));
    }

    @Test
    void doesNotMatchDifferentLevel() {
        LogEvent event = event("gateway", LogLevel.WARN);

        assertFalse(evaluator.matches(rule, event));
    }

    @Test
    void doesNotMatchDifferentComponent() {
        LogEvent event = event("payment", LogLevel.ERROR);

        assertFalse(evaluator.matches(rule, event));
    }

    @Test
    void ignoresEventFieldsThatAreNotPartOfTheRule() {
        LogEvent event = new LogEvent(
                "event-2",
                "gateway",
                Instant.parse("2025-07-01T12:56:08Z"),
                LogLevel.ERROR,
                "another-thread",
                "com.example.OtherLogger",
                "A different message",
                "another.log",
                42);

        assertTrue(evaluator.matches(rule, event));
    }

    private LogEvent event(String component, LogLevel level) {
        return new LogEvent(
                "event-1",
                component,
                Instant.parse("2025-07-01T12:56:07Z"),
                level,
                "http-worker-1",
                "com.example.GatewayClient",
                "Payment gateway timeout",
                "gateway.log",
                1);
    }
}
