package com.logsentinel.evaluator.rule;

import com.logsentinel.contracts.Alert;
import com.logsentinel.contracts.LogEvent;
import com.logsentinel.contracts.LogLevel;
import com.logsentinel.evaluator.rule.config.LevelCountRuleConfiguration;
import com.logsentinel.evaluator.rule.config.LogLevelRuleConfiguration;
import com.logsentinel.evaluator.rule.config.TotalLogRateRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleEvaluationServiceTest {

    @Test
    void createsAlertWhenLogLevelRuleMatches() {
        RuleEvaluationService service = new RuleEvaluationService(List.of(
                new LogLevelRuleConfiguration(
                        "gateway-errors",
                        "gateway",
                        LogLevel.ERROR)));
        LogEvent event = event("event-1", LogLevel.ERROR, 0);

        List<Alert> alerts = service.evaluate(event);

        assertEquals(List.of(new Alert(
                "gateway-errors",
                "gateway",
                event.eventTime(),
                "Payment gateway timeout",
                "ERROR")), alerts);
    }

    @Test
    void returnsNoAlertWhenRuleDoesNotMatch() {
        RuleEvaluationService service = new RuleEvaluationService(List.of(
                new LogLevelRuleConfiguration(
                        "gateway-errors",
                        "gateway",
                        LogLevel.ERROR)));

        List<Alert> alerts = service.evaluate(event("event-1", LogLevel.INFO, 0));

        assertTrue(alerts.isEmpty());
    }

    @Test
    void createsAlertsWhenCountAndRateThresholdsAreReached() {
        RuleEvaluationService service = new RuleEvaluationService(List.of(
                new LevelCountRuleConfiguration(
                        "gateway-error-count",
                        "gateway",
                        LogLevel.ERROR,
                        2,
                        Duration.ofMinutes(1)),
                new TotalLogRateRuleConfiguration(
                        "gateway-rate",
                        "gateway",
                        2,
                        Duration.ofMinutes(1))));

        assertTrue(service.evaluate(event("event-1", LogLevel.ERROR, 0)).isEmpty());

        LogEvent triggeringEvent = event("event-2", LogLevel.ERROR, 30);
        List<Alert> alerts = service.evaluate(triggeringEvent);

        assertEquals(List.of(
                new Alert(
                        "gateway-error-count",
                        "gateway",
                        triggeringEvent.eventTime(),
                        triggeringEvent.message(),
                        "ERROR"),
                new Alert(
                        "gateway-rate",
                        "gateway",
                        triggeringEvent.eventTime(),
                        triggeringEvent.message(),
                        "ERROR")), alerts);
    }

    private LogEvent event(String eventId, LogLevel level, long seconds) {
        return new LogEvent(
                eventId,
                "gateway",
                Instant.parse("2025-07-01T12:00:00Z").plusSeconds(seconds),
                level,
                "http-worker-1",
                "com.example.GatewayClient",
                "Payment gateway timeout",
                "gateway.log",
                seconds + 1);
    }
}
