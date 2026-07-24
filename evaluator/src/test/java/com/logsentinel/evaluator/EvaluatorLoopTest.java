package com.logsentinel.evaluator;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EvaluatorLoopTest {

    @Test
    void pollsUntilTheStopConditionChanges() throws Exception {
        AtomicInteger polls = new AtomicInteger();
        Duration timeout = Duration.ofMillis(250);
        EvaluatorLoop loop = new EvaluatorLoop(receivedTimeout -> {
            assertEquals(timeout, receivedTimeout);
            return polls.incrementAndGet();
        }, timeout);

        loop.run(() -> polls.get() < 3);

        assertEquals(3, polls.get());
    }

    @Test
    void propagatesPollingFailure() {
        AtomicInteger polls = new AtomicInteger();
        EvaluatorLoop loop = new EvaluatorLoop(timeout -> {
            polls.incrementAndGet();
            throw new SQLException("database unavailable");
        }, Duration.ofSeconds(1));

        SQLException exception = assertThrows(
                SQLException.class,
                () -> loop.run(() -> true));

        assertEquals("database unavailable", exception.getMessage());
        assertEquals(1, polls.get());
    }
}
