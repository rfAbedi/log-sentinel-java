package com.logsentinel.evaluator.rule;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.contracts.LogLevel;
import com.logsentinel.evaluator.rule.config.LevelCountRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelCountRuleEvaluatorTest {

    private final LevelCountRuleEvaluator evaluator = new LevelCountRuleEvaluator();
    private final LevelCountRuleConfiguration rule = new LevelCountRuleConfiguration(
            "gateway-errors",
            "gateway",
            LogLevel.ERROR,
            3,
            Duration.ofMinutes(1));

    @Test
    void matchesWhenThresholdIsReachedWithinWindow() {
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.ERROR, 0)));
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.ERROR, 20)));

        assertTrue(evaluator.matches(rule, event("gateway", LogLevel.ERROR, 40)));
    }

    @Test
    void ignoresEventsWithDifferentComponentOrLevel() {
        assertFalse(evaluator.matches(rule, event("payment", LogLevel.ERROR, 0)));
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.WARN, 10)));
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.ERROR, 20)));
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.ERROR, 30)));

        assertTrue(evaluator.matches(rule, event("gateway", LogLevel.ERROR, 40)));
    }

    @Test
    void removesEventsOlderThanWindow() {
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.ERROR, 0)));
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.ERROR, 30)));

        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.ERROR, 61)));
    }

    @Test
    void includesEventExactlyOnWindowBoundary() {
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.ERROR, 0)));
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.ERROR, 30)));

        assertTrue(evaluator.matches(rule, event("gateway", LogLevel.ERROR, 60)));
    }

    @Test
    void keepsStateIndependentForEachRule() {
        LevelCountRuleConfiguration paymentRule = new LevelCountRuleConfiguration(
                "payment-errors",
                "payment",
                LogLevel.ERROR,
                2,
                Duration.ofMinutes(1));

        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.ERROR, 0)));
        assertFalse(evaluator.matches(paymentRule, event("payment", LogLevel.ERROR, 10)));
        assertFalse(evaluator.matches(rule, event("gateway", LogLevel.ERROR, 20)));

        assertTrue(evaluator.matches(paymentRule, event("payment", LogLevel.ERROR, 30)));
        assertTrue(evaluator.matches(rule, event("gateway", LogLevel.ERROR, 40)));
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
