package com.logsentinel.ingester.kafka;

import com.logsentinel.contracts.LogEvent;
import java.util.concurrent.Future;

public interface LogEventPublisher {

    Future<?> publish(LogEvent event);
}
