package com.logsentinel.evaluator.processing;

import com.logsentinel.contracts.Alert;
import com.logsentinel.contracts.LogEvent;
import com.logsentinel.evaluator.alert.AlertRepository;
import com.logsentinel.evaluator.kafka.LogEventBatchConsumer;
import com.logsentinel.evaluator.rule.RuleEvaluationService;

import java.sql.SQLException;
import java.time.Duration;

public final class EvaluatorBatchProcessor {

    private final LogEventBatchConsumer consumer;
    private final RuleEvaluationService ruleEvaluationService;
    private final AlertRepository alertRepository;

    public EvaluatorBatchProcessor(
            LogEventBatchConsumer consumer,
            RuleEvaluationService ruleEvaluationService,
            AlertRepository alertRepository) {
        this.consumer = consumer;
        this.ruleEvaluationService = ruleEvaluationService;
        this.alertRepository = alertRepository;
    }

    public int pollOnce(Duration timeout) throws SQLException {
        int savedAlertCount = 0;

        for (LogEvent event : consumer.pollOnce(timeout)) {
            for (Alert alert : ruleEvaluationService.evaluate(event)) {
                alertRepository.save(alert);
                savedAlertCount++;
            }
        }

        return savedAlertCount;
    }
}
