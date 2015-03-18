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
package org.seedstack.seed.persistence.jdbc.api;

import org.seedstack.seed.persistence.jdbc.internal.JdbcTransaction;
import org.seedstack.seed.transaction.spi.ExceptionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;

/**
 * Transaction exception handler for JDBC transactions
 */
public interface JdbcExceptionHandler extends ExceptionHandler<JdbcTransaction> {

    @Override
    boolean handleException(Exception exception, TransactionMetadata associatedTransactionMetadata, JdbcTransaction associatedTransaction);

}
