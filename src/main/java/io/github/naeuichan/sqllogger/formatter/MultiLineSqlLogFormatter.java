package io.github.naeuichan.sqllogger.formatter;

import io.github.naeuichan.sqllogger.SqlLogContext;

import java.time.format.DateTimeFormatter;

public class MultiLineSqlLogFormatter implements SqlLogFormatter {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String format(SqlLogContext context) {
        String formattedSql = SqlPrettyPrinter.format(context.getSql());
        if (context.isError()) {
            return String.format("[SQL-ERROR]\nconnection : %s\nquery :\n%s",
                    context.getConnectionId(), formattedSql);
        }
        return String.format("[SQL]\ntime       : %s\nconnection : %s\nelapsed    : %dms\nquery :\n%s",
                context.getExecutedAt().format(TIME_FORMATTER),
                context.getConnectionId(),
                context.getElapsedMs(),
                formattedSql);
    }
}
