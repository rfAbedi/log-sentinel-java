package com.logsentinel.evaluator.rule;

import com.logsentinel.contracts.Alert;
import com.logsentinel.contracts.LogEvent;
import com.logsentinel.evaluator.rule.config.LevelCountRuleConfiguration;
import com.logsentinel.evaluator.rule.config.LogLevelRuleConfiguration;
import com.logsentinel.evaluator.rule.config.RuleConfiguration;
import com.logsentinel.evaluator.rule.config.TotalLogRateRuleConfiguration;

import java.util.List;

public final class RuleEvaluationService {

    private final List<RuleConfiguration> rules;
    private final LogLevelRuleEvaluator logLevelRuleEvaluator = new LogLevelRuleEvaluator();
    private final LevelCountRuleEvaluator levelCountRuleEvaluator = new LevelCountRuleEvaluator();
    private final TotalLogRateRuleEvaluator totalLogRateRuleEvaluator = new TotalLogRateRuleEvaluator();

    public RuleEvaluationService(List<RuleConfiguration> rules) {
        this.rules = List.copyOf(rules);
    }

    public List<Alert> evaluate(LogEvent event) {
        return rules.stream()
                .filter(rule -> matches(rule, event))
                .map(rule -> toAlert(rule, event))
                .toList();
    }

    private boolean matches(RuleConfiguration rule, LogEvent event) {
        return switch (rule) {
            case LogLevelRuleConfiguration logLevelRule ->
                    logLevelRuleEvaluator.matches(logLevelRule, event);
            case LevelCountRuleConfiguration levelCountRule ->
                    levelCountRuleEvaluator.matches(levelCountRule, event);
            case TotalLogRateRuleConfiguration totalLogRateRule ->
                    totalLogRateRuleEvaluator.matches(totalLogRateRule, event);
        };
    }

    private Alert toAlert(RuleConfiguration rule, LogEvent event) {
        return new Alert(
                rule.id(),
                event.component(),
                event.eventTime(),
                event.message(),
                event.level().name());
    }
}
