package com.logsentinel.evaluator.rule.config;

public sealed interface RuleConfiguration permits
        LogLevelRuleConfiguration,
        LevelCountRuleConfiguration,
        TotalLogRateRuleConfiguration {

    String id();

    String component();
}
