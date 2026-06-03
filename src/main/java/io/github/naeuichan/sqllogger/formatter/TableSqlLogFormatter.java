package io.github.naeuichan.sqllogger.formatter;

import io.github.naeuichan.sqllogger.SqlLogContext;

import java.time.format.DateTimeFormatter;

/**
 * 표(table) 형식으로 SQL을 출력한다.
 *
 * ┌────────────────────────────────────────────────────┐
 * │ SQL                                                │
 * ├────────────┬───────────────────────────────────────┤
 * │ time       │ 2026-06-03 17:43:42.123               │
 * │ connection │ conn-1                                │
 * │ elapsed    │ 5ms                                   │
 * ├────────────┼───────────────────────────────────────┤
 * │ query      │ SELECT id,                            │
 * │            │        name                           │
 * │            │   FROM users                          │
 * │            │  WHERE id = 1                         │
 * │            │    AND name = 'alice'                 │
 * └────────────┴───────────────────────────────────────┘
 */
public class TableSqlLogFormatter implements SqlLogFormatter {

    private static final int LABEL_WIDTH = 10; // "connection".length()
    private static final int MIN_VALUE_WIDTH = 40;
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String format(SqlLogContext context) {
        String formattedSql = SqlPrettyPrinter.format(context.getSql());
        String[] sqlLines = formattedSql.split("\n");

        String timeStr    = context.getExecutedAt().format(TIME_FORMATTER);
        String connStr    = context.getConnectionId();
        String elapsedStr = context.isError() ? "ERROR" : context.getElapsedMs() + "ms";
        String title      = context.isError() ? "SQL-ERROR" : "SQL";

        int valueWidth = MIN_VALUE_WIDTH;
        valueWidth = Math.max(valueWidth, timeStr.length());
        valueWidth = Math.max(valueWidth, connStr.length());
        valueWidth = Math.max(valueWidth, elapsedStr.length());
        for (String line : sqlLines) {
            valueWidth = Math.max(valueWidth, line.length());
        }

        // 각 구획의 수평선: "─" * (LABEL_WIDTH + 2), "─" * (valueWidth + 2)
        String labelHr = "─".repeat(LABEL_WIDTH + 2);
        String valueHr = "─".repeat(valueWidth + 2);
        int innerWidth  = LABEL_WIDTH + 2 + 1 + valueWidth + 2;

        StringBuilder sb = new StringBuilder();

        // 상단 테두리 + 제목
        sb.append("┌").append("─".repeat(innerWidth)).append("┐\n");
        sb.append("│ ").append(pad(title, innerWidth - 2)).append(" │\n");
        sb.append("├").append(labelHr).append("┬").append(valueHr).append("┤\n");

        // 메타 정보
        row(sb, "time",       timeStr,    valueWidth);
        row(sb, "connection", connStr,    valueWidth);
        row(sb, "elapsed",    elapsedStr, valueWidth);

        // 쿼리 구분선
        sb.append("├").append(labelHr).append("┼").append(valueHr).append("┤\n");

        // 쿼리 (첫 줄만 "query" 레이블, 이후는 빈 레이블로 정렬)
        for (int i = 0; i < sqlLines.length; i++) {
            row(sb, i == 0 ? "query" : "", sqlLines[i], valueWidth);
        }

        // 하단 테두리
        sb.append("└").append(labelHr).append("┴").append(valueHr).append("┘");

        return "\n" + sb;
    }

    private void row(StringBuilder sb, String label, String value, int valueWidth) {
        sb.append("│ ")
          .append(pad(label, LABEL_WIDTH))
          .append(" │ ")
          .append(pad(value, valueWidth))
          .append(" │\n");
    }

    private String pad(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        return s + " ".repeat(width - s.length());
    }
}
