package io.github.naeuichan.sqllogger;

import java.time.LocalDateTime;

public class SqlLogContext {

    private final String sql;
    private final long elapsedMs;
    private final String connectionId;
    private final LocalDateTime executedAt;

    public SqlLogContext(String sql, long elapsedMs, String connectionId, LocalDateTime executedAt) {
        this.sql = sql;
        this.elapsedMs = elapsedMs;
        this.connectionId = connectionId;
        this.executedAt = executedAt;
    }

    public String getSql() { return sql; }
    public long getElapsedMs() { return elapsedMs; }
    public String getConnectionId() { return connectionId; }
    public LocalDateTime getExecutedAt() { return executedAt; }
    public boolean isError() { return elapsedMs < 0; }
}
