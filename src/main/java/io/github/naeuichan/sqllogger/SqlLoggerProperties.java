package io.github.naeuichan.sqllogger;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sql-logger")
public class SqlLoggerProperties {

    private boolean enabled = true;

    /** -1이면 모든 쿼리 출력, 그 외 값이면 해당 ms 초과 쿼리만 출력 */
    private long slowQueryThresholdMs = -1;

    private Format format = Format.TABLE;

    private boolean showExecutionTime = true;

    private boolean showParameters = true;

    public enum Format {
        SINGLE_LINE, MULTI_LINE, JSON, TABLE
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public long getSlowQueryThresholdMs() { return slowQueryThresholdMs; }
    public void setSlowQueryThresholdMs(long slowQueryThresholdMs) { this.slowQueryThresholdMs = slowQueryThresholdMs; }

    public Format getFormat() { return format; }
    public void setFormat(Format format) { this.format = format; }

    public boolean isShowExecutionTime() { return showExecutionTime; }
    public void setShowExecutionTime(boolean showExecutionTime) { this.showExecutionTime = showExecutionTime; }

    public boolean isShowParameters() { return showParameters; }
    public void setShowParameters(boolean showParameters) { this.showParameters = showParameters; }
}
