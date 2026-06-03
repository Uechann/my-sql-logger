package io.github.naeuichan.sqllogger.formatter;

public class SingleLineSqlLogFormatter implements SqlLogFormatter {

    @Override
    public String format(String sql, long elapsedMs) {
        if (elapsedMs < 0) {
            return "[SQL-ERROR] " + sql;
        }
        return String.format("[SQL] %dms | %s", elapsedMs, sql);
    }
}
