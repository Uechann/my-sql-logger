package io.github.naeuichan.sqllogger.formatter;

import io.github.naeuichan.sqllogger.SqlLogContext;

public class JsonSqlLogFormatter implements SqlLogFormatter {

    @Override
    public String format(SqlLogContext context) {
        String escaped = escape(context.getSql());
        if (context.isError()) {
            return String.format(
                "{\"type\":\"SQL-ERROR\",\"connection\":\"%s\",\"query\":\"%s\"}",
                context.getConnectionId(), escaped);
        }
        return String.format(
            "{\"type\":\"SQL\",\"connection\":\"%s\",\"elapsed_ms\":%d,\"query\":\"%s\"}",
            context.getConnectionId(), context.getElapsedMs(), escaped);
    }

    private String escape(String sql) {
        return sql.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
