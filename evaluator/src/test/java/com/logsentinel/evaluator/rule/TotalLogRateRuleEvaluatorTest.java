package com.logsentinel.evaluator.rule;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.contracts.LogLevel;
import com.logsentinel.evaluator.rule.config.TotalLogRateRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotalLogRateRuleEvaluatorTest {

    private final TotalLogRateRuleEvaluator evaluator = new TotalLogRateRuleEvaluator();
    private final TotalLogRateRuleConfiguration rule = new TotalLogRateRuleConfiguration(
            "gateway-rate",
            "gateway",
            3,
            Duration.ofMinutes(1));

    @Test
    void matchesWhenTotalEventsReachThresholdWithinWindow() {
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.INFO, 0)));
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.WARN, 20)));

        assertTrue(evaluator.matches(rule, event("gateway", LogLevel.ERROR, 40)));
    }

    @Test
    void ignoresEventsFromDifferentComponent() {
        assertFalse(evaluator.matches(rule, event("payment", LogLevel.INFO, 0)));
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.INFO, 10)));
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.DEBUG, 20)));

        assertTrue(evaluator.matches(rule, event("gateway", LogLevel.TRACE, 30)));
    }

    @Test
    void removesEventsOlderThanWindow() {
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.INFO, 0)));
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.INFO, 30)));

        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.INFO, 61)));
    }

    @Test
    void includesEventExactlyOnWindowBoundary() {
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.INFO, 0)));
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.INFO, 30)));

        assertTrue(evaluator.matches(rule, event("gateway", LogLevel.INFO, 60)));
    }

    @Test
    void keepsStateIndependentForEachRule() {
        TotalLogRateRuleConfiguration paymentRule = new TotalLogRateRuleConfiguration(
                "payment-rate",
                "payment",
                2,
                Duration.ofMinutes(1));

        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.INFO, 0)));
        assertFalse(evaluator.matches(paymentRule, event("payment", LogLevel.WARN, 10)));
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.ERROR, 20)));

        assertTrue(evaluator.matches(paymentRule, event("payment", LogLevel.ERROR, 30)));
        assertTrue(evaluator.matches(rule, event("gateway", LogLevel.DEBUG, 40)));
    }

    private LogEvent event(String component, LogLevel level, long seconds) {
        return new LogEvent(
                component + "-" + seconds,
                component,
                Instant.parse("2025-07-01T12:00:00Z").plusSeconds(seconds),
                level,
                "worker-1",
                "com.example.Service",
                "Test message",
                component + ".log",
                seconds + 1);
    }
}
