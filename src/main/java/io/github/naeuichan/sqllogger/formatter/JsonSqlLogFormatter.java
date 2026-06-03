package io.github.naeuichan.sqllogger.formatter;

public class JsonSqlLogFormatter implements SqlLogFormatter {

    @Override
    public String format(String sql, long elapsedMs) {
        String escapedSql = sql.replace("\\", "\\\\")
                               .replace("\"", "\\\"")
                               .replace("\n", "\\n")
                               .replace("\r", "\\r")
                               .replace("\t", "\\t");
        if (elapsedMs < 0) {
            return String.format("{\"type\":\"SQL-ERROR\",\"query\":\"%s\"}", escapedSql);
        }
        return String.format("{\"type\":\"SQL\",\"elapsed_ms\":%d,\"query\":\"%s\"}", elapsedMs, escapedSql);
    }
}
