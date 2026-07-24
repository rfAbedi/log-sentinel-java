package com.logsentinel.evaluator.kafka;

import com.logsentinel.contracts.LogEvent;

import java.time.Duration;
import java.util.List;

@FunctionalInterface
public interface LogEventBatchConsumer {

    List<LogEvent> pollOnce(Duration timeout);
}
