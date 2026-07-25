package com.logsentinel.evaluator.alert;

import com.logsentinel.contracts.Alert;

import java.sql.SQLException;

@FunctionalInterface
public interface AlertRepository {

    boolean save(Alert alert) throws SQLException;
}
