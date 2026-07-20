package com.logsentinel.evaluator.rule;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.evaluator.rule.config.LevelCountRuleConfiguration;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class LevelCountRuleEvaluator {

    private final Map<LevelCountRuleConfiguration, Deque<Instant>> eventTimesByRule =
            new HashMap<>();

    public boolean matches(LevelCountRuleConfiguration rule, LogEvent event) {
        if (!rule.component().equals(event.component()) || rule.level() != event.level()) {
            return false;
        }

        Deque<Instant> eventTimes = eventTimesByRule.computeIfAbsent(
                rule,
                ignored -> new ArrayDeque<>());
        Instant windowStart = event.eventTime().minus(rule.window());

        while (!eventTimes.isEmpty() && eventTimes.getFirst().isBefore(windowStart)) {
            eventTimes.removeFirst();
        }

        eventTimes.addLast(event.eventTime());
        return eventTimes.size() >= rule.threshold();
    }
}
