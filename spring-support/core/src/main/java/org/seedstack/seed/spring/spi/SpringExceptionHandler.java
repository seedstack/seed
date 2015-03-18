/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 * 
 */
package org.seedstack.seed.spring.spi;

import org.seedstack.seed.transaction.spi.ExceptionHandler;
import org.springframework.transaction.TransactionStatus;

import org.seedstack.seed.transaction.spi.TransactionMetadata;

/**
 * Exception handler for Spring transactions.
 * 
 * @author redouane.loulou@ext.mpsa.com
 *
 */
public interface SpringExceptionHandler extends ExceptionHandler<TransactionStatus> {

    @Override
    boolean handleException(Exception exception, TransactionMetadata associatedTransactionMetadata, TransactionStatus associatedTransaction);

}
