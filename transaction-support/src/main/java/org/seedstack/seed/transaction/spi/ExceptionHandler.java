/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.transaction.spi;

/**
 * Generic exception handler to add custom behaviour on exception during transaction.
 *
 * @param <T> the transaction object type
 * @author epo.jemba@ext.mpsa.com
 */
public interface ExceptionHandler<T> {

    /**
     * Called when an exception occurred during transaction.
     *
     * @param exception the exception that occurred.
     * @param associatedTransactionMetadata the associated transaction metadata.
     * @param associatedTransaction the associated transaction object.
     * @return true if it handled the error and as such transaction should continue normally, false otherwise.
     */
    boolean handleException(Exception exception, TransactionMetadata associatedTransactionMetadata, T associatedTransaction);

}
