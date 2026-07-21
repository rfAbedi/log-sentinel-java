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

class PostgresAlertRepositoryTest {

    @Test
    void savesAlertToPostgresTable() throws Exception {
        CapturingDataSource dataSource = new CapturingDataSource();
        PostgresAlertRepository repository = new PostgresAlertRepository(dataSource);

        Alert alert = new Alert(
                "gateway-errors",
                "gateway",
                Instant.parse("2025-07-01T12:56:07.451Z"),
                "Payment gateway timeout",
                "ERROR");

        repository.save(alert);

        assertEquals(
                "INSERT INTO alerts (rule_id, component, triggered_at, message, level) VALUES (?, ?, ?, ?, ?)",
                dataSource.sql);
        assertEquals(
                List.of(
                        "gateway-errors",
                        "gateway",
                        Timestamp.from(alert.triggeredAt()),
                        "Payment gateway timeout",
                        "ERROR"),
                dataSource.parameters);
    }

    private static final class CapturingDataSource implements DataSource {
        private final List<Object> parameters = new ArrayList<>();
        private String sql;

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
                                    new PreparedStatementHandler(parameters));
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

        private PreparedStatementHandler(List<Object> parameters) {
            this.parameters = parameters;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "setString":
                    parameters.add(args[1]);
                    return null;
                case "setTimestamp":
                    parameters.add(args[1]);
                    return null;
                case "executeUpdate":
                    return 1;
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
