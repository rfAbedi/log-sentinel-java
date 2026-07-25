package com.logsentinel.evaluator.alert;

import com.logsentinel.contracts.Alert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public final class PostgresAlertRepository implements AlertRepository {

    private static final String INSERT_ALERT = """
            INSERT INTO alerts (
                source_event_id,
                rule_id,
                component,
                triggered_at,
                message,
                level
            )
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (rule_id, source_event_id) DO NOTHING
            """;

    private final DataSource dataSource;

    public PostgresAlertRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean save(Alert alert) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_ALERT)) {
            statement.setString(1, alert.sourceEventId());
            statement.setString(2, alert.ruleId());
            statement.setString(3, alert.component());
            statement.setTimestamp(4, Timestamp.from(alert.triggeredAt()));
            statement.setString(5, alert.message());
            statement.setString(6, alert.level());
            return statement.executeUpdate() == 1;
        }
    }
}
