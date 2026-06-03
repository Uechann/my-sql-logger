package io.github.naeuichan.sqllogger.proxy;

import io.github.naeuichan.sqllogger.SqlLoggerProperties;
import io.github.naeuichan.sqllogger.formatter.SqlLogFormatter;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class ProxyDataSource implements DataSource {

    private final DataSource delegate;
    private final SqlLoggerProperties properties;
    private final SqlLogFormatter formatter;

    public ProxyDataSource(DataSource delegate, SqlLoggerProperties properties, SqlLogFormatter formatter) {
        this.delegate = delegate;
        this.properties = properties;
        this.formatter = formatter;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = delegate.getConnection();
        return new ProxyConnection(connection, properties, formatter);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = delegate.getConnection(username, password);
        return new ProxyConnection(connection, properties, formatter);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException { return delegate.getLogWriter(); }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException { delegate.setLogWriter(out); }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException { delegate.setLoginTimeout(seconds); }

    @Override
    public int getLoginTimeout() throws SQLException { return delegate.getLoginTimeout(); }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException { return delegate.getParentLogger(); }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException { return delegate.unwrap(iface); }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException { return delegate.isWrapperFor(iface); }
}
