/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.jpa.sample;

import org.seedstack.seed.core.api.Logging;
import org.seedstack.seed.persistence.jpa.api.JpaExceptionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;
import org.slf4j.Logger;

import javax.persistence.EntityTransaction;

public class Unit3ExceptionHandler implements JpaExceptionHandler {
    private boolean handled = false;

    @Logging
    Logger logger;

    @Override
    public boolean handleException(Exception exception, TransactionMetadata associatedTransactionMetadata, EntityTransaction associatedTransaction) {
        handled = true;

        logger.debug("inside exception handler");
        return true;
    }

    public boolean hasHandled() {
        return handled;
    }

}