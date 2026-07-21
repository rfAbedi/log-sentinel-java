package com.logsentinel.evaluator.alert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public final class PostgresAlertRepository {

    private final DataSource dataSource;

    public PostgresAlertRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(Alert alert) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO alerts (rule_id, component, triggered_at, message, level) VALUES (?, ?, ?, ?, ?)")) {
            statement.setString(1, alert.ruleId());
            statement.setString(2, alert.component());
            statement.setTimestamp(3, Timestamp.from(alert.triggeredAt()));
            statement.setString(4, alert.message());
            statement.setString(5, alert.level());
            statement.executeUpdate();
        }
    }
}
