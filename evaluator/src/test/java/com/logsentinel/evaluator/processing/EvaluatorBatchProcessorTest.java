package com.logsentinel.evaluator.processing;

import com.logsentinel.contracts.Alert;
import com.logsentinel.contracts.LogEvent;
import com.logsentinel.contracts.LogLevel;
import com.logsentinel.evaluator.alert.AlertRepository;
import com.logsentinel.evaluator.rule.RuleEvaluationService;
import com.logsentinel.evaluator.rule.config.LogLevelRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EvaluatorBatchProcessorTest {

    @Test
    void returnsZeroWhenKafkaBatchIsEmpty() throws Exception {
        CapturingAlertRepository repository = new CapturingAlertRepository();
        EvaluatorBatchProcessor processor = processor(List.of(), repository);

        int savedAlertCount = processor.pollOnce(Duration.ofSeconds(1));

        assertEquals(0, savedAlertCount);
        assertEquals(List.of(), repository.savedAlerts);
    }

    @Test
    void evaluatesEveryEventAndSavesEveryGeneratedAlert() throws Exception {
        LogEvent firstError = event("event-1", LogLevel.ERROR, 0);
        LogEvent info = event("event-2", LogLevel.INFO, 1);
        LogEvent secondError = event("event-3", LogLevel.ERROR, 2);
        CapturingAlertRepository repository = new CapturingAlertRepository();
        EvaluatorBatchProcessor processor = processor(
                List.of(firstError, info, secondError),
                repository);

        int savedAlertCount = processor.pollOnce(Duration.ofSeconds(1));

        assertEquals(2, savedAlertCount);
        assertEquals(List.of(
                alertFor(firstError),
                alertFor(secondError)), repository.savedAlerts);
    }

    @Test
    void stopsAndPropagatesWhenSavingAnAlertFails() {
        LogEvent firstError = event("event-1", LogLevel.ERROR, 0);
        LogEvent secondError = event("event-2", LogLevel.ERROR, 1);
        CapturingAlertRepository repository = new CapturingAlertRepository(2);
        EvaluatorBatchProcessor processor = processor(
                List.of(firstError, secondError),
                repository);

        SQLException exception = assertThrows(
                SQLException.class,
                () -> processor.pollOnce(Duration.ofSeconds(1)));

        assertEquals("database unavailable", exception.getMessage());
        assertEquals(List.of(alertFor(firstError)), repository.savedAlerts);
    }

    private EvaluatorBatchProcessor processor(
            List<LogEvent> events,
            AlertRepository repository) {
        RuleEvaluationService ruleEvaluationService = new RuleEvaluationService(List.of(
                new LogLevelRuleConfiguration(
                        "gateway-errors",
                        "gateway",
                        LogLevel.ERROR)));

        return new EvaluatorBatchProcessor(
                timeout -> events,
                ruleEvaluationService,
                repository);
    }

    private Alert alertFor(LogEvent event) {
        return new Alert(
                "gateway-errors",
                "gateway",
                event.eventTime(),
                event.message(),
                "ERROR");
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

    private static final class CapturingAlertRepository implements AlertRepository {
        private final List<Alert> savedAlerts = new ArrayList<>();
        private final int failOnSaveNumber;
        private int saveAttempts;

        private CapturingAlertRepository() {
            this(Integer.MAX_VALUE);
        }

        private CapturingAlertRepository(int failOnSaveNumber) {
            this.failOnSaveNumber = failOnSaveNumber;
        }

        @Override
        public void save(Alert alert) throws SQLException {
            saveAttempts++;
            if (saveAttempts == failOnSaveNumber) {
                throw new SQLException("database unavailable");
            }
            savedAlerts.add(alert);
        }
    }
}
