/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 18 févr. 2015
 */
package org.seedstack.seed.persistence.jdbc.internal.datasource;

import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.persistence.jdbc.api.JdbcErrorCode;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * A simple datasource that will take its connection directly from the driver. No pooling or anything.
 *
 * @author yves.dautremay@mpsa.com
 */
public class PlainDataSource implements DataSource {

    private int timeout;

    private PrintWriter writer = new PrintWriter(System.out);

    private String url;

    private Properties properties;

    public PlainDataSource(String driver, String url, String user, String password, Properties jdbcProperties) {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw SeedException.wrap(e, JdbcErrorCode.WRONG_JDBC_DRIVER);
        }

        this.url = url;
        jdbcProperties.put("user", user);
        jdbcProperties.put("password", password);
        this.properties = jdbcProperties;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return writer;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.writer = out;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.timeout = seconds;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return timeout;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        throw new SQLException("Wrapped DataSource is not an instance of " + iface);
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, properties);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException("Not supported by PlainDataSource");
    }

}
