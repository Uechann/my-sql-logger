package io.github.naeuichan.sqllogger.formatter;

import io.github.naeuichan.sqllogger.SqlLogContext;

public class SingleLineSqlLogFormatter implements SqlLogFormatter {

    @Override
    public String format(SqlLogContext context) {
        if (context.isError()) {
            return String.format("[SQL-ERROR] [%s] %s", context.getConnectionId(), context.getSql());
        }
        return String.format("[SQL] %dms | [%s] %s",
                context.getElapsedMs(), context.getConnectionId(), context.getSql());
    }
}
