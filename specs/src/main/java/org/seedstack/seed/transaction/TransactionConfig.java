/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.transaction;

import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;
import org.seedstack.seed.transaction.spi.TransactionHandler;
import org.seedstack.seed.transaction.spi.TransactionManager;

@Config("transaction")
public class TransactionConfig {
    @SingleValue
    private Class<? extends TransactionManager> manager;
    private Class<? extends TransactionHandler<?>> defaultHandler;
    private JtaConfig jta = new JtaConfig();

    public Class<? extends TransactionManager> getManager() {
        return manager;
    }

    public TransactionConfig setManager(Class<? extends TransactionManager> manager) {
        this.manager = manager;
        return this;
    }

    public Class<? extends TransactionHandler<?>> getDefaultHandler() {
        return defaultHandler;
    }

    public TransactionConfig setDefaultHandler(Class<? extends TransactionHandler<?>> defaultHandler) {
        this.defaultHandler = defaultHandler;
        return this;
    }

    public JtaConfig jta() {
        return jta;
    }

    @Config("jta")
    public static class JtaConfig {
        private static final String DEFAULT_USER_TRANSACTION_NAME = "java:comp/UserTransaction";
        @SingleValue
        private String txManagerName;
        private String userTxName = DEFAULT_USER_TRANSACTION_NAME;

        public String getTxManagerName() {
            return txManagerName;
        }

        public JtaConfig setTxManagerName(String txManagerName) {
            this.txManagerName = txManagerName;
            return this;
        }

        public String getUserTxName() {
            return userTxName;
        }

        public JtaConfig setUserTxName(String userTxName) {
            this.userTxName = userTxName;
            return this;
        }
    }
}
