package io.github.naeuichan.sqllogger.formatter;

import io.github.naeuichan.sqllogger.SqlLogContext;

public interface SqlLogFormatter {
    String format(SqlLogContext context);
}
