package io.github.naeuichan.sqllogger.formatter;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SQL 키워드 대문자화, 절(Clause)별 줄 바꿈, SELECT 컬럼 정렬을 수행한다.
 *
 * 모든 주요 절은 오른쪽 정렬(column 6)로 출력된다:
 *   SELECT col1,   ← 6자
 *          col2    ← 7칸 들여쓰기 (SELECT + 공백)
 *     FROM table   ← "  FROM" = 6자
 *    WHERE cond    ← " WHERE" = 6자
 *      AND cond    ← "   AND" = 6자
 */
public class SqlPrettyPrinter {

    // 긴 키워드가 짧은 접두사보다 먼저 매칭되어야 하므로 순서가 중요하다
    private static final Map<String, String> KEYWORD_DISPLAY = new LinkedHashMap<>();
    private static final List<String> KEYWORDS;

    // SELECT 이후 컬럼 정렬 기준: "SELECT ".length() = 7
    private static final int SELECT_INDENT = 7;

    static {
        KEYWORD_DISPLAY.put("LEFT OUTER JOIN", "LEFT OUTER JOIN");
        KEYWORD_DISPLAY.put("RIGHT OUTER JOIN", "RIGHT OUTER JOIN");
        KEYWORD_DISPLAY.put("FULL OUTER JOIN", "FULL OUTER JOIN");
        KEYWORD_DISPLAY.put("CROSS JOIN", "CROSS JOIN");
        KEYWORD_DISPLAY.put("INNER JOIN", "INNER JOIN");
        KEYWORD_DISPLAY.put("LEFT JOIN", "LEFT JOIN");
        KEYWORD_DISPLAY.put("RIGHT JOIN", "RIGHT JOIN");
        KEYWORD_DISPLAY.put("INSERT INTO", "INSERT INTO");
        KEYWORD_DISPLAY.put("DELETE FROM", "DELETE FROM");
        KEYWORD_DISPLAY.put("GROUP BY", "GROUP BY");
        KEYWORD_DISPLAY.put("ORDER BY", "ORDER BY");
        KEYWORD_DISPLAY.put("UNION ALL", "UNION ALL");
        // 6자 기준 오른쪽 정렬
        KEYWORD_DISPLAY.put("SELECT", "SELECT");
        KEYWORD_DISPLAY.put("UPDATE", "UPDATE");
        KEYWORD_DISPLAY.put("HAVING", "HAVING");
        KEYWORD_DISPLAY.put("OFFSET", "OFFSET");
        KEYWORD_DISPLAY.put("VALUES", "VALUES");
        KEYWORD_DISPLAY.put("FROM",   "  FROM");
        KEYWORD_DISPLAY.put("WHERE",  " WHERE");
        KEYWORD_DISPLAY.put("LIMIT",  " LIMIT");
        KEYWORD_DISPLAY.put("UNION",  " UNION");
        KEYWORD_DISPLAY.put("JOIN",   "  JOIN");
        KEYWORD_DISPLAY.put("SET",    "   SET");
        KEYWORD_DISPLAY.put("AND",    "   AND");
        KEYWORD_DISPLAY.put("ON",     "    ON");
        KEYWORD_DISPLAY.put("OR",     "    OR");
        KEYWORDS = List.copyOf(KEYWORD_DISPLAY.keySet());
    }

    private SqlPrettyPrinter() {}

    public static String format(String sql) {
        if (sql == null || sql.isBlank()) return sql;
        String normalized = normalizeWhitespace(sql);
        String formatted = insertNewlines(normalized);
        return stripTrailingSpaces(formatted);
    }

    /**
     * 문자열 리터럴 내부는 건드리지 않고 연속 공백을 단일 공백으로 정규화한다.
     */
    private static String normalizeWhitespace(String sql) {
        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '\'') inString = !inString;
            if (!inString && Character.isWhitespace(c)) {
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                    sb.append(' ');
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString().trim();
    }

    private static String insertNewlines(String sql) {
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean firstClause = true;
        String currentClause = null;
        int parenDepth = 0;
        int i = 0;

        while (i < sql.length()) {
            char c = sql.charAt(i);

            if (c == '\'') {
                inString = !inString;
                result.append(c);
                i++;
                continue;
            }
            if (inString) {
                result.append(c);
                i++;
                continue;
            }
            if (c == '(') { parenDepth++; result.append(c); i++; continue; }
            if (c == ')') { parenDepth--; result.append(c); i++; continue; }

            // 최상위 레벨(서브쿼리 밖)에서만 키워드 포맷팅 적용
            if (parenDepth == 0 && isWordBoundary(sql, i)) {
                String keyword = matchKeyword(sql, i);
                if (keyword != null) {
                    if (!firstClause) {
                        // 앞의 trailing space 제거 후 줄 바꿈
                        if (result.length() > 0 && result.charAt(result.length() - 1) == ' ') {
                            result.deleteCharAt(result.length() - 1);
                        }
                        result.append('\n');
                    }
                    result.append(KEYWORD_DISPLAY.get(keyword)).append(' ');
                    i += keyword.length();
                    while (i < sql.length() && sql.charAt(i) == ' ') i++;
                    currentClause = keyword;
                    firstClause = false;
                    continue;
                }
            }

            // SELECT 절의 최상위 콤마에서 컬럼 정렬
            if ("SELECT".equals(currentClause) && c == ',' && parenDepth == 0) {
                result.append(',').append('\n').append(" ".repeat(SELECT_INDENT));
                i++;
                while (i < sql.length() && sql.charAt(i) == ' ') i++;
                continue;
            }

            result.append(c);
            i++;
        }

        return result.toString();
    }

    private static boolean isWordBoundary(String sql, int pos) {
        if (pos == 0) return true;
        char prev = sql.charAt(pos - 1);
        return !Character.isLetterOrDigit(prev) && prev != '_';
    }

    private static String matchKeyword(String sql, int pos) {
        for (String keyword : KEYWORDS) {
            int end = pos + keyword.length();
            if (end > sql.length()) continue;
            if (!sql.substring(pos, end).equalsIgnoreCase(keyword)) continue;
            // 키워드 끝이 단어 경계인지 확인
            if (end < sql.length()) {
                char next = sql.charAt(end);
                if (Character.isLetterOrDigit(next) || next == '_') continue;
            }
            return keyword;
        }
        return null;
    }

    private static String stripTrailingSpaces(String s) {
        return Arrays.stream(s.split("\n"))
                .map(String::stripTrailing)
                .collect(Collectors.joining("\n"));
    }
}
