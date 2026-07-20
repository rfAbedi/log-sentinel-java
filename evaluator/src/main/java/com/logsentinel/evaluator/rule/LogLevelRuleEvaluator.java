package com.logsentinel.evaluator.rule;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.evaluator.rule.config.LogLevelRuleConfiguration;

public final class LogLevelRuleEvaluator {

    public boolean matches(LogLevelRuleConfiguration rule, LogEvent event) {
        return rule.component().equals(event.component())
                && rule.level() == event.level();
    }
}
