package com.logsentinel.evaluator.rule;

import com.logsentinel.contracts.LogEvent;
import com.logsentinel.evaluator.rule.config.TotalLogRateRuleConfiguration;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class TotalLogRateRuleEvaluator {

    private final Map<TotalLogRateRuleConfiguration, Deque<Instant>> eventTimesByRule =
            new HashMap<>();

    public boolean matches(TotalLogRateRuleConfiguration rule, LogEvent event) {
        if (!rule.component().equals(event.component())) {
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
