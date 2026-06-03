package io.github.naeuichan.sqllogger.formatter;

public class MultiLineSqlLogFormatter implements SqlLogFormatter {

    @Override
    public String format(String sql, long elapsedMs) {
        if (elapsedMs < 0) {
            return String.format("""
                    [SQL-ERROR]
                    query : %s
                    """, sql);
        }
        return String.format("""
                [SQL]
                time  : %dms
                query : %s
                """, elapsedMs, sql);
    }
}
