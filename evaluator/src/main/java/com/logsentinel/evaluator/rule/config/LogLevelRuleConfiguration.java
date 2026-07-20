package com.logsentinel.evaluator.rule.config;

import com.logsentinel.contracts.LogLevel;

public record LogLevelRuleConfiguration(
        String id,
        String component,
        LogLevel level
) implements RuleConfiguration {
}
