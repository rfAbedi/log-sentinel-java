package com.logsentinel.evaluator.rule.config;

import com.logsentinel.contracts.LogLevel;

import java.time.Duration;

public record LevelCountRuleConfiguration(
        String id,
        String component,
        LogLevel level,
        int threshold,
        Duration window
) implements RuleConfiguration {
}
