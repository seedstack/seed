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
 * Creation : 17 févr. 2015
 */
package org.seedstack.seed.persistence.jdbc.internal;

import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.persistence.jdbc.api.JdbcErrorCode;
import org.seedstack.seed.persistence.jdbc.api.JdbcTransaction;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The transaction Handler for JDBC. It needs to make sure only one connection is open for each thread. For nested new transactions, the same
 * connection must be passed but a savepoint is created in case of partial rollback.
 */
class JdbcTransactionHandler implements TransactionHandler<JdbcTransaction> {

    private final DataSource dataSource;

    private final JdbcConnectionLink jdbcConnectionLink;

    /**
     * Constructor
     * 
     * @param jdbcConnectionLink jdbc link for the connection
     * @param dataSource the datasource to use when needing a new connection
     */
    JdbcTransactionHandler(JdbcConnectionLink jdbcConnectionLink, DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcConnectionLink = jdbcConnectionLink;
    }

    @Override
    public void doInitialize(TransactionMetadata transactionMetadata) {
        Connection connection = jdbcConnectionLink.getCurrentConnection();
        JdbcTransaction transaction;
        try {
            if (connection == null) {
                connection = dataSource.getConnection();
                connection.setAutoCommit(false);
                transaction = new JdbcTransaction(connection);
            } else {
                transaction = new JdbcTransaction(connection, connection.setSavepoint());
            }
        } catch (SQLException e) {
            throw SeedException.wrap(e, JdbcErrorCode.CANNOT_CONNECT_TO_JDBC_DATASOURCE);
        }
        jdbcConnectionLink.push(transaction);
    }

    @Override
    public JdbcTransaction doCreateTransaction() {
        return jdbcConnectionLink.getCurrentTransaction();
    }

    @Override
    public void doJoinGlobalTransaction() {
        throw new UnsupportedOperationException("JDBC persistence implementation does not support global transactions");
    }

    @Override
    public void doBeginTransaction(JdbcTransaction transaction) {
        // Nothing to do
    }

    @Override
    public void doCommitTransaction(JdbcTransaction transaction) {
        try {
            transaction.commit();
        } catch (SQLException e) {
            throw SeedException.wrap(e, JdbcErrorCode.JDBC_COMMIT_EXCEPTION);
        }
    }

    @Override
    public void doMarkTransactionAsRollbackOnly(JdbcTransaction transaction) {
        transaction.setRollbackOnly();
    }

    @Override
    public void doRollbackTransaction(JdbcTransaction transaction) {
        try {
            transaction.rollBack();
        } catch (SQLException e) {
            throw SeedException.wrap(e, JdbcErrorCode.JDBC_ROLLBACK_EXCEPTION);
        }
    }

    @Override
    public void doReleaseTransaction(JdbcTransaction transaction) {
        if (transaction.getRollbackOnly())
            doRollbackTransaction(transaction);
    }

    @Override
    public void doCleanup() {
        try {
            if (jdbcConnectionLink.isLastTransaction()) {
                jdbcConnectionLink.pop().getConnection().close();
            } else {
                jdbcConnectionLink.pop();
            }
        } catch (SQLException e) {
            throw SeedException.wrap(e, JdbcErrorCode.JDBC_CLOSE_EXCEPTION);
        }
    }

    @Override
    public JdbcTransaction getCurrentTransaction() {
        return jdbcConnectionLink.getCurrentTransaction();
    }

}
