package com.logsentinel.evaluator.alert;

import com.logsentinel.contracts.Alert;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgresAlertRepositoryTest {

    @Test
    void insertsAlertWithItsSourceEventId() throws Exception {
        CapturingDataSource dataSource = new CapturingDataSource(1);
        PostgresAlertRepository repository = new PostgresAlertRepository(dataSource);
        Alert alert = alert();

        boolean inserted = repository.save(alert);

        assertTrue(inserted);
        assertEquals(
                """
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
                        """,
                dataSource.sql);
        assertEquals(
                List.of(
                        "event-123",
                        "gateway-errors",
                        "gateway",
                        Timestamp.from(alert.triggeredAt()),
                        "Payment gateway timeout",
                        "ERROR"),
                dataSource.parameters);
    }

    @Test
    void reportsDuplicateAlertAsNotInserted() throws Exception {
        PostgresAlertRepository repository = new PostgresAlertRepository(
                new CapturingDataSource(0));

        boolean inserted = repository.save(alert());

        assertFalse(inserted);
    }

    private Alert alert() {
        return new Alert(
                "event-123",
                "gateway-errors",
                "gateway",
                Instant.parse("2025-07-01T12:56:07.451Z"),
                "Payment gateway timeout",
                "ERROR");
    }

    private static final class CapturingDataSource implements DataSource {
        private final List<Object> parameters = new ArrayList<>();
        private final int updatedRows;
        private String sql;

        private CapturingDataSource(int updatedRows) {
            this.updatedRows = updatedRows;
        }

        @Override
        public Connection getConnection() {
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    (proxy, method, args) -> {
                        if ("prepareStatement".equals(method.getName())) {
                            sql = (String) args[0];
                            return Proxy.newProxyInstance(
                                    PreparedStatement.class.getClassLoader(),
                                    new Class<?>[]{PreparedStatement.class},
                                    new PreparedStatementHandler(parameters, updatedRows));
                        }
                        if ("close".equals(method.getName())) {
                            return null;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }

        @Override
        public Connection getConnection(String username, String password) {
            return getConnection();
        }

        @Override
        public java.io.PrintWriter getLogWriter() {
            return null;
        }

        @Override
        public void setLogWriter(java.io.PrintWriter out) {
        }

        @Override
        public void setLoginTimeout(int seconds) {
        }

        @Override
        public int getLoginTimeout() {
            return 0;
        }

        @Override
        public java.util.logging.Logger getParentLogger() {
            return java.util.logging.Logger.getLogger("test");
        }

        @Override
        public <T> T unwrap(Class<T> iface) {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }
    }

    private static final class PreparedStatementHandler implements InvocationHandler {
        private final List<Object> parameters;
        private final int updatedRows;

        private PreparedStatementHandler(List<Object> parameters, int updatedRows) {
            this.parameters = parameters;
            this.updatedRows = updatedRows;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "setString":
                case "setTimestamp":
                    parameters.add(args[1]);
                    return null;
                case "executeUpdate":
                    return updatedRows;
                case "close":
                    return null;
                default:
                    return defaultValue(method.getReturnType());
            }
        }
    }

    private static Object defaultValue(Class<?> type) {
        if (boolean.class.equals(type)) {
            return false;
        }
        if (int.class.equals(type)) {
            return 0;
        }
        if (long.class.equals(type)) {
            return 0L;
        }
        return null;
    }
}
