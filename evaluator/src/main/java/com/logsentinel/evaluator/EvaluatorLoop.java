package com.logsentinel.evaluator;

import java.sql.SQLException;
import java.time.Duration;
import java.util.function.BooleanSupplier;

public final class EvaluatorLoop {

    private final PollAction pollAction;
    private final Duration pollTimeout;

    public EvaluatorLoop(PollAction pollAction, Duration pollTimeout) {
        this.pollAction = pollAction;
        this.pollTimeout = pollTimeout;
    }

    public void run(BooleanSupplier keepRunning) throws SQLException {
        while (keepRunning.getAsBoolean()) {
            pollAction.poll(pollTimeout);
        }
    }

    @FunctionalInterface
    public interface PollAction {
        int poll(Duration timeout) throws SQLException;
    }
}
