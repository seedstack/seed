/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.neo4j.fixtures;

import org.neo4j.graphdb.Transaction;
import org.seedstack.seed.core.api.Logging;
import org.seedstack.seed.it.api.ITBind;
import org.seedstack.seed.persistence.neo4j.api.Neo4jExceptionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;
import org.slf4j.Logger;

@ITBind
public class ItemThrowableHandler implements Neo4jExceptionHandler {
    private static boolean handled = false;

    @Logging
    Logger logger;

    @Override
    public boolean handleException(Exception exception, TransactionMetadata associatedTransactionMetadata, Transaction associatedTransaction) {
        handled = true;
        logger.debug("inside thowable handler");
        return true;
    }

    public boolean hasHandled() {
        return handled;
    }

}