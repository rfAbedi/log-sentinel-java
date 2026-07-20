package com.logsentinel.evaluator.rule.config;

import java.time.Duration;

public record TotalLogRateRuleConfiguration(
        String id,
        String component,
        int threshold,
        Duration window
) implements RuleConfiguration {
}
