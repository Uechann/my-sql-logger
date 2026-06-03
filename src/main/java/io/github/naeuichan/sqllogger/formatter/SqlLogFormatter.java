package io.github.naeuichan.sqllogger.formatter;

public interface SqlLogFormatter {
    /**
     * @param sql       실행된 SQL (파라미터 치환 완료)
     * @param elapsedMs 실행 시간 (ms), 실패 시 -1
     */
    String format(String sql, long elapsedMs);
}
