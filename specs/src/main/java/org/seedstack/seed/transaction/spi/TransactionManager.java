/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.transaction.spi;

import org.aopalliance.intercept.MethodInterceptor;

/**
 * This interface must be implemented by transaction managers. Transaction managers are responsible to intercept
 * and handle transactional method calls.
 */
public interface TransactionManager {
    /**
     * Returns the method interceptor that implements the transactional behavior.
     *
     * @return the {@link org.aopalliance.intercept.MethodInterceptor} implementing the generic transactional behavior.
     */
    MethodInterceptor getMethodInterceptor();
}
