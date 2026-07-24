package com.logsentinel.evaluator.alert;

import com.logsentinel.contracts.Alert;

import java.sql.SQLException;

@FunctionalInterface
public interface AlertRepository {

    void save(Alert alert) throws SQLException;
}
