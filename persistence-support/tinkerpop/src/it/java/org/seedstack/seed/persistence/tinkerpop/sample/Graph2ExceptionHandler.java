/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.tinkerpop.sample;

import com.tinkerpop.blueprints.Graph;
import org.seedstack.seed.core.api.Logging;
import org.seedstack.seed.persistence.tinkerpop.api.GraphExceptionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;
import org.slf4j.Logger;

public class Graph2ExceptionHandler implements GraphExceptionHandler {
    private boolean handled = false;

    @Logging
    Logger logger;

    @Override
    public boolean handleException(Exception exception, TransactionMetadata associatedTransactionMetadata, Graph associatedTransaction) {
        handled = true;

        logger.debug("inside exception handler");
        return true;
    }

    public boolean hasHandled() {
        return handled;
    }

}