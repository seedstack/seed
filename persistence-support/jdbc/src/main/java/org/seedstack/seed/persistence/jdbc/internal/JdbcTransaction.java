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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * Represents a JDBC transaction. In JDBC you can only have one connection per thread. If partial rollback are needed, we use the Savepoint object on
 * the already known connection.
 */
public class JdbcTransaction {

    private Connection connection;

    private Savepoint savepoint;

    private boolean rollbackOnly;

    /**
     * Constructor
     * 
     * @param connection the jdbc connection
     */
    public JdbcTransaction(Connection connection) {
        this.connection = connection;
    }

    /**
     * Constructor
     * 
     * @param connection the jdbc connection
     * @param savepoint the savepoint
     */
    public JdbcTransaction(Connection connection, Savepoint savepoint) {
        this.connection = connection;
        this.savepoint = savepoint;
    }

    /**
     * Commits the transaction, thus the underlying connection
     * 
     * @throws SQLException when db error
     */
    public void commit() throws SQLException {
        connection.commit();
    }

    /**
     * Rollbacks the transaction, thus the underlying connection
     * 
     * @throws SQLException when db error
     */
    public void rollBack() throws SQLException {
        if (savepoint != null)
            connection.rollback(savepoint);
        else
            connection.rollback();
    }

    /**
     * If the transaction is marked as rollback only
     */
    public void setRollbackOnly() {
        this.rollbackOnly = true;
    }

    /**
     * Whether the transaction is marked as rollback only or not
     * 
     * @return is rollback only
     */
    public boolean getRollbackOnly() {
        return rollbackOnly;
    }

    /**
     * Gets the connection of this transaction
     * 
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }
}
