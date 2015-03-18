/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.tinkerpop.api;

import com.tinkerpop.blueprints.Graph;
import org.seedstack.seed.transaction.spi.ExceptionHandler;
import org.seedstack.seed.transaction.spi.TransactionMetadata;

/**
 * A throwable transaction handler
 *
 * @author epo.jemba@ext.mpsa.com
 */
public interface GraphExceptionHandler extends ExceptionHandler<Graph> {

    @Override
    boolean handleException(Exception exception, TransactionMetadata associatedTransactionMetadata, Graph associatedTransaction);

}
